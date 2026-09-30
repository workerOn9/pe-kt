# 技术决策记录（Architecture Decision Records）

> 每条决策记录：背景 → 选项 → 决定 → 后果。后续可追加新条目，已决定的条目修改需说明理由。

## D-01 后端框架：Ktor，不用 Spring Boot

- **背景**：需要一个 Kotlin Web 框架承载 REST API 和静态资源。
- **选项**：Ktor / Spring Boot / Javalin / http4k。
- **决定**：Ktor Server 3.x。
- **理由**：
  - 项目目的之一是练 Kotlin，Ktor 是 Kotlin-first（协程、DSL 路由），Spring Boot 的注解式风格练习价值低；
  - 本项目只有几个只读 API + 一个受控执行端点，Spring 的生态系统优势用不上；
  - Ktor 的 `embeddedServer` 单 jar 启动，运维简单。
- **后果**：插件生态需要自己组装（ContentNegotiation、CORS、StatusPages），这正是学习点。

## D-02 运行时：JVM，暂不上 Kotlin/JS 或 Native

- **背景**：Kotlin 有三套后端可选目标平台。
- **决定**：JVM（OpenJDK 21+）。
- **理由**：`BigInteger`、JIT 性能、成熟调试工具；Native 的 BigInteger 支持不成熟，JS 单线程不适合执行计时。
- **后果**：冷启动和内存占用高于 Native，对本地个人项目无影响。

## D-03 前端：Vite + React + TypeScript（独立子项目）

- **背景**：需要 Markdown/公式/代码高亮/Canvas 可视化的展示站点。
- **选项**：Kotlin/JS + React 包装器 / 服务端模板渲染（Freemarker）/ Vite + React。
- **决定**：Vite + React 19 + TypeScript。
- **理由**：
  - 前端重点在可视化渲染与阅读体验，与"练 Kotlin"目标弱相关，应选熟悉且生态最强的方案，把精力留给后端与内容；
  - Kotlin/JS 的 CSS/DOM 生态和文档明显弱于 TS，迭代成本高；
  - 服务端模板无法支撑交互式可视化播放器。
- **后果**：仓库含两个技术栈；通过 `web/` 子目录与 Gradle task 集成构建产物。

## D-04 解法执行：先进程内隔离，后子进程沙箱

- **背景**：`/run` 端点需要执行用户可触发的解法代码。
- **选项**：直接同进程调用 / 进程内自定义 ClassLoader + SecurityManager / fork 子进程（ProcessBuilder）/ 容器。
- **决定**：MVP 阶段解法都是项目作者自己写的可信代码，先进程内直接调用 + 超时熔断（协程 `withTimeout` + 执行线程池）；Phase 2 末期迁移到子进程隔离。
- **理由**：MVP 阶段攻击面只有自己；子进程方案需要处理 classpath、流控、清理，复杂度高，不适合第一刀。
- **后果**：进程内执行失控的代码（死循环内存暴涨）可能拖垮服务——通过线程池隔离 + 超时缓解，且所有解法代码经过 code review。
- **待验证**：线程池 + withTimeout 对原生阻塞死循环的杀伤可靠性。

## D-05 持久化：v1 无数据库

- **背景**：题目内容是静态资产，运行时数据只有执行结果。
- **决定**：内容 = 文件系统 + JSON 索引；运行结果不落库（每次实时执行）。
- **理由**：无用户、无会话、无写密集场景，引入数据库是纯负担。
- **后果**：若未来加判题历史/统计，需重新评估（倾向 SQLite）。

## D-06 单仓库多模块

- **决定**：`server`（Gradle）+ `web`（Vite）+ `content`（纯资产）同仓。
- **理由**：内容资产与代码强耦合演进（解析引用代码行号）；个人项目无需发布分离。
- **后果**：CI 需构建两个子项目；`content/` 变更不触发前端构建（通过 API 动态加载，无构建期耦合）。

## D-07 部署形态：本地单进程单端口（installDist）

- **背景**：Phase 4 需要确定部署形态，前端与后端如何一起跑（Q-3）。
- **选项**：前后端分端口长期共存 / Ktor 单端口托管前端构建产物 / Docker 镜像 / shadowJar 单 fat-jar。
- **决定**：本地单进程单端口——`npm run build` 产出 `web/dist` → `./gradlew :server:installDist`（application 插件已有）→ 运行 `server/build/install/server/bin/server` 即在 8080 同时托管 API 与前端（SPA fallback）；Docker 暂缓，待有 NAS/VPS 需求再评估。
- **理由**：
  - 选 installDist 而非 shadowJar：零新插件依赖（shadow 需额外引入 Gradle 插件，国内镜像下多一份下载与版本维护），application 插件现成且生成的启动脚本已带正确 classpath 与 JVM 参数入口，个人本地项目够用；
  - 单端口省去 CORS 与端口管理，`PEKT_WEB_DIST` 可覆盖产物路径；目录不存在时自动回退开发模式（前后端分端口），互不影响。
