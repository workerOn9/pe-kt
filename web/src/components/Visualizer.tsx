import { useCallback, useEffect, useMemo, useState } from 'react'
import type {
  CellState,
  Grid1dScene,
  Grid2dScene,
  SequenceScene,
  VizCell,
  Visualization,
} from '../api/types'

/** 1× 速度下每步的基础间隔（ms） */
const BASE_STEP_MS = 800
const SPEED_OPTIONS = [0.5, 1, 2, 4] as const

interface VisualizerProps {
  data: Visualization
}

/**
 * 可视化播放器（协议 v1，docs/06-visualization-protocol.md）。
 * 数据驱动：只渲染后端生成的分步数据，三种 kind 对应三个 SVG 渲染器。
 */
export function Visualizer({ data }: VisualizerProps) {
  const total = data.steps.length
  /** 已应用的步数：0 = 初始场景，k = 已应用 steps[0..k-1] */
  const [pos, setPos] = useState(0)
  const [playing, setPlaying] = useState(false)
  const [speed, setSpeed] = useState(1)

  const stepTo = useCallback(
    (next: number) => setPos(Math.max(0, Math.min(total, next))),
    [total],
  )

  // 播放推进：到末尾自动停止
  useEffect(() => {
    if (!playing) return
    if (pos >= total) {
      setPlaying(false)
      return
    }
    const timer = setTimeout(() => setPos((p) => Math.min(p + 1, total)), BASE_STEP_MS / speed)
    return () => clearTimeout(timer)
  }, [playing, pos, speed, total])

  // 键盘无障碍：焦点在播放器内时 ←/→ 步进，空格播放/暂停
  const handleKeyDown = (e: React.KeyboardEvent<HTMLDivElement>) => {
    const target = e.target as HTMLElement
    if (e.key === ' ') {
      // 空格落在按钮/滑块上时交给原生行为，避免双重触发
      if (target === e.currentTarget) {
        e.preventDefault()
        if (pos >= total && !playing) {
          setPos(0)
        }
        setPlaying((p) => !p)
      }
      return
    }
    if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') {
      if (target instanceof HTMLSelectElement || target instanceof HTMLInputElement) return
      e.preventDefault()
      setPlaying(false)
      stepTo(pos + (e.key === 'ArrowRight' ? 1 : -1))
    }
  }

  const currentStep = pos > 0 ? data.steps[pos - 1] : null

  return (
    <div
      className="visualizer"
      role="region"
      aria-label={`过程可视化：${data.title}`}
      tabIndex={0}
      onKeyDown={handleKeyDown}
    >
      <p className="visualizer__title">{data.title}</p>

      <div className="visualizer__stage">
        {data.kind === 'grid1d' && <Grid1dRenderer scene={data.scene} steps={data.steps} pos={pos} />}
        {data.kind === 'grid2d' && <Grid2dRenderer scene={data.scene} steps={data.steps} pos={pos} />}
        {data.kind === 'sequence' && <SequenceRenderer scene={data.scene} steps={data.steps} pos={pos} />}
      </div>

      <div className="visualizer__controls">
        <button
          type="button"
          className="viz-button viz-button--primary"
          aria-label={playing ? '暂停' : '播放'}
          onClick={() => {
            if (!playing && pos >= total) setPos(0) // 播完后再次播放从头开始
            setPlaying((p) => !p)
          }}
        >
          {playing ? '⏸ 暂停' : '▶ 播放'}
        </button>
        <button
          type="button"
          className="viz-button"
          aria-label="上一步"
          disabled={pos <= 0}
          onClick={() => {
            setPlaying(false)
            stepTo(pos - 1)
          }}
        >
          ← 上一步
        </button>
        <button
          type="button"
          className="viz-button"
          aria-label="下一步"
          disabled={pos >= total}
          onClick={() => {
            setPlaying(false)
            stepTo(pos + 1)
          }}
        >
          下一步 →
        </button>

        <input
          type="range"
          className="visualizer__slider"
          min={0}
          max={total}
          value={pos}
          aria-label="步骤进度"
          onChange={(e) => {
            setPlaying(false)
            stepTo(Number(e.target.value))
          }}
        />

        <label className="visualizer__speed">
          速度
          <select
            value={speed}
            onChange={(e) => setSpeed(Number(e.target.value))}
            aria-label="播放速度"
          >
            {SPEED_OPTIONS.map((s) => (
              <option key={s} value={s}>
                {s}×
              </option>
            ))}
          </select>
        </label>

        <span className="visualizer__step-indicator">
          第 {pos} / {total} 步
        </span>
      </div>

      {currentStep?.codeLine != null && (
        <p className="visualizer__codeline">
          <code>solution.kt</code> 第 {currentStep.codeLine} 行
        </p>
      )}

      <p className="visualizer__caption" aria-live="polite">
        {currentStep?.caption ?? '初始状态——按播放或下一步开始。'}
      </p>
    </div>
  )
}

