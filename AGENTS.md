# AGENT.md — pe-kt 续作规范（给后续会话的 Agent）

你是 pe-kt 项目的续作 Agent。本项目是 Kotlin 全栈的 Project Euler 学习展示平台，
当前已完成 1–300 题，301–400 已预抓取入库（状态 draft，待解析——见决策 D-11）。
**2026-09-13 起主仓库是 GitHub 的 `workerOn9/pe-kt`**（本地路径
`~/Documents/github/pe-kt`），由旧仓库搬迁而来且**不带 git 历史**——`m0-foundation` …
`m6-problems-51-100` 这批里程碑 tag 只存在于旧仓库 `~/Documents/pe/pe-kt`，查历史去那边；
101–300 这批直接提交在主仓库的分支上，没有对应 tag。
你的任务通常是**按既有标准继续扩充题库（301 题起）**，或在此基础上的维护工作。

先读 `docs/01-vision.md`、`docs/02-architecture.md`、`docs/04-decisions.md` 了解全局，
本文件是**作业层面的硬约束与工艺流程**。任何与本文件冲突的"想当然"，以本文件 + 仓库现状为准。
**涉及前端 UI 改动前，必须先读 `DESIGN.md` 并遵守其中的设计系统（令牌/组件约定/反模式）。**

## 绝对红线（不可协商）

1. **英文题面不进入任何渲染路径**（D/Q-1 + D-08）：PE 题面有版权且需登录可见。
   `statement.en.md` 只作写作参考留在仓库；前端展示中文意译（`statement.md`）+ 原文链接。
   不要把英文原文渲染进页面、不要加进 API 响应或构建产物。站点已按用户决定公网部署到
   Vercel（D-08），公开暴露的只是中文意译；要彻底消除英文底稿的公开副本，需把仓库转为
   私有或把 `statement.en.md` 移出版本库。
2. **答案必须实跑验证**：每题的 `meta.json.answer` 必须来自求解器真实运行输出，
   禁止从网页/记忆抄答案填进去；预抓取的 draft 题**不得**带 `answer`（答案只能出现在已解题）。
   **不要向 PE 官网提交答案**（也不要预填答案表单）：
   官网提交由用户自己完成——用户会自己慢慢看题、自己输入，出问题再来找我们修复。
   我们的验证到「本机实跑 + 双方法互证 + 公开官方答案表对照」为止。
3. **解析不许注水**：每题 `analysis.md` 保持「思路推导（含数学公式）→ 复杂度对比 → 实测耗时」结构，
   参考 `content/problems/0025/analysis.md` 的密度。写不出有信息量的解析就先不要做这道题。
4. **危险/不可逆操作先问用户**（删文件、改 git 历史、`docker rm` 以外的清理等）。

## 两段式生产流水线（D-11）

题库工作分两类，**每批 25 题（或用户指定范围）一个提交**；动手前先查分支：

> **动手前第一件事：`git branch --show-current`。在 `main` 上就先切出新分支再干活**——
> `main` 已设为受保护分支，禁止直接 push，所有改动必须走 PR（详见「提交规范」）。

### A. 预抓取批次（扒题，不解题）

把机械抓取与翻译从解题流水线里剥离，一次建一段跑道；以后解题批次不再碰官网。

- **A1 机械抓取**：已完成 1–1001 全量抓取入库。抓取脚本经 Kimi WebBridge
  （daemon `http://127.0.0.1:10086/command`，session `pe-fetch-problems`）逐题 navigate + 读 DOM，
  每题间隔 3.5s，写入 `statement.en.md`、草稿 `meta.json`（`status: "draft"`、`titleZh` 留空）
  与题面图片；历史抓取逻辑见 git 记录。若脚本报"页面无题面内容"= 未登录/403/题号
  不存在，停下让用户检查登录态，不要编造。**禁止用页面内 `fetch()` 批量拉题**（会触发 403）。
- **A2 中文翻译**：逐题产出 `statement.md`（格式照既有题目：`# N · 标题` + 中文意译来源说明 +
  正文，公式用 KaTeX）与 `meta.titleZh`；图片引用用脚本落盘的本地文件名。英文原文不得进入
  `statement.md`（绝对红线 1）。
- **A3 自查**：多行 `$$` 两端独占一行（ContentValidationTest 会把关）；`cd web && npm run check:math`。
- **A4 校验与提交**：`./gradlew :server:test`（draft 走放宽分支：只要求 meta + 中英题面，
  禁止解析/代码/答案/求解器注册）。提交信息例：`feat: 预抓取 301–325——……（待解析）`。

### B. 解题批次（从既有 draft 底稿出发，不再抓题）

以题号 N 为例（目录统一 4 位数字：`content/problems/0301/`）：