- **后果**：分发物是一个目录而非单文件；将来上 NAS/VPS 时再评估 Docker（届时可直接以 installDist 产物为基础打镜像）。
- **补充（2026-09-12，Docker 落地）**：按本决策的预设路径兑现——根目录 `Dockerfile` 以 `eclipse-temurin:21-jre` 为基座，直接 COPY 本地构建产物（`server/build/install/server/` + `web/dist/` + `content/`），容器内不跑 Gradle/npm（避免容器内拉依赖的网络问题）；`PEKT_CONTENT_DIR=/app/content`、`PEKT_WEB_DIST=/app/web-dist` 通过 ENV 注入。构建命令：`docker build -t pe-kt . && docker run -d --name pe-kt -p 8080:8080 pe-kt`。**教训**：打镜像前必须重新执行 `./gradlew :server:installDist`——曾因 installDist 产物落后于源码（旧 jar 里通配路由是未命名的 `/{...}`，`getAll("path")` 恒为空导致静态资源全部回退 index.html）踩坑，现象是 JS 返回 200 但 Content-Type 为 text/html 的白屏变体。

## D-08 公网部署：Vercel 双服务（前端静态 CDN + 后端容器）

- **背景**：仓库迁到 GitHub 后需要一个公网可访问的部署形态；Vercel 构建机在海外，仓库声明的依赖源必须回到官方默认（见 docs/05-build-toolchain.md）。
- **选项**：单容器（Ktor 沿用单端口托管前端，照 D-07）/ 双服务（`web` 静态服务 + `server` 容器服务）/ 只部署前端静态、后端另找宿主。
- **决定**：双服务，配置在根目录 `vercel.json`：
  - `web`：`root: web/`，按 Vite 预设构建（`npm run build` → `dist`）走 Vercel CDN，另配一条服务内 rewrite `/(.*) → /index.html` 兜住 SPA 深链；
  - `server`：`root: .`、`runtime: container`、`entrypoint: Dockerfile.vercel`，容器内从源码跑 `./gradlew :server:installDist`，只承载 `/api/*` 与 `/health`。
- **理由**：
  - 前端是纯静态内容站，交给 CDN 后首屏与后端冷启动解耦；后端只在 XHR 时被调用，容器伸缩对阅读体验无感；
  - 顶层 rewrite 顺序（`/api/*` → server、`/health` → server、`/(.*)` → web）让每个公开路径只有一个归属，Vercel 的路由是「进入服务即终局」，不存在回退歧义。
- **后果**：
  - 站点变为**公网公开可访问**，与 Q-1 初版「不公开部署」约束冲突，已按用户决定改为公开，Q-1 纪要同步更新；
  - 本地 `Dockerfile`（单端口、COPY 本地产物）保留给本机/NAS 场景，Vercel 走 `Dockerfile.vercel`，两者不要混用；
  - 容器内没有前端产物、不注入 `PEKT_WEB_DIST`，Ktor 启动时会打印「静态托管未启用」，这是预期状态；
  - 监听端口改由 `Application.kt` 的 `resolvePort()` 读取 `PORT` 环境变量（缺省 8080），`Dockerfile.vercel` 声明 `PORT=80` 对齐 Vercel 容器服务的默认流量端口。

## D-09 题面静态资源（图片）托管：内容目录 + API 资产路由

- **背景**：0244 的题面需要展示滑块初始/目标/示例局面三张图，这是题库首次出现图片资源；此前 Markdown 渲染只处理文本与公式，没有任何图片托管路径，相对路径会被 SPA fallback 吞掉（返回 index.html，图片裂开）。
- **选项**：图片放进 `web/public/` 走前端构建 / 在题目目录内保留图片、由后端 API 托管 / 上传第三方图床。
- **决定**：图片留在 `content/problems/XXXX/`（与题面同源，便于校验与搬运），后端新增 `GET /api/problems/{id}/assets/{name}`，只托管题目目录内的单段文件名（字母数字与 `. _ -`，杜绝目录穿越）；前端 `Markdown` 组件新增 `assetBase` 参数，把相对 `src` 改写为资产路由，绝对地址（协议/`//`/`/` 开头）原样保留。
- **理由**：内容资产归属 content 目录，避免二进制在 `web/` 与 `content/` 出现双份；Vercel 部署下 `/api/*` 已路由到容器服务（D-08），图片随 API 一并可达，无需额外配置；本地单端口同样直接可用。
- **后果**：`ContentValidationTest` 增加「题面引用的相对图片必须存在且不越界」校验；资产路由只支持单段文件名（不支持子目录），Content-Type 由 Ktor 按扩展名推断。

## D-10 答案类型：统一为字符串（2026-09-30）

