# pe-kt

Kotlin 全栈的 Project Euler 学习与展示平台：每道数学题配深度解析、可运行的 Kotlin 参考实现，以及计算过程可视化。

## 快速开始

```bash
./gradlew :server:run        # 启动 Ktor 服务（端口 8080）
curl localhost:8080/health   # 健康检查，应返回 {"status":"ok"}
./gradlew :server:test       # 运行测试（含内容资产校验）

# 单端口部署形态（D-07）：前端构建产物由 Ktor 直接托管
cd web && npm run build && cd ..
./gradlew :server:run        # 打开 http://localhost:8080/ 即是完整站点

# 开发模式（前端热更新走 Vite）
cd web && npm run dev        # 前端 :7100，代理 /api 到 :8080

# Docker 一体化镜像（先构建本地产物，再打镜像；容器内不拉依赖）
cd web && npm run build && cd ..
./gradlew :server:installDist   # 每次打镜像前必须重跑，保证 jar 与源码同步
docker build -t pe-kt . && docker run -d --name pe-kt -p 8080:8080 pe-kt
# 管理：docker stop pe-kt / docker start pe-kt / docker rm -f pe-kt
```

首次构建从官方源下载 Gradle 8.10 与全部依赖；本机在国内网络下由 `~/.gradle/init.gradle.kts` 与
`~/.npmrc` 的镜像配置自动加速，仓库内不声明任何国内镜像（见 [docs/05-build-toolchain.md](docs/05-build-toolchain.md)）。

## 公网部署（Vercel）

Vercel 项目导入本仓库即可，构建与路由由 [vercel.json](vercel.json) 声明（D-08）：两个服务，
`web`（Vite 静态产物走 CDN）与 `server`（容器服务，只承载 `/api/*` 与 `/health`）。

本地想先验证容器能否在无本地产物的情况下自己构建：

```bash
docker build -f Dockerfile.vercel -t pe-kt-vercel .
docker run --rm -e PORT=8080 -p 8080:8080 pe-kt-vercel
curl -s localhost:8080/health
```

两个 Dockerfile 用途不同，不要混用：根目录 `Dockerfile` 是本地/NAS 用的单端口镜像（COPY 本地产物），
`Dockerfile.vercel` 供 Vercel 从其构建机从源码构建。

## 仓库结构

```
pe-kt/
├── docs/                  # 规划与决策文档
├── server/                # Ktor 后端（Gradle 子项目）
├── content/               # 题目内容资产（Markdown + Kotlin 源码，见 content/README.md）
├── web/                   # 前端（Vite 子项目）
├── Dockerfile             # 本地/NAS 单端口镜像（COPY 本地产物）
├── Dockerfile.vercel      # Vercel 容器服务镜像（容器内从源码构建）
└── vercel.json            # Vercel 双服务与路由声明
```

## 提交规范与分支策略

### 提交信息

格式：`<type>: <主题>——<补充说明>`（主题用中文，一行说清"做了什么"）。

| type | 用途 |
|------|------|
| `docs` | 规划/决策/流程文档 |
| `content` | 题目内容资产（题面、解析、meta） |
| `feat` | 新功能（端点、页面、工具库函数） |
| `fix` | 缺陷修复 |
| `test` | 测试相关 |
| `build` | 构建与依赖配置 |
| `refactor` | 不改变行为的重构 |

规则：一次提交只做一件事；题面原文抓取底稿（`statement.en.md`）与解析/实现分开提交；通过验证（构建 + 测试）后才提交。

### 分支策略

- `main`：始终可构建、可演示，直接提交仅用于 docs/content 等小步快跑内容
- 功能分支：`feat/<主题>`（如 `feat/run-endpoint`），完成后合回 `main`
- 每阶段出口标准达成后在 `main` 打 tag：`m0-foundation`、`m1-mvp`……

## 文档

| 文档 | 内容 |
|------|------|
| [docs/01-vision.md](docs/01-vision.md) | 项目愿景与范围 |
| [docs/02-architecture.md](docs/02-architecture.md) | 技术架构与 API 设计 |
| [docs/03-roadmap.md](docs/03-roadmap.md) | 分阶段路线图 |
| [docs/04-decisions.md](docs/04-decisions.md) | 技术决策记录（ADR） |
| [docs/05-build-toolchain.md](docs/05-build-toolchain.md) | 构建与依赖源配置（官方源 + 本机镜像） |

## CI

push / PR 到 `main` 时 GitHub Actions 自动执行（配置见 [.github/workflows/ci.yml](.github/workflows/ci.yml)）：

- **后端测试**：JDK 21（temurin）上运行 `./gradlew :server:test`，包含内容资产校验（ContentValidationTest 逐题检查 meta/题面/解析/参考实现，并实跑引擎比对答案）
- **前端构建**：Node 22 上 `cd web && npm ci && npm run build`

## 当前状态

**题库已扩充到 100 题**（M0–M6）：100 道题完整内容 + math/ 数论工具库 + 5 题可视化动画 + 搜索筛选/耗时对比图 + CI + 单端口部署（D-07）+ Vercel 双服务公网部署（D-08）。另有一份内容资产驱动的报告页 `/benchmark`（主流编程语言性能对比，六种语言在同一批题目上的本机实测 + 图表 + 结论），入口在题库列表页的「延伸阅读」。`npm run build` 后 `./gradlew :server:run`，浏览器打开 <http://localhost:8080/> 即是完整站点。