1. **读底稿**：`statement.en.md`（编写参考）、`statement.md`（题面）与 `meta.json` 的
   官方难度/解题人数。
2. **解题 + `solution.kt`**：可独立运行的 Kotlin 文件，头部注释块格式固定
   （题号/标题/思路/复杂度/`kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar`）。
   优先复用 `dev.pekt.math` 工具库（素数筛、gcd/lcm、组合数、模运算、BigInteger 扩展等，见
   `server/src/main/kotlin/dev/pekt/math/`）。**数位数禁止用 toString() 数**，用阈值比较（见 025 的教训）。
   需要暴力对照时另写 `brute-force.kt`（同时是耗时对比图的数据来源）。
3. **独立验证**：用 kotlinc 单独编译运行 `solution.kt`，输出必须与预期一致。
   预期来源优先级：题面样例外推 > 数学双方法互证（如 025 的通项公式旁证）> 公开官方
   答案表对照（作为旁证；meta.answer 仍必须是本机实跑输出）。官网提交确认留给用户，不作为
   我们的验证手段。
4. **`analysis.md`**：中文解析，结构 = 思路推导 → （有旁证给旁证）→ 答案加粗 →
   「复杂度对比」表（含 JIT 预热后实测毫秒数）→ 关键教训。数学用 KaTeX。
5. **`applications.md`**（现实应用板块，可选但默认要写）：中文 150–350 字，
   **文件内不带标题**（前端自渲染 `<h2>现实应用</h2>`），讲题目涉及的数学结构/算法技术在
   现实工程与科学中的真实应用；技术迁移是合理写法，禁止把题目本身吹嘘成有直接工业用途、
   禁止编造具体软件或研究。参考 `content/problems/0015/applications.md` 的写法与密度。
   实在没有可写内容的题才省略该文件（前端自动不渲染）。
6. **`meta.json`**：补 `answer`/`tags`(kebab-case)/`bruteForceBaselineMs`/`optimizedBaselineMs`
   （两个 baseline 是**本机 JIT 预热后**实测毫秒数，与 analysis.md 表格数据一致），并把
   `status` 删除（缺省即 solved）；`titleZh` 若预抓取批次已写就则校对正确性。
7. **注册求解器**：在 `server/src/main/kotlin/dev/pekt/engine/Solvers.kt` 的 `solvers` map
   加一行 `301 to ::solve301,` 并实现 `private fun solve301(): Long`，
   逻辑与 `content/problems/0301/solution.kt` 保持一致（通用步骤换用 math 工具库）。
   注意返回值是 `Long`，确实超 Long 的题（罕见）要先停下来和用户讨论方案。
   **draft 题不得注册求解器**（ContentValidationTest 会拦）。

## 验收门禁（每批做完必须全绿才算完）

```bash
./gradlew :server:test        # ContentValidationTest 逐题校验 meta/题面/解析/代码并实跑比对答案（draft 题走放宽分支）
cd web && npm run check:math  # 全库公式过一遍 KaTeX，报错即红字原文（已挂在 npm run build 前面）
cd web && npm run build       # 前端构建
./gradlew :server:installDist # 打 Docker 镜像前必须重跑！jar 过期会导致静态资源 404/白屏（D-07 教训）
docker build -t pe-kt . && docker rm -f pe-kt && docker run -d --name pe-kt -p 8080:8080 pe-kt
curl -s localhost:8080/health # {"status":"ok"}
curl -s localhost:8080/api/problems | python3 -c "import json,sys;print(len(json.load(sys.stdin)))"
```

**预抓取批次（A）**只动 content：最低跑 `:server:test` + `npm run check:math` + `npm run build`；
分支收尾（发 PR 前）再跑完整门禁（installDist + docker + 浏览器实测）。
**解题批次（B）**与代码/部署改动执行上面全量。

浏览器实测（WebBridge，同 session）：navigate `http://localhost:8080/`，
screenshot + evaluate 检查新题出现在列表、详情页解析渲染、公式无裸 `$` 残留。

动了部署配置（`vercel.json` / `Dockerfile.vercel` / `Application.kt` 的端口解析）时另加一道：

```bash
docker build -f Dockerfile.vercel -t pe-kt-vercel .   # Vercel 上没有本地产物可 COPY，容器必须能自己从源码构建
docker run --rm -e PORT=8080 -p 8080:8080 pe-kt-vercel
curl -s localhost:8080/health
```

## 提交规范

- **先查分支，再开工（硬性）**：每批工作开始前执行 `git branch --show-current`；若在 `main` 上，
  **先切出新分支再动手**。`main` 已设为受保护分支（GitHub 规则），禁止直接 push，改动一律走 PR。
  分支命名：题库存量扩充 `feat/problems-XXX-YYY`（如 `feat/problems-251-260`），
  文档/维护 `docs/...`、修复 `fix/...`。
