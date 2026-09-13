# DESIGN.md — pe-kt 设计系统

本站是**数学阅读站**，设计原则是「排版优先、内容为王」：视觉服务于长时间阅读数学推导，
不追求花哨。任何 UI 改动必须保持本文件描述的语言一致；本文件与
`web/src/index.css` 冲突时以代码为准（改代码后回来同步这里）。

## 设计语言

- **气质**：学术笔记本 / 讲义，安静、克制、纸感。无渐变、无玻璃拟态、无阴影堆叠、无装饰性动效。
- **唯一强调色**是沉静的深青蓝 `#1a5f7a`，链接、徽标、激活态统一用它，不引入第二个强调色。
- **圆角**：小组件（徽标、标签、chip）用 3–6px；不使用大圆角卡片嵌套。
- **分隔**：靠留白与 1px 细线（`--color-border`），不用投影卡片。

## 设计令牌（`web/src/index.css` `:root`）

| 令牌 | 值 | 用途 |
|------|-----|------|
| `--color-bg` | `#fbfaf7` | 页面底色（暖白纸感） |
| `--color-text` | `#2b2a26` | 正文 |
| `--color-text-muted` | `#6f6c64` | 次级文字、表头、提示 |
| `--color-accent` | `#1a5f7a` | 链接 / 强调 |
| `--color-accent-soft` | `#e8f1f4` | 标签、徽标底色 |
| `--color-border` | `#e3e0d8` | 分隔线、边框 |
| `--color-ok` / `--color-bad` | `#2e7d32` / `#b3261e` | 正确/错误、通过/超时等语义态 |
| `--font-body` | Source Han Serif → Songti → Charter/Georgia serif | 正文与数学解析（衬线） |
| `--font-ui` | -apple-system → PingFang SC → … sans-serif | 界面元素（表格/按钮/徽标） |
| `--font-mono` | SF Mono → JetBrains Mono → Menlo… | 代码 |
| `--content-width` | `min(80vw, 1600px)` | 内容列宽，居中 |

字体规则（硬约束）：

- **中文绝不斜体**——强调用字重/颜色/底色，不用 `font-style: italic`。
- 正文阅读走衬线栈（中西文混排衬线在前、回退齐全）；UI 控件走系统无衬线。
- 正文字号 17px、行高 1.85，这是长文阅读的基准，不要调紧。

## 布局骨架

- `site-header`（顶部分隔线 + 标题）/ `site-main`（`--content-width` 居中）/ `site-footer`。
- 列表页：工具栏（搜索框 + 难度下拉）→ 标签 chip 墙 → 统计行 → 「延伸阅读」文字链 → 表格 → 触底哨兵提示。
- 详情页：面包屑返回 → 吸顶题号栏（第 N 题 + 上一题/下一题）→ 标题区（中文标题 + 英文标题）→
  题面（`statement.md`）→ 解析（`analysis.md`，KaTeX）→ 代码块 → 耗时对比图 →
  （可选）可视化播放器。正文首行的 `# 题目名称` 由 `web/src/lib/contentPreamble.ts` 摘掉
  （页头与 Tab 已表明题号与板块），题面紧跟着的「中文意译」来源说明留在原处、字号压一档。
- 报告页（`/benchmark`）：面包屑返回 → 标题区（标题 + 副标题 + 实测日期/机器徽标）→
  图表与正文交替（正文里的 `[[chart:id]]` 是图表插入点）。

## 组件约定

- **表格**（`.problem-table`）：表头 sticky 吸顶、不透明 `--color-bg` 背景；
  用 `border-collapse: separate; border-spacing: 0`（collapse 与 sticky 不兼容）；
  行hover 高亮，行分隔用 1px 下边框。
- **难度徽标**（`.difficulty-badge`）：12px UI 字体、圆角小胶囊、中性色。
- **标签**（`.tag` / `.filter-chip`）：11–12px、`--color-accent-soft` 底、3–4px 圆角；
  chip 激活态反白为 `--color-accent` 底白字。
- **吸顶题号栏**（`.problem-nav`）：`position: sticky; top: 0` + 不透明 `--color-bg` 底 + 1px 下边框；
  左「第 N 题」用 accent 色 UI 字体，右「上一题 / 下一题」文字链。第 1 题无上一题、
  第 100 题无下一题，缺省的一侧**不渲染**（不放假交互态，避免点了没反应）。
  吸顶状态由零高度哨兵 `.problem-nav__sentinel` + IntersectionObserver 判定（sticky 元素自身
  粘住后始终可见，观测不到）：吸顶时在题号后补「中文标题 · 英文标题」，此时页头大标题已滚出视野；
  该标题窄屏下截断，不挤压右侧按钮。无过渡动画，随滚动状态直接出现/消失。
- **状态列**：列表页**不设**状态列——`meta.json` 的 `answer` 在 server 端是非空 `Long`，
  缺答案的目录在 `ContentIndex` 加载时就解码失败被跳过，能列出来的题目必然已解，恒真的列是噪声。
- **代码块**（`.code-block`）：标题栏 + `#f6f4ef` 米色底，`--font-mono`，上下圆角 6px 拼合。
- **耗时对比图**（`.timing-chart`）：1px 边框 + `#fdfcf9` 底的 SVG 框架，横向条形、对数刻度。
- **多语言对比图**（`.lang-chart`）：同上视觉语言，N 条序列按耗时升序排列；
  算不出结果的运行时不画条，用 `--color-bad` 虚线空槽 + 文字标注（不伪造数值）；
  窄屏下 `min-width: 560px` + 横向滚动，不把标签缩到看不清。
- **延伸阅读入口**（`.read-more`）：列表页统计行下方的 13px 文字链，`--color-accent` 着色。
- **空态/加载**：居中、muted、`--font-ui`，配虚线边框框（`.empty-filter`）。
- **响应式**：`max-width: 640px` 下表格隐藏「标签」列（`:nth-child(4)`）。

## 动效

只有 hover 态的 `transition`（0.15s 上下的颜色/边框过渡）。**不新增**：
滚动触发 reveal、入场动画、加载 spinner、marquee。数学站读者要的是静止。

## 反模式（明确禁止）

- 蓝紫渐变 / 毛玻璃 / 彩虹装饰色 / 第二个强调色
- 卡片套卡片、按钮下阴影、左侧色条装饰
- emoji 当图标、图标字体外加彩色圆角方块底
- 中断阅读的模态框、登录墙（本站无账号体系）
