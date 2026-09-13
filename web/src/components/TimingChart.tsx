interface TimingChartProps {
  /** 暴力解基线耗时（ms），可能为 null */
  bruteForceMs: number | null
  /** 优化解基线耗时（ms），可能为 null */
  optimizedMs: number | null
}

interface BarEntry {
  key: 'brute' | 'opt'
  label: string
  ms: number
}

/* 对数刻度布局常量（viewBox 单位） */
const VIEW_W = 680
const PAD_Y = 8
const BAR_H = 22
const ROW_GAP = 20
const LABEL_X = 68
const BAR_X = 80
const BAR_MAX_W = 440

/**
 * 暴力解 vs 优化解基线耗时对比（横向条形图，对数刻度）。
 * 倍差可达 10^5，线性刻度下短 bar 不可见，故以 log10 映射宽度。
 * 至少一个基线非 null 时渲染；都为 null 时由调用方决定是否挂载。
 */
export function TimingChart({ bruteForceMs, optimizedMs }: TimingChartProps) {
  const entries: BarEntry[] = []
  if (bruteForceMs !== null) {
    entries.push({ key: 'brute', label: '暴力解', ms: bruteForceMs })
  }
  if (optimizedMs !== null) {
    entries.push({ key: 'opt', label: '优化解', ms: optimizedMs })
  }
  if (entries.length === 0) return null

  const logs = entries.map((e) => Math.log10(e.ms))
  let logMin = Math.floor(Math.min(...logs))
  let logMax = Math.ceil(Math.max(...logs))
  if (logMax === logMin) logMax = logMin + 1

  const widthOf = (ms: number): number => {
    const frac = (Math.log10(ms) - logMin) / (logMax - logMin)
    // 最小值恰落在 10^k 上时 frac 为 0，给一个可见的最小宽度
    return Math.max(frac, 0.06) * BAR_MAX_W
  }

  const twoBars = entries.length === 2
  const speedup =
    twoBars && bruteForceMs !== null && optimizedMs !== null && optimizedMs > 0
      ? bruteForceMs / optimizedMs
      : null

  const viewH = PAD_Y * 2 + entries.length * BAR_H + (entries.length - 1) * ROW_GAP

  return (
    <div className="timing-chart">
      <svg
        viewBox={`0 0 ${VIEW_W} ${viewH}`}
        role="img"
        aria-label={
          speedup !== null
            ? `耗时对比：暴力解 ${formatBaseline(bruteForceMs ?? 0)}，优化解 ${formatBaseline(optimizedMs ?? 0)}，快约 ${formatRatio(speedup)} 倍`
            : entries.map((e) => `${e.label} ${formatBaseline(e.ms)}`).join('，')
        }
      >
        {entries.map((e, i) => {
          const y = PAD_Y + i * (BAR_H + ROW_GAP)
          const w = widthOf(e.ms)
          return (
            <g key={e.key}>
              <text
                className="timing-chart__label"
                x={LABEL_X}
                y={y + BAR_H / 2}
                textAnchor="end"
                dominantBaseline="central"
              >
                {e.label}
              </text>
              <rect
                className={`timing-chart__bar timing-chart__bar--${e.key}`}
                x={BAR_X}
                y={y}
                width={w}
                height={BAR_H}
                rx={3}
              />
              <text
                className="timing-chart__value"
                x={BAR_X + w + 8}
                y={y + BAR_H / 2}
                dominantBaseline="central"
              >
                ~{formatBaseline(e.ms)}
              </text>
            </g>
          )
        })}
        {speedup !== null && (
          <text
            className="timing-chart__speedup"
            x={BAR_X + BAR_MAX_W / 2}
            y={PAD_Y + BAR_H + ROW_GAP / 2}
            textAnchor="middle"
            dominantBaseline="central"
          >
            快 ~{formatRatio(speedup)}×
          </text>
        )}
      </svg>
      <p className="timing-chart__note">基线为 JIT 预热后实测最优值，见 meta.json。</p>
    </div>
  )
}

/** 自适应单位格式化：输入 ms，输出 ns / µs / ms / s */
export function formatBaseline(ms: number): string {
  const ns = ms * 1e6
  if (ns < 10) return `${trimNum(ns)} ns`
  const us = ns / 1e3
  if (us < 1e3) return `${trimNum(us)} µs`
  if (ms < 1e3) return `${trimNum(ms)} ms`
  return `${trimNum(ms / 1e3)} s`
}

/** 数值修剪：≥100 取整，≥10 一位小数，否则两位小数，并去掉多余的尾零 */
function trimNum(v: number): string {
  const s = v >= 100 ? v.toFixed(0) : v >= 10 ? v.toFixed(1) : v.toFixed(2)
  return s.includes('.') ? s.replace(/0+$/, '').replace(/\.$/, '') : s
}

function formatRatio(r: number): string {
  if (r >= 10) return String(Math.round(r))
  return r.toFixed(1)
}