- **PR 流程**：分支完成后 push（`git push -u origin <branch>`）并用 `gh pr create` 开 PR
  （中文标题与描述，正文写清本批内容与验收结果）；**由用户 review 并合并，Agent 不自行合并、不直推 main**。
- 中文提交信息，风格照 git log：`feat: 题库扩充 026–050——……`。
- 每批（25 题或用户指定范围）一个提交。
- 影响面大的改动（部署、构建、依赖源）同样走功能分支（如 `feat/vercel-deploy`），确认无误后经 PR 合回 `main`。
- 工作区保持干净，构建产物（`build/`、`web/dist/`、`node_modules/`）不提交。

## 可视化动画（可选，不凑数）

协议见 `docs/06-visualization-protocol.md`，实现在 `server/.../visualize/`。
只为**视觉上有表现力**的题做（v1 是 25 题选 5）；做完设 `hasVisualization: true`。
判断不准就跳过，动画不是每题义务。

## 环境备忘

- 技术栈：Kotlin 2.1.21 / Ktor 3.1.3 / Gradle 8.10 wrapper / React 19 + Vite。
- **依赖源必须是官方默认**（docs/05）：Gradle 用 `gradlePluginPortal()` + `mavenCentral()`、wrapper 指向
  `services.gradle.org`，`web/` 不放 `.npmrc`，`package-lock.json` 的 `resolved` 全指向 `registry.npmjs.org`。
  本机加速靠机器级配置 `~/.gradle/init.gradle.kts` 与 `~/.npmrc`（不在仓库里）；**别把国内镜像地址提交进仓库**，
  Vercel 构建机在海外会拉不到。
- 部署（本机 / NAS）：单端口 8080，Ktor 同托管 API + `web/dist`（`PEKT_WEB_DIST` 指定，SPA fallback）。
  Docker 见根目录 `Dockerfile`（基座 `eclipse-temurin:21-jre`，Docker Hub 不通时走
  `docker.1ms.run/library/` 镜像源拉取后 `docker tag` 回原名）。
- 部署（Vercel，D-08）：双服务，配置在 `vercel.json`——`web` 按 Vite 预设静态托管走 CDN，
  `server` 是容器服务（`Dockerfile.vercel`）只接 `/api/*` 与 `/health`。容器内从源码构建，
  监听端口读 `PORT` 环境变量（`Dockerfile.vercel` 声明 `PORT=80`，对齐 Vercel 容器默认流量端口）。
- 前端约束：`--content-width: min(80vw, 1600px)`；列表触底自动加载（20/屏）；表头 sticky。
  改前端后本机要重跑 `npm run build` + 重建镜像才在 8080 生效；Vercel 上则是 push 后自动重建。
- PE 账号：用户 Chrome 已登录 PE 官网（用户名不记录进仓库），所有官网交互通过 WebBridge，不要另存凭据。
  **只用于抓题面/题目配图等读取操作**；不要提交答案、不要预填答案表单、不要代为点击 Check。
- **本机没有 `kotlinc`**：独立编译验证 `solution.kt` 要自己搭 shim——用 Gradle 缓存里的
  `kotlin-compiler-embeddable-2.1.21.jar` + `kotlin-stdlib-2.1.21.jar` + `kotlinx-coroutines-core-jvm`
  当 classpath 跑 `org.jetbrains.kotlin.cli.jvm.K2JVMCompiler`，编译时加 `-no-stdlib -classpath <stdlib>`
  输出到目录，再 `java -cp <outdir>:<stdlib> <文件名>Kt` 运行。**不要用 `-include-runtime`**（找不到
  kotlin-home 会报 `Couldn't find kotlin-stdlib`）。
  编译器自己的 classpath 还缺两样东西，不加会分别报错，记得一起带上：
  `trove4j-*.jar`（`org.jetbrains.intellij.deps/trove4j`，否则 `NoClassDefFoundError: gnu/trove/TObjectHashingStrategy`）
  与 `annotations-*.jar`（`org.jetbrains/annotations`，否则 codegen 阶段 `NoClassDefFoundError: org/jetbrains/annotations/NotNull`）。
  另注意 **JVM facade 类名会把文件名里的 `-` mangle 成 `_`**：`brute-force.kt` 生成的是 `Brute_forceKt` 而不是
  `Brute-forceKt`，用 `java -cp` 启动时类名要写对。编译阶段给 JVM `-Xmx768m` 就够，不需要更大的堆。

## 已知坑（踩过的，别再踩）

