# 技术架构（Architecture）

## 总体结构

```
┌─────────────────────────────────────────────────┐
│                  前端 (Frontend)                   │
│  题目列表 / 题目详情 / 代码高亮 / 可视化动画         │
│  Vite + React + TypeScript（详见决策 D-03）        │
└──────────────────┬──────────────────────────────┘
                   │ REST API (JSON)
┌──────────────────┴──────────────────────────────┐
│                后端 (Backend)                     │
│              Ktor Server (JVM)                    │
│  ┌───────────┬───────────┬───────────────────┐   │
│  │ 题目路由   │ 求解引擎   │ 可视化数据生成      │   │
│  │ /problems │ /run      │ /visualize        │   │
│  └───────────┴───────────┴───────────────────┘   │
│  ┌───────────────────────────────────────────┐   │
│  │  内容层：题目 Markdown + Kotlin 源码资产     │   │
│  └───────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

## 技术选型

### 后端

| 组件 | 选型 | 理由 |
|------|------|------|
| 语言 | Kotlin (JVM, 2.x) | 项目核心目的 |
| 框架 | Ktor Server 3.x | 轻量、Kotlin-first、协程原生，远比 Spring Boot 贴合本项目规模 |
| 序列化 | kotlinx.serialization | Kotlin 官方，与数据类无缝 |
| 构建 | Gradle (Kotlin DSL) | Kotlin 生态标准 |
| 测试 | JUnit 5 + kotlinx-coroutines-test | |
| 执行沙箱 | 进程内类加载器隔离（v1）→ 子进程隔离（v2） | 见决策 D-04 |
| 持久化 | 无数据库（v1） | 题目内容是静态资产，JSON 索引文件即可；见决策 D-05 |

### 前端

| 组件 | 选型 |
|------|------|
| 构建 | Vite |
| 框架 | React 19 + TypeScript |
| 路由 | React Router |
| Markdown 渲染 | react-markdown + remark/rehype（数学公式用 KaTeX） |
| 代码高亮 | Shiki 或 highlight.js |
| 可视化 | Canvas API / SVG（自绘，轻量优先，暂不引入图表库） |

### 内容资产格式

每道题一个目录，版本化管理：

```
content/
└── problems/
    └── 0001/
        ├── meta.json        # 题号、标题、难度、标签、答案、耗时基线
        ├── statement.md     # 题面：中文意译为主（公开渲染）；英文原文仅作编写底稿，不直接渲染，附 PE 原文链接
        ├── analysis.md      # 解析：思路推导、复杂度、暴力 vs 优化对比
        ├── solution.kt      # 参考实现（可独立运行）
        ├── brute-force.kt   # （可选）暴力解，用于对比教学
        └── visualize.json   # （可选）可视化配置 + 分步数据
```

`meta.json` 示例：

```json
{
  "id": 1,
  "title": "Multiples of 3 or 5",
  "titleZh": "3 或 5 的倍数",
  "difficulty": 5,
  "tags": ["math", "inclusion-exclusion"],
  "answer": 233168,
  "bruteForceBaselineMs": 5,
  "optimizedBaselineMs": 0.1,
  "hasVisualization": false
}
```

## API 设计草案（v1）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/problems` | 题目列表（分页，含标签/难度筛选） |
| GET | `/api/problems/{id}` | 题目详情（题面 + 解析全文） |
| GET | `/api/problems/{id}/solution` | 参考实现源码（返回源码文本 + 元信息） |
| POST | `/api/problems/{id}/run` | 执行解法，返回答案与耗时 |
| GET | `/api/problems/{id}/visualize` | 可视化分步数据 |
| GET | `/api/benchmarks/{slug}` | 基准报告（Markdown 正文 + 图表数据，内容资产在 `content/benchmarks/`） |

统一错误格式：`{ "error": "code", "message": "..." }`

## 目录结构规划

```
pe-kt/
├── docs/                  # 规划与决策文档
├── server/                # Ktor 后端 (Gradle 子项目)
│   ├── src/main/kotlin/
│   │   ├── Application.kt
│   │   ├── routes/
│   │   ├── engine/        # 求解执行引擎
│   │   ├── content/       # 内容加载与索引
│   │   └── math/          # 可复用数论工具库
│   └── src/test/kotlin/
├── web/                   # 前端 (Vite 子项目)
│   └── src/
│       ├── pages/         # 题目列表 / 题目详情 / 基准报告
│       ├── components/    # CodeBlock / Visualizer / KaTeX / LangChart 等
│       └── api/           # 后端 API 客户端
├── content/               # 内容资产
│   ├── problems/          # 题目内容资产
│   └── benchmarks/        # 基准报告（Markdown 正文 + 同名 .charts.json 图表数据）
└── README.md
```

## 关键设计约束

1. **解法代码必须可独立运行**：`solution.kt` 不依赖 Ktor，只依赖 `math/` 工具库，可用 `kotlinc` 单独编译验证
2. **运行接口有超时与资源限制**：默认 10s 超时、512MB 堆上限（v1 进程内隔离的具体策略见 D-04）
3. **可视化是数据驱动**：前端只做渲染，计算过程的分步数据由后端 Kotlin 代码生成，保证"看到的 = 真实执行的"
