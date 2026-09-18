# 题目内容资产（content/）

每道题一个目录，目录名为四位题号（`0001/`）。结构见 [docs/02-architecture.md](../docs/02-architecture.md)。

## 文件说明

| 文件 | 用途 | 是否公开渲染 |
|------|------|------|
| `meta.json` | 题号、标题、难度、标签、答案、耗时基线 | 是 |
| `statement.md` | 题面中文意译 | 是 |
| `statement.en.md` | 英文原文抓取底稿（WebBridge 抓取，仅供翻译/解析参考） | **否**（见决策 Q-1） |
| `analysis.md` | 解析：思路推导、复杂度、暴力 vs 优化对比 | 是 |
| `solution.kt` | 参考实现（可独立运行） | 是 |
| `brute-force.kt` | （可选）暴力解 | 是 |
| `applications.md` | （可选）现实应用板块，150–350 字，文件内不带标题 | 是 |
| `visualize.json` | （可选）可视化配置 + 分步数据 | 是 |
| `<resource>.*` | （可选）题目资源文件（poker.txt / matrix.txt / sudoku.txt 等） | 否（供求解器读取） |

## 抓取记录

- 2026-09-12：通过 Kimi WebBridge（用户已登录 Chrome）抓取 001–003 题面原文。批量抓取时控制节奏（每次请求间隔 ≥1s），登录态过期需重新登录后再抓。
- 2026-09-12：同法抓取 051–100 题面原文与官方难度/解题人数，并按题下载 9 个资源文件
  （poker/cipher/triangle/keylog/matrix/roman/sudoku/words/base_exp）。批量抓取用 `navigate` + 读 DOM，
  每题间隔 3.5s；**不要用页面内 `fetch()` 批量拉取**，会被 PE 反爬整体 403。
- 2026-09-17：同法抓取 101–125 题面原文与官方难度/解题人数，并按题下载 3 个资源文件
  （triangles.txt / sets.txt / network.txt）。批量抓取仍用 `navigate` + 读 DOM，每题间隔 3.5s。
- 2026-09-18：同法抓取 126–150 题面原文与官方难度/解题人数（这 25 题不需要额外资源文件）。
  批量抓取仍用 `navigate` + 读 DOM，每题间隔 3.5s；官方难度与解题人数取自含 `Published on`
  的 `.tooltiptext_right` 节点（`innerText` 取不到，须用 `textContent`）。