- **installDist 产物过期**：改 server 源码后不重跑 `./gradlew :server:installDist` 就打镜像，
  容器里跑的是旧 jar（症状：静态资源 200 但 Content-Type=text/html，页面白屏）。D-07 有记录。
- **Ktor 3.1.3 根路径 staticFiles 缺陷**：静态托管是手写 `get("/{path...}")` 通配路由，别"优化"回 staticFiles。
- **HEAD 请求不走 get 路由**：验证静态资源用 `curl -s -o /dev/null -w ...`（GET），别用 `curl -I`。
- **`.dockerignore` 的 `*.md` 会误伤 content/ 里的 markdown**：排除规则要用 `/docs`、`/README.md` 这种根锚定写法。
- **多行显示公式的 `$$` 必须两端独占一行**：`remark-math` 沿用了 micromark 的代码围栏语义——
  `$$` 后面在同一行还跟着内容、而收尾的 `$$` 又在后面某一行时，首行内容会被当成「围栏信息串」丢掉，
  公式块还会一路吞到下一个独占一行的 `$$`，于是整页后半段渲染成一片红字原文（0065 等 7 题踩过，
  共 8 处）。多行公式一律写成

  ```
  $$
  \begin{aligned}
  a &= b
  \end{aligned}
  $$
  ```

  单行 `$$x$$` 可以放心写：`web/src/lib/displayMath.ts` 的 `normalizeDisplayMath` 会在解析前把这两种
  写法统一规范成竖排围栏，交给 KaTeX 的都是 display 模式；规范写法由 `ContentValidationTest` 的围栏校验守住。
- **公式写完跑 `npm run check:math`**（在 `web/` 下，已挂在 `npm run build` 前面）：把题库与基准报告的
  每一条公式按前端同一条管线过一遍 KaTeX，报错就非零退出。KaTeX 层面的错字——`\*` 应写 `^{*}`、命令拼错、
  环境名笔误——只会把 LaTeX 原文渲染成红字贴在页面上，浏览器不点进去看不出来，只能靠这道校验拦。
  多行对齐公式用 `aligned`（`align` 会按行自动编号，编号贴在内容列最右侧，很难看）。
- **PE 官网会整体 403**：在 `evaluate` 里用 `fetch()` 批量拉题会被反爬判定，之后该 IP 上整个站
  （包括用户已登录的会话）都返回 `403 Request forbidden by administrative rules`。批量抓题必须用
  `navigate` + 读 DOM，每题间隔 3–4 秒；真被 403 了等 1–2 分钟即可自行恢复，别连续重试加重判定。
- **Vercel 容器镜像仓库（VCR）有数量上限**：Hobby 计划每个仓库（pe-kt 的 `server`）最多 50 个镜像，
  每次部署（含 PR 分支的 preview）都会推入一个以提交 SHA 为 tag 的镜像，累积到上限后部署报
  `denied: repository has reached the maximum allowed number of images`，**应用照常跑但新内容上不去**。
  清理（本机执行；未登录先 `npx vercel@latest login` 走设备码在浏览器里确认，`vercel whoami` 查当前账号）：
  `vercel vcr image ls server --project pe-kt --scope <账号 scope> --format json` 列出镜像，
  `vercel vcr image rm server <image-id> --yes --project pe-kt --scope <账号 scope>` 逐个删除
  （scope 为 Vercel 账号/团队标识，按本机登录账号自行填写，不写进仓库）。
  **必须保留当前生产部署对应提交 SHA 的镜像**（删掉会让生产冷启动拉不到镜像），其余旧镜像可删；
  建议每次批次合并后顺手把水位清到 ≤ 10，不够用后在 Vercel 面板点 Redeploy 触发重建。
- **275 的引擎实现很重（已知性能特征）**：本地 8 核约 4 s，但 Vercel 函数容器约 1 vCPU，实测约 33 s；
  结果正确、请求能返回（枚举是纯 CPU 无挂起，10 s 熔断不会打断它），别误以为是 bug；
  若未来要收敛，需要换 transfer-matrix / 剖分 DP 级算法，不是常数优化能解决的。
- **PE 题面的 DOM 位置**：正文在 `.problem_content`；MathJax 的 LaTeX 原文在
  `mjx-container [data-mml-node="math"]` 的 `data-latex` 属性上（`display="true"` 表示独立公式，其余为行内），
  把 `mjx-container` 节点替换成 `$...$` / `$$...$$` 文本再取 `innerText` 即可还原题面。
  官方难度与解题人数在含 `Published on` 的那个 `.tooltiptext_right` 里，形如 `Difficulty: Level 3 [10%]`、
  `and solved by 104574`（tooltip 内容 `innerText` 取不到，要用 `textContent`/`innerHTML`）。