- **背景**：281–290 批次中 PE 284 要求以 base-14 小写字母形式给出答案（形如 `5a411d7b`），而 `ProblemMeta.answer` / `RunResult.answer` 此前是 `Long`，无法表示非十进制答案。（013 的十位数字曾以 JSON 字符串存放，被 lenient 解析容忍，但数值型字段本身表达不了字母。）
- **选项**：给 284 加一个字符串旁路字段 / 把 answer 全面改为字符串 / 跳过该题。
- **决定**：全面改为字符串。`ProblemMeta.answer: String`（解析侧 `Json { isLenient = true }` 兼容存量 meta.json 的 JSON 数字）；`RunResult.answer` / `expected: String`；引擎注册表保持 `solvers: Map<Int, () -> Long>`（数值题统一输出十进制字符串），新增 `stringSolvers: Map<Int, () -> String>` 承载非数值答案题（284 为首个）。
- **理由**：改动面小（服务端 4 个文件 + 前端类型 + 测试断言 + 文档示例），一次性解决未来同类题；避免把 284 的答案编码成误导性的「base-14 数值」。
- **后果**：`POST /api/problems/{id}/run` 响应中 answer/expected 由 JSON 数字变为字符串（前端仅展示，无逻辑差异）；`ContentValidationTest` 的注册表检查改用 `RunEngine.hasSolver`；`Solved` 判定仍为字符串相等比较。

## D-11 预抓取与「待解析」状态（2026-09-30）

- **背景**：题库已按 25 题/批做到 300 题，但每批解题的第一步都是经 WebBridge 逐题抓取 PE 题面（每题间隔 3.5s，依赖用户本机 Chrome 登录态、易触发 403）——纯机械劳动却卡在解题流水线里。用户决定"先扒题、后解题"，把抓题独立成批。PE 现有 1001 题，本批预抓取 301–400 作为跑道（抓翻同批，25 题/提交）。
- **选项**：A. 维持边解题边抓题；B. 预抓取入库但不上站（仅作解题素材）；C. 预抓取 + 站点以「待解析」状态展示（仅题面，无解析/代码/答案）。
- **决定**：C。
  - `meta.json` 增加 `status` 字段：`"solved"`（缺省，存量 meta.json 不带此字段）/ `"draft"`；`answer` 改为可空，draft 必须为 null；
  - draft 题目仅有 `meta.json` + `statement.md`（中文题面）+ `statement.en.md`（英文底稿）+ 题面图片；不得有解析/代码/答案/耗时基线，不得注册求解器；
  - 站点列表加「待解析」虚线徽标，详情页只渲染题面与题目信息并注明"解析制作中"；
  - 解题批次不再抓题：从已抓底稿出发，补 `analysis.md`/`solution.kt`/答案与基线，翻 status 回 `solved` 并注册求解器；
  - 机械抓取由 `scripts/fetch-pe.mjs` 承担（断点续抓、3.5s 限速、图片经页面内 fetch 落盘），中文翻译仍由 Agent 在批内完成。
- **理由**：
  - 抓取是流水线里唯一依赖外部登录态与限流节奏的步骤，独立成批后解题批次完全不碰官网；
  - draft 只公开中文题面，英文底稿不进入任何渲染路径（Q-1 红线不变）；未解题没有答案，不存在泄题；
  - 列表/搜索/难度筛选对 draft 照常工作，读者能直观看题库进度。
- **后果**：
  - `ContentValidationTest` 按状态分流校验（draft 不要求解析/代码/答案，且不得注册求解器）；
  - 站点会长时期存在"待解析"过渡状态，这是预抓取策略的可见代价；
  - 预抓取范围是可扩展的：想继续抓 401+ 时重复同一批次流程即可。

## 待决策（Open Questions）

| # | 问题 | 阻塞于 |
|---|------|--------|
| Q-1 | ~~题面原文的获取与展示方式~~ **已解决（2026-09-12）**：通过 Kimi WebBridge 操作用户已登录的 Chrome 抓取 PE 官网完整题面；题面不公开渲染在页面上，仅作内容资产的编写参考，前端展示以中文意译 + 原文链接为主。**（2026-09-13 变更：站点由「不公开部署」改为公网公开，见 D-08 与下方纪要）** | — |
| Q-2 | 可视化协议 schema 的最终形态 | Phase 3 动手前用 2 道题做 spike |
| Q-3 | ~~部署目标（本机 / NAS / VPS / Docker）~~ **已解决（Phase 4）**：本机单进程单端口，installDist 形态，见 D-07；Docker 待 NAS/VPS 需求出现时再评估 | — |

### Q-1 解决纪要

- **获取方式**：PE 题目页需登录后可见完整内容；用户已在 Chrome 登录 PE 账号，通过 Kimi WebBridge 插件复用该浏览器会话抓取题面，无需维护 PE 凭据。
- **使用边界**：抓取的英文原文仅作为 `content/problems/000X/statement.md` 的编写底稿（翻译与解析参考），不作为前端公开渲染内容对外展示；前端展示采用中文意译 + 指向 PE 官网的原文链接，规避 PE 的转载条款风险。
- **注意**：WebBridge 依赖用户本机 Chrome 的登录态，登录过期后需用户重新登录再抓取；批量抓取时控制节奏，避免高频请求。
- **公开部署决策变更（2026-09-13）**：用户明确决定站点公开部署到 Vercel（D-08），推翻初版「不公开部署」约束。前端渲染路径依旧只取 `statement.md`（中文意译），`statement.en.md` 不进入任何页面、API 与构建产物；但该底稿仍随 GitHub 仓库公开，若需完全避免英文原文的公开副本，应把仓库转为私有或把 `statement.en.md` 移出版本库。
