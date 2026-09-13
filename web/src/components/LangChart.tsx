import type { ReportChart } from '../api/types'
import { formatBaseline } from './TimingChart'

/* 对数刻度布局常量（viewBox 单位），视觉语言与详情页的耗时对比图保持一致 */
const VIEW_W = 680
const PAD_Y = 8
const BAR_H = 22
const ROW_GAP = 14
/* 语言标签较长（含 CJK），左列留足 210 个单位，条形区 330 */
const LABEL_X = 210
const BAR_X = 220
const BAR_MAX_W = 330

interface Row {
  lang: string
  ms: number | null
  error: string | null
}

/**
 * 同任务下多语言/多运行时的耗时对比（横向条形图，对数刻度）。
 * 倍差可达两三个数量级，线性刻度下短条不可见，故以 log10 映射宽度；
 * 算不出结果的运行时（如 JSC 的 BigInt 上限）渲染成虚线空槽，不伪造数值。
 */
export function LangChart({ chart }: { chart: ReportChart }) {
  const rows: Row[] = [...chart.series]
    .map((s) => ({ lang: s.lang, ms: s.ms ?? null, error: s.error ?? null }))
    .sort((a, b) => (a.ms ?? Number.POSITIVE_INFINITY) - (b.ms ?? Number.POSITIVE_INFINITY))
  if (rows.length === 0) return null

  const values = rows.map((r) => r.ms).filter((v): v is number => v !== null && v > 0)
  let logMin = values.length > 0 ? Math.floor(Math.log10(Math.min(...values))) : 0
  let logMax = values.length > 0 ? Math.ceil(Math.log10(Math.max(...values))) : 1
  if (logMax === logMin) logMax = logMin + 1

  const widthOf = (ms: number): number => {
    const frac = (Math.log10(ms) - logMin) / (logMax - logMin)
    // 最小值恰落在 10^k 上时 frac 为 0，给一个可见的最小宽度
    return Math.max(frac, 0.06) * BAR_MAX_W
  }

  const viewH = PAD_Y * 2 + rows.length * BAR_H + (rows.length - 1) * ROW_GAP

  return (
    <figure className="lang-chart">
      <svg
        viewBox={`0 0 ${VIEW_W} ${viewH}`}
        role="img"
        aria-label={`${chart.title}：${rows
          .map((r) => `${r.lang} ${r.ms === null ? `无法完成（${r.error ?? '失败'}）` : formatBaseline(r.ms)}`)
          .join('，')}`}
      >
        {rows.map((row, i) => {
          const y = PAD_Y + i * (BAR_H + ROW_GAP)
          return (
            <g key={row.lang}>
              <text
                className="lang-chart__label"
                x={LABEL_X}
                y={y + BAR_H / 2}
                textAnchor="end"
                dominantBaseline="central"
              >
                {row.lang}
              </text>
              {row.ms === null ? (
                <>
                  <rect
                    className="lang-chart__bar lang-chart__bar--error"
                    x={BAR_X}
                    y={y}
                    width={BAR_MAX_W * 0.06}
                    height={BAR_H}
                    rx={3}
                  />
                  <text
                    className="lang-chart__value"
                    x={BAR_X + BAR_MAX_W * 0.06 + 8}
                    y={y + BAR_H / 2}
                    dominantBaseline="central"
                  >
                    {row.error ?? '失败'}
                  </text>
                </>
              ) : (
                <>
                  <rect
                    className="lang-chart__bar"
                    x={BAR_X}
                    y={y}
                    width={widthOf(row.ms)}
                    height={BAR_H}
                    rx={3}
                  />
                  <text
                    className="lang-chart__value"
                    x={BAR_X + widthOf(row.ms) + 8}
                    y={y + BAR_H / 2}
                    dominantBaseline="central"
                  >
                    {formatBaseline(row.ms)}
                  </text>
                </>
              )}
            </g>
          )
        })}
      </svg>
      <figcaption className="lang-chart__note">
        {chart.note}
        <span className="lang-chart__unit">（单位：{chart.unit}）</span>
      </figcaption>
    </figure>
  )
}
