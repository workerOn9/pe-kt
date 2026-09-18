# AGENT.md — pe-kt 续作规范（给后续会话的 Agent）

你是 pe-kt 项目的续作 Agent。本项目是 Kotlin 全栈的 Project Euler 学习展示平台，
当前已完成 1–150 题。**2026-09-13 起主仓库是 GitHub 的 `workerOn9/pe-kt`**（本地路径
`~/Documents/github/pe-kt`），由旧仓库搬迁而来且**不带 git 历史**——`m0-foundation` …
`m6-problems-51-100` 这批里程碑 tag 只存在于旧仓库 `~/Documents/pe/pe-kt`，查历史去那边；
101–150 这批直接提交在主仓库的分支上，没有对应 tag。
你的任务通常是**按既有标准继续扩充题库（151 题起）**，或在此基础上的维护工作。

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
   禁止从网页/记忆抄答案填进去。条件允许时用用户已登录的 Chrome 向 PE 官网提交做最终确认。
3. **解析不许注水**：每题 `analysis.md` 保持「思路推导（含数学公式）→ 复杂度对比 → 实测耗时」结构，
   参考 `content/problems/0025/analysis.md` 的密度。写不出有信息量的解析就先不要做这道题。
4. **危险/不可逆操作先问用户**（删文件、改 git 历史、`docker rm` 以外的清理等）。

## 单题生产流水线（每题 9 步，缺一不可）

以题号 N=26 为例（目录统一 4 位数字：`content/problems/0026/`）：

1. **抓题面**：用户 Chrome 已登录 PE（经 Kimi WebBridge 插件，daemon 在
   `http://127.0.0.1:10086/command`，沿用 session `pe-fetch-problems`）。
   navigate 到 `https://projecteuler.net/problem=26`，evaluate 抽取题面正文、
   官方难度百分比、解题人数（solvedBy）。抓不到就停下让用户检查登录态，不要编造。
2. **`statement.en.md`**：题面原文存档（仅参考用）。
3. **`statement.md`**：中文意译题面，忠实但不必逐字；KaTeX 公式（`$...$` / `$$...$$`）。
4. **解题 + `solution.kt`**：可独立运行的 Kotlin 文件，头部注释块格式固定
   （题号/标题/思路/复杂度/`kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar`）。
   优先复用 `dev.pekt.math` 工具库（素数筛、gcd/lcm、组合数、模运算、BigInteger 扩展等，见
   `server/src/main/kotlin/dev/pekt/math/`）。**数位数禁止用 toString() 数**，用阈值比较（见 025 的教训）。
   需要暴力对照时另写 `brute-force.kt`（同时是耗时对比图的数据来源）。
5. **独立验证**：用 kotlinc 单独编译运行 `solution.kt`，输出必须与预期一致。
   预期来源优先级：PE 官网提交确认 > 题面样例外推 > 数学双方法互证（如 025 的通项公式旁证）。
6. **`analysis.md`**：中文解析，结构 = 思路推导 → （有旁证给旁证）→ 答案加粗 →
   「复杂度对比」表（含 JIT 预热后实测毫秒数）→ 关键教训。数学用 KaTeX。
7. **`applications.md`**（现实应用板块，可选但默认要写）：中文 150–350 字，
   **文件内不带标题**（前端自渲染 `<h2>现实应用</h2>`），讲题目涉及的数学结构/算法技术在
   现实工程与科学中的真实应用；技术迁移是合理写法，禁止把题目本身吹嘘成有直接工业用途、
   禁止编造具体软件或研究。参考 `content/problems/0015/applications.md` 的写法与密度。
   实在没有可写内容的题才省略该文件（前端自动不渲染）。
8. **`meta.json`**：字段格式照抄已有题目（看 `content/problems/0025/meta.json`）：
   `id/title/titleZh/difficulty(官方百分比数字)/difficultyLevel/tags(kebab-case)/answer/
   solvedBy/bruteForceBaselineMs/optimizedBaselineMs/hasVisualization/sourceUrl/fetchedAt`。
   两个 baseline 是**本机 JIT 预热后**的实测毫秒数（与 analysis.md 表格数据一致）。
9. **注册求解器**：在 `server/src/main/kotlin/dev/pekt/engine/Solvers.kt` 的 `solvers` map
   加一行 `26 to ::solve026,` 并实现 `private fun solve026(): Long`，
   逻辑与 `content/problems/0026/solution.kt` 保持一致（通用步骤换用 math 工具库）。
   注意返回值是 `Long`，确实超 Long 的题（罕见）要先停下来和用户讨论方案。

## 验收门禁（每批做完必须全绿才算完）

```bash
./gradlew :server:test        # ContentValidationTest 逐题校验 meta/题面/解析/代码并实跑比对答案
cd web && npm run check:math  # 全库公式过一遍 KaTeX，报错即红字原文（已挂在 npm run build 前面）
cd web && npm run build       # 前端构建
./gradlew :server:installDist # 打 Docker 镜像前必须重跑！jar 过期会导致静态资源 404/白屏（D-07 教训）
docker build -t pe-kt . && docker rm -f pe-kt && docker run -d --name pe-kt -p 8080:8080 pe-kt
curl -s localhost:8080/health # {"status":"ok"}
curl -s localhost:8080/api/problems | python3 -c "import json,sys;print(len(json.load(sys.stdin)))"
```

浏览器实测（WebBridge，同 session）：navigate `http://localhost:8080/`，
screenshot + evaluate 检查新题出现在列表、详情页解析渲染、公式无裸 `$` 残留。

动了部署配置（`vercel.json` / `Dockerfile.vercel` / `Application.kt` 的端口解析）时另加一道：

```bash
docker build -f Dockerfile.vercel -t pe-kt-vercel .   # Vercel 上没有本地产物可 COPY，容器必须能自己从源码构建
docker run --rm -e PORT=8080 -p 8080:8080 pe-kt-vercel
curl -s localhost:8080/health
```

## 提交规范

- 中文提交信息，风格照 git log：`feat: 题库扩充 026–050——……`。
- 每批（25 题或用户指定范围）一个提交。
- 影响面大的改动（部署、构建、依赖源）走功能分支（如 `feat/vercel-deploy`），确认无误后再合回 `main`。
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
- PE 账号：用户 Chrome 已登录（howardch1993），所有官网交互通过 WebBridge，不要另存凭据。
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
- **PE 题面的 DOM 位置**：正文在 `.problem_content`；MathJax 的 LaTeX 原文在
  `mjx-container [data-mml-node="math"]` 的 `data-latex` 属性上（`display="true"` 表示独立公式，其余为行内），
  把 `mjx-container` 节点替换成 `$...$` / `$$...$$` 文本再取 `innerText` 即可还原题面。
  官方难度与解题人数在含 `Published on` 的那个 `.tooltiptext_right` 里，形如 `Difficulty: Level 3 [10%]`、
  `and solved by 104574`（tooltip 内容 `innerText` 取不到，要用 `textContent`/`innerHTML`）。