/* ==========================================================================
   状态应用：把 steps[0..pos-1] 的 updates 累积到初始场景上
   ========================================================================== */

function applyGridSteps(cells: VizCell[], steps: Visualization['steps'], pos: number): VizCell[] {
  const byIndex = new Map<number, VizCell>(cells.map((c) => [c.index, { ...c }]))
  for (let k = 0; k < pos; k++) {
    for (const u of steps[k].updates) {
      if (u.index == null) continue
      const cell = byIndex.get(u.index)
      if (!cell) continue
      if (u.state != null) cell.state = u.state
      if (u.label != null) cell.label = u.label
    }
  }
  return cells.map((c) => byIndex.get(c.index) ?? c)
}

function cellText(cell: VizCell): string {
  return cell.label ?? String(cell.value)
}

/* ==========================================================================
   grid1d —— 一维方格阵列（按 columns 换行），方格 36px
   ========================================================================== */

const G1_CELL = 36
const G1_GAP = 4

function Grid1dRenderer({
  scene,
  steps,
  pos,
}: {
  scene: Grid1dScene
  steps: Visualization['steps']
  pos: number
}) {
  const cells = useMemo(() => applyGridSteps(scene.cells, steps, pos), [scene, steps, pos])
  const cols = Math.max(1, scene.columns)
  const rows = Math.ceil(cells.length / cols)
  const width = cols * (G1_CELL + G1_GAP) + G1_GAP
  const height = rows * (G1_CELL + G1_GAP) + G1_GAP

  return (
    <svg
      viewBox={`0 0 ${width} ${height}`}
      className="viz-svg"
      role="img"
      aria-label="一维网格可视化"
    >
      {cells.map((cell, i) => {
        const col = i % cols
        const row = Math.floor(i / cols)
        const x = G1_GAP + col * (G1_CELL + G1_GAP)
        const y = G1_GAP + row * (G1_CELL + G1_GAP)
        return (
          <g key={cell.index} className={`viz-cell viz-cell--${cell.state}`}>
            <rect x={x} y={y} width={G1_CELL} height={G1_CELL} rx={4} />
            <text x={x + G1_CELL / 2} y={y + G1_CELL / 2} className="viz-cell__text">
              {cellText(cell)}
            </text>
            {cell.state === 'eliminated' && (
              <line
                x1={x + 6}
                y1={y + G1_CELL - 6}
                x2={x + G1_CELL - 6}
                y2={y + 6}
                className="viz-cell__strike"
              />
            )}
          </g>
        )
      })}
    </svg>
  )
}

/* ==========================================================================
   grid2d —— 二维/三角网格；triangle=true 时按三角阵居中排布（圆角块）
   ========================================================================== */

const G2_CELL_W = 48
const G2_CELL_H = 36
const G2_GAP = 6

function Grid2dRenderer({
  scene,
  steps,
  pos,
}: {
  scene: Grid2dScene
  steps: Visualization['steps']
  pos: number
}) {
  const cells = useMemo(() => applyGridSteps(scene.cells, steps, pos), [scene, steps, pos])
  const rows = Math.max(1, scene.rows)
  const triangle = scene.triangle === true
  // 非三角阵时按每行最大列数对齐
  const maxCols = triangle
    ? rows
    : Math.max(1, ...cells.map((c) => (c.col ?? 0) + 1))

  const width = maxCols * (G2_CELL_W + G2_GAP) + G2_GAP
  const height = rows * (G2_CELL_H + G2_GAP) + G2_GAP

  return (
    <svg
      viewBox={`0 0 ${width} ${height}`}
      className="viz-svg"
      role="img"
      aria-label="二维网格可视化"
    >
      {cells.map((cell) => {
        const row = cell.row ?? 0
        const col = cell.col ?? 0
        const rowLen = triangle ? row + 1 : maxCols
        const offsetX = triangle
          ? (width - G2_GAP - rowLen * (G2_CELL_W + G2_GAP)) / 2 + G2_GAP
          : G2_GAP
        const x = offsetX + col * (G2_CELL_W + G2_GAP)
        const y = G2_GAP + row * (G2_CELL_H + G2_GAP)
        return (
          <g key={cell.index} className={`viz-cell viz-cell--${cell.state}`}>
            <rect x={x} y={y} width={G2_CELL_W} height={G2_CELL_H} rx={G2_CELL_H / 2} />
            <text x={x + G2_CELL_W / 2} y={y + G2_CELL_H / 2} className="viz-cell__text">
              {cellText(cell)}
            </text>
          </g>
        )
      })}
    </svg>
  )
}

