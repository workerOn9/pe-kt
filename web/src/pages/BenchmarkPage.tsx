import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError, fetchReport } from '../api/client'
import type { ReportChart, ReportDetail } from '../api/types'
import { ErrorMessage } from '../components/ErrorMessage'
import { LangChart } from '../components/LangChart'
import { Markdown } from '../components/Markdown'

/** 图表插入点：正文里独占一行的 `[[chart:<id>]]` */
const CHART_MARKER = /^\[\[chart:([A-Za-z0-9_-]+)]]$/gm

/** 默认报告（站点目前只有这一份基准报告） */
const DEFAULT_SLUG = 'languages'

type Block = { kind: 'markdown'; text: string } | { kind: 'chart'; chart: ReportChart }

/**
 * 把正文按图表插入点切成 Markdown 段与图表段；标记指向的图表不存在时该标记被忽略
 * （内容校验测试会拦住这种脱节，这里只做降级渲染）。
 */
function splitBlocks(markdown: string, charts: ReportChart[]): Block[] {
  const byId = new Map(charts.map((c) => [c.id, c]))
  const blocks: Block[] = []
  let cursor = 0
  for (const match of markdown.matchAll(CHART_MARKER)) {
    const index = match.index ?? 0
    const before = markdown.slice(cursor, index)
    if (before.trim() !== '') blocks.push({ kind: 'markdown', text: before })
    const chart = byId.get(match[1])
    if (chart) blocks.push({ kind: 'chart', chart })
    cursor = index + match[0].length
  }
  const tail = markdown.slice(cursor)
  if (tail.trim() !== '') blocks.push({ kind: 'markdown', text: tail })
  return blocks
}

export function BenchmarkPage() {
  const params = useParams<{ slug: string }>()
  const slug = params.slug ?? DEFAULT_SLUG

  const [report, setReport] = useState<ReportDetail | null>(null)
  const [error, setError] = useState<ApiError | null>(null)

  useEffect(() => {
    let cancelled = false
    setReport(null)
    setError(null)
    fetchReport(slug)
      .then((r) => {
        if (!cancelled) setReport(r)
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e : new ApiError(0, 'UNKNOWN', '加载报告失败'))
        }
      })
    return () => {
      cancelled = true
    }
  }, [slug])

  const blocks = useMemo(
    () => (report ? splitBlocks(report.markdown, report.charts) : []),
    [report],
  )

  if (error) {
    return (
      <ErrorMessage title="报告加载失败" message={error.message}>
        <p>
          <Link to="/">← 返回题目列表</Link>
        </p>
      </ErrorMessage>
    )
  }
  if (!report) {
    return <p className="loading">加载中…</p>
  }

  return (
    <article className="report">
      <p className="back-link">
        <Link to="/">← 返回题目列表</Link>
      </p>

      <header className="problem-header">
        <h1>{report.title}</h1>
        {report.subtitle !== '' && <p className="problem-header__en">{report.subtitle}</p>}
        <div className="problem-header__meta">
          {report.measuredAt !== '' && (
            <span className="difficulty-badge">实测于 {report.measuredAt}</span>
          )}
          {report.machine !== '' && <span className="tag">{report.machine}</span>}
        </div>
      </header>

      <div className="report-body">
        {blocks.map((block, i) =>
          block.kind === 'markdown' ? (
            <Markdown key={`md-${i}`}>{block.text}</Markdown>
          ) : (
            <LangChart key={`chart-${block.chart.id}`} chart={block.chart} />
          ),
        )}
      </div>
    </article>
  )
}
