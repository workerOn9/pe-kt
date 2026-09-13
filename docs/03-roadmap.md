# 路线图（Roadmap）

> 原则：每阶段结束都有一个可演示的产物。先打通"一题端到端"，再横向扩展题目数量。

## Phase 0：地基（当前）

- [ ] Gradle 多模块骨架（`server` / `web` / `content`）
- [ ] Ktor 最小应用：一个健康检查端点
- [ ] 提交规范与分支策略写入 README
- [ ] 前 3 道题的内容资产（001/002/003）作为格式样板

**出口标准**：`./gradlew run` 起服务，`curl localhost:8080/health` 返回 200。

## Phase 1：端到端打通（MVP）

- [x] 内容加载层：扫描 `content/problems/`，构建内存索引
- [x] 题目列表 API + 详情 API
- [x] 前端：题目列表页 + 详情页（Markdown + KaTeX 公式渲染）
- [x] 代码展示：后端返回 `solution.kt` 源码，前端 highlight.js 高亮（Shiki 留待后续按需替换）
- [x] 运行接口 v1：进程内注册表执行（与 solution.kt 逻辑一致），返回答案 + 耗时 + 正确性
- [x] 5 道题完整内容（题面、解析、代码）

**出口标准**：浏览器打开任一题，能看到解析、高亮代码、点"运行"得到正确答案。✅ 已实测达成（2026-09-12）

## Phase 2：数论工具库 + 批量内容

- [x] `math/` 工具库成型：素数筛、GCD/扩展 GCD、快速幂、模逆元、组合数、BigInteger 扩展
- [x] 工具库单元测试覆盖率 ≥ 80%（12 个公开函数，40 个用例，典型/边界/错误路径全覆盖）
- [x] 题目扩展到 25 道，覆盖标签：math / primes / dp / combinatorics / number-theory
- [x] 解法运行改为子进程隔离——v1 进程内方案未暴露问题，按条件**不迁移**（25 题最慢实测 <100ms，远低于 10s 熔断）

**出口标准**：达成 vision 中的 v1 成功标准前 3 项。✅ 已达成（2026-09-12）

## Phase 3：可视化

- [x] 可视化协议设计：分步数据的统一 schema（[docs/06-visualization-protocol.md](06-visualization-protocol.md)，Q-2 已解决）
- [x] 后端：5 道题的可视化数据生成（007 埃氏筛 / 011 网格乘积 / 014 Collatz / 015 路径 DP / 018 三角形 DP）
- [x] 前端：SVG 播放器组件（播放/暂停/步进/速度控制 + 进度滑块 + 键盘操作）
- [x] 可视化与代码行联动：v1 形式为每步携带 `codeLine` 并在播放器下方显示「solution.kt 第 N 行」；源码内嵌行高亮留作后续增强

**出口标准**：至少 5 道题有流畅的计算过程动画。✅ 已达成（2026-09-12，浏览器实测 007/014/018 播放）

## Phase 4：打磨与扩展（暂列）

- [x] 全站搜索（按标签/难度/题号）
- [x] 暴力解 vs 优化解的耗时对比图表（详情页对数刻度 SVG 条形图）
- [x] CI：构建 + 测试 + 内容资产校验（.github/workflows/ci.yml + ContentValidationTest 4 例，含 25 题答案实跑比对）
- [x] 部署形态决策 → D-07：本地单进程单端口（installDist + Ktor 托管 web/dist），Q-3 已解决

**出口标准**：Phase 4 收尾完成（2026-09-12）。v1 全部里程碑达成。

## Phase 5：题库扩充至 100 题

- [x] 026–050 共 25 题内容资产（m5-problems-26-50）
- [x] 051–100 共 50 题内容资产（m6-problems-51-100）：每题 statement（中/英底稿）、solution.kt、
  brute-force.kt、analysis.md、applications.md、meta.json，以及 Solvers.kt 注册的 `solve0NN`
- [x] 每题的独立验证：solution.kt 与 brute-force.kt 双算法实跑一致，答案与公开参考值交叉校验
- [x] 9 道题的外部资源文件入库（poker/cipher/triangle/keylog/matrix/roman/sudoku/words/base_exp）

**出口标准**：`./gradlew :server:test`（含 100 题答案实跑比对）与 `cd web && npm run build` 全绿，Docker 单端口实测通过。

## 里程碑总结

| 里程碑 | 内容 | 验收 |
|--------|------|------|
| M0 | 地基 | health check 通过 |
| M1 | MVP | 一题端到端可演示 |
| M2 | 内容库 | 25 题 + 工具库 |
| M3 | 可视化 | 5 题动画 |
| M4 | 打磨 | 搜索 + 对比图 + CI + 单端口部署 |
| M5 | 题库扩充 | 26–50 题内容 + 求解器全量注册 |
| M6 | 题库扩充 | 51–100 题内容 + 求解器全量注册 |