/* ==========================================================================
   sequence —— 折线图：x=步数 y=值，自动缩放 viewBox，已过点实线、未到点不画
   ========================================================================== */

const SEQ_W = 700
const SEQ_H = 260
const SEQ_PAD_X = 44
const SEQ_PAD_Y = 32

function SequenceRenderer({
  scene,
  steps,
  pos,
}: {
  scene: SequenceScene
  steps: Visualization['steps']
  pos: number
}) {
  // 全部点（含起点）用于确定坐标范围；已应用 pos 步则画出前 pos+1 个点
  const allValues = useMemo(() => {
    const values = [scene.start]
    for (const step of steps) {
      for (const u of step.updates) {
        if (u.append != null) values.push(u.append)
      }
    }
    return values
  }, [scene, steps])

  const pointStates = useMemo(() => {
    // 与 allValues 对齐的状态：起点 idle，第 k 步 append 的状态取自该步
    const states: CellState[] = ['idle']
    for (const step of steps) {
      for (const u of step.updates) {
        if (u.append != null) states.push(u.state ?? 'idle')
      }
    }
    return states
  }, [steps])

  const drawnCount = Math.min(pos + 1, allValues.length)
  const visible = allValues.slice(0, drawnCount)

  const min = Math.min(...allValues)
  const max = Math.max(...allValues)
  const span = max - min || 1
  const n = Math.max(1, allValues.length - 1)

  const px = (i: number) => SEQ_PAD_X + (i / n) * (SEQ_W - 2 * SEQ_PAD_X)
  const py = (v: number) => SEQ_H - SEQ_PAD_Y - ((v - min) / span) * (SEQ_H - 2 * SEQ_PAD_Y)

  const polyline = visible.map((v, i) => `${px(i)},${py(v)}`).join(' ')

  return (
    <svg
      viewBox={`0 0 ${SEQ_W} ${SEQ_H}`}
      className="viz-svg viz-svg--sequence"
      role="img"
      aria-label="数值序列折线图"
    >
      {/* 坐标轴（从简）：轴线 + x/y 标签 */}
      <line
        x1={SEQ_PAD_X}
        y1={SEQ_H - SEQ_PAD_Y}
        x2={SEQ_W - SEQ_PAD_X}
        y2={SEQ_H - SEQ_PAD_Y}
        className="viz-axis"
      />
      <line
        x1={SEQ_PAD_X}
        y1={SEQ_PAD_Y}
        x2={SEQ_PAD_X}
        y2={SEQ_H - SEQ_PAD_Y}
        className="viz-axis"
      />
      {scene.xLabel && (
        <text x={SEQ_W - SEQ_PAD_X} y={SEQ_H - SEQ_PAD_Y + 20} className="viz-axis__label" textAnchor="end">
          {scene.xLabel}
        </text>
      )}
      {scene.yLabel && (
        <text x={SEQ_PAD_X - 8} y={SEQ_PAD_Y - 10} className="viz-axis__label" textAnchor="start">
          {scene.yLabel}
        </text>
      )}
      <text x={SEQ_PAD_X - 8} y={py(max)} className="viz-axis__tick" textAnchor="end">
        {formatTick(max)}
      </text>
      <text x={SEQ_PAD_X - 8} y={py(min)} className="viz-axis__tick" textAnchor="end">
        {formatTick(min)}
      </text>

      {visible.length >= 2 && <polyline points={polyline} className="viz-sequence__line" />}

      {visible.map((v, i) => {
        const isCurrent = i === visible.length - 1 && pos > 0
        return (
          <circle
            key={i}
            cx={px(i)}
            cy={py(v)}
            r={isCurrent ? 6 : 3}
            className={
              isCurrent
                ? 'viz-sequence__dot viz-sequence__dot--current'
                : `viz-sequence__dot viz-sequence__dot--${pointStates[i] ?? 'idle'}`
            }
          />
        )
      })}
    </svg>
  )
}

function formatTick(v: number): string {
  if (Math.abs(v) >= 10000) return v.toExponential(1)
  return String(Math.round(v * 100) / 100)
}
