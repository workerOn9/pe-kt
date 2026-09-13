/** 后端 API 数据模型（与 Ktor 端 DTO 对齐） */

export interface ProblemMeta {
  id: number
  title: string
  titleZh: string
  difficulty: number
  difficultyLevel: string
  tags: string[]
  /** 数字答案；server 端为非空 Long（缺答案的题目加载时即被跳过），前端恒有值 */
  answer: number
  solvedBy: number
  bruteForceBaselineMs: number | null
  optimizedBaselineMs: number | null
  hasVisualization: boolean
  sourceUrl: string
  fetchedAt: string
}

export interface ProblemDetail extends ProblemMeta {
  /** 题面 Markdown（中文） */
  statement: string
  /** 解析 Markdown（含 KaTeX 数学公式与表格） */
  analysis: string
  /** 现实应用 Markdown；该题没有此板块时为 null */
  applications: string | null
}

export interface Solution {
  id: number
  /** 优化解 Kotlin 源码 */
  solution: string
  /** 暴力解源码，可能为 null */
  bruteForce: string | null
}

export interface RunResult {
  id: number
  answer: number
  expected: number
  correct: boolean
  durationMs: number
}

/** 后端统一错误格式 { error, message } */
export interface ApiErrorBody {
  error: string
  message: string
}

/* ---------- 可视化协议 v1（docs/06-visualization-protocol.md） ---------- */

/** 单元格/元素状态枚举（v1） */
export type CellState =
  | 'idle'
  | 'current'
  | 'eliminated'
  | 'prime'
  | 'onPath'
  | 'considered'
  | 'done'

/** 网格单元格；row/col 仅 grid2d 提供，label 为步骤覆盖显示文本 */
export interface VizCell {
  index: number
  value: number
  state: CellState
  row?: number
  col?: number
  label?: string
}

/** grid1d 场景：一维网格（如埃氏筛） */
export interface Grid1dScene {
  columns: number
  cells: VizCell[]
}

/** grid2d 场景：二维/三角网格（如路径 DP）；index 按行优先编号 */
export interface Grid2dScene {
  rows: number
  triangle?: boolean
  cells: VizCell[]
}

/** sequence 场景：数值序列（如 Collatz 轨道），从 start 起逐点追加 */
export interface SequenceScene {
  start: number
  xLabel?: string
  yLabel?: string
}

/**
 * 步骤的增量修改条目：
 * - grid1d / grid2d：{ index, state, label? }
 * - sequence：{ append, state? }
 */
export interface CellUpdate {
  index?: number
  state?: CellState
  label?: string
  append?: number
}

/** 单个步骤 */
export interface VizStep {
  /** 步骤序号（从 0 开始，冗余自描述） */
  i: number
  updates: CellUpdate[]
  /** 中文解说（一句话） */
  caption: string
  /** 可选；对应 solution.kt 的行号（代码联动） */
  codeLine?: number
}

interface VisualizationBase {
  version: number
  problemId: number
  title: string
  steps: VizStep[]
}

/** 可视化分步数据；kind 决定 scene 形状与前端渲染器 */
export type Visualization =
  | (VisualizationBase & { kind: 'grid1d'; scene: Grid1dScene })
  | (VisualizationBase & { kind: 'grid2d'; scene: Grid2dScene })
  | (VisualizationBase & { kind: 'sequence'; scene: SequenceScene })

/* ---------- 基准报告（content/benchmarks/，正文用 [[chart:id]] 标记图表插入点） ---------- */

/** 图表中的一条序列；ms 与 error 恰有其一（后者表示该运行时无法完成该规模） */
export interface ChartSeries {
  lang: string
  ms?: number | null
  error?: string | null
}

export interface ReportChart {
  id: string
  title: string
  unit: string
  note: string
  series: ChartSeries[]
}

export interface ReportDetail {
  slug: string
  title: string
  subtitle: string
  /** 测量日期（YYYY-MM-DD），缺失时为空串 */
  measuredAt: string
  /** 测量机器描述，缺失时为空串 */
  machine: string
  /** 报告正文 Markdown */
  markdown: string
  charts: ReportChart[]
}
