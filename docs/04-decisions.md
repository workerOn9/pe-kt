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
