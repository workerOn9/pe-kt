import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError, fetchProblem, fetchProblems, fetchSolution, fetchVisualization, runProblem } from '../api/client'
import type { ProblemDetail, RunResult, Solution, Visualization } from '../api/types'
import { CodeBlock } from '../components/CodeBlock'
import { ErrorMessage } from '../components/ErrorMessage'
import { Markdown } from '../components/Markdown'
import { TimingChart } from '../components/TimingChart'
import { Visualizer } from '../components/Visualizer'
import { stripLeadingHeading } from '../lib/contentPreamble'

type BodyTab = 'statement' | 'analysis'
type CodeTab = 'solution' | 'bruteForce'

export function ProblemDetailPage() {
  const params = useParams<{ id: string }>()
  const id = Number(params.id)

  const [detail, setDetail] = useState<ProblemDetail | null>(null)
  const [solution, setSolution] = useState<Solution | null>(null)
  const [visualization, setVisualization] = useState<Visualization | null>(null)
  const [loadError, setLoadError] = useState<ApiError | null>(null)
  const [solutionError, setSolutionError] = useState<string | null>(null)

  const [bodyTab, setBodyTab] = useState<BodyTab>('statement')
  const [codeTab, setCodeTab] = useState<CodeTab>('solution')

  const [running, setRunning] = useState(false)
  const [runResult, setRunResult] = useState<RunResult | null>(null)
  const [runError, setRunError] = useState<string | null>(null)

  // 相邻题号：null 表示尚未拿到列表（不渲染按钮）
  const [neighbors, setNeighbors] = useState<{ prev: number | null; next: number | null } | null>(null)

  // 吸顶状态：零高度哨兵滚出视口即题号栏已粘住（此时页头大标题已看不见）
  const navSentinelRef = useRef<HTMLDivElement>(null)
  const [navStuck, setNavStuck] = useState(false)

  useEffect(() => {
    if (!Number.isInteger(id) || id <= 0) {
      setLoadError(new ApiError(400, 'BAD_ID', '无效的题目编号'))
      return
    }
    let cancelled = false
    fetchProblem(id)
      .then((d) => {
        if (!cancelled) setDetail(d)
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setLoadError(e instanceof ApiError ? e : new ApiError(0, 'UNKNOWN', '加载题目详情失败'))
        }
      })
    fetchSolution(id)
      .then((s) => {
        if (!cancelled) setSolution(s)
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setSolutionError(e instanceof ApiError ? e.message : '加载参考实现失败')
        }
      })
    return () => {
      cancelled = true
    }
  }, [id])

  // 可视化数据：仅当 meta.hasVisualization 为 true 时拉取；
  // 404 no_visualization 或其他失败均静默处理（不渲染可视化区）
  useEffect(() => {
    if (!detail?.hasVisualization) return
    let cancelled = false
    fetchVisualization(detail.id)
      .then((v) => {
        if (!cancelled) setVisualization(v)
      })
      .catch(() => {
        // 静默：题目暂无可视化数据或后端未就绪
      })
    return () => {
      cancelled = true
    }
  }, [detail])

  // 相邻题号：列表接口一次返回全部题目，按 id 排序后取前后各一个。
  // 拉取失败或当前题不在列表里就静默不出按钮，不影响正文
  useEffect(() => {
    let cancelled = false
    fetchProblems()
      .then((list) => {
        if (cancelled) return
        const ids = list.map((p) => p.id).sort((a, b) => a - b)
        const at = ids.indexOf(id)
        setNeighbors({
          prev: at > 0 ? ids[at - 1] : null,
          next: at >= 0 && at < ids.length - 1 ? ids[at + 1] : null,
        })
      })
      .catch(() => {
        // 静默：导航按钮不可用不影响阅读
      })
    return () => {
      cancelled = true
    }
  }, [id])

  // 切换题目后回到文章顶部：路由跳转不会重置滚动位置，
  // 否则点「下一题」会落在下一题的中段
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [id])

  // 观测哨兵判断吸顶：依赖 detail 的 id 是因为哨兵要等正文渲染出来才存在
  useEffect(() => {
    const sentinel = navSentinelRef.current
    if (!sentinel) return
    const observer = new IntersectionObserver(([entry]) => setNavStuck(!entry.isIntersecting))
    observer.observe(sentinel)
    return () => observer.disconnect()
  }, [detail?.id])

  const handleRun = useCallback(() => {
    setRunning(true)
    setRunResult(null)
    setRunError(null)
    runProblem(id)
      .then(setRunResult)
      .catch((e: unknown) => {
        setRunError(e instanceof ApiError ? e.message : '运行请求失败')
      })
      .finally(() => setRunning(false))
  }, [id])

  if (loadError) {
    return (
      <ErrorMessage title="题目详情加载失败" message={loadError.message}>
        <p>
          <Link to="/">← 返回题目列表</Link>
        </p>
      </ErrorMessage>
    )
  }
  if (!detail) {
    return <p className="loading">加载中…</p>
  }

  // 正文首行是重复的题目名称（页头与 Tab 已经写明），渲染前摘掉；「中文意译」说明留在正文原处
  const statementBody = stripLeadingHeading(detail.statement)
  const analysisBody = stripLeadingHeading(detail.analysis)

  return (
    <article className="problem-detail">
      <p className="back-link">
        <Link to="/">← 返回题目列表</Link>
      </p>

      {/* 零高度哨兵：滚出视口即题号栏已吸顶 */}
      <div className="problem-nav__sentinel" ref={navSentinelRef} aria-hidden="true" />
      <nav className="problem-nav" aria-label="题目切换">
        <span className="problem-nav__id">第 {detail.id} 题</span>
        {navStuck && (
          <span className="problem-nav__title">
            {detail.titleZh} · {detail.title}
          </span>
        )}
        <div className="problem-nav__links">
          {neighbors?.prev != null && (
            <Link className="problem-nav__link" to={`/problem/${neighbors.prev}`}>
              ← 上一题
            </Link>
          )}
          {neighbors?.next != null && (
            <Link className="problem-nav__link" to={`/problem/${neighbors.next}`}>
              下一题 →
            </Link>
          )}
        </div>
      </nav>

      <header className="problem-header">
        <h1>{detail.titleZh}</h1>
        <p className="problem-header__en">{detail.title}</p>
        <div className="problem-header__meta">
          <span className="difficulty-badge">难度 {detail.difficulty}%</span>
          {detail.tags.map((t) => (
            <span key={t} className="tag">
              {t}
            </span>
          ))}
          <a href={detail.sourceUrl} target="_blank" rel="noreferrer" className="pe-link">
            Project Euler 原文 ↗
          </a>
        </div>
      </header>

      <nav className="tabs" aria-label="正文切换">
        <button
          type="button"
          className={bodyTab === 'statement' ? 'tab tab--active' : 'tab'}
          onClick={() => setBodyTab('statement')}
        >
          题面
        </button>
        <button
          type="button"
          className={bodyTab === 'analysis' ? 'tab tab--active' : 'tab'}
          onClick={() => setBodyTab('analysis')}
        >
          解析
        </button>
      </nav>

      <section className="problem-body">
        {bodyTab === 'statement' ? (
          <Markdown>{statementBody}</Markdown>
        ) : (
          <Markdown>{analysisBody}</Markdown>
        )}
      </section>

      <section className="run-section">
        <h2>运行</h2>
        <button type="button" className="run-button" onClick={handleRun} disabled={running}>
          {running ? '运行中…' : '▶ 运行参考实现'}
        </button>
        {runError && <ErrorMessage title="运行失败" message={runError} />}
        {runResult && (
          <dl className={runResult.correct ? 'run-result run-result--ok' : 'run-result run-result--bad'}>
            <div>
              <dt>答案</dt>
              <dd>{runResult.answer}</dd>
            </div>
            <div>
              <dt>期望答案</dt>
              <dd>{runResult.expected}</dd>
            </div>
            <div>
              <dt>结果</dt>
              <dd>{runResult.correct ? '✓ 正确' : '✗ 不正确'}</dd>
            </div>
            <div>
              <dt>耗时</dt>
              <dd>{formatDuration(runResult.durationMs)}</dd>
            </div>
          </dl>
        )}
      </section>

      {(detail.bruteForceBaselineMs !== null || detail.optimizedBaselineMs !== null) && (
        <section className="timing-section">
          <h2>耗时对比</h2>
          <TimingChart
            bruteForceMs={detail.bruteForceBaselineMs}
            optimizedMs={detail.optimizedBaselineMs}
          />
        </section>
      )}

      {visualization && (
        <section className="viz-section">
          <h2>过程可视化</h2>
          <Visualizer data={visualization} />
        </section>
      )}

      <section className="code-section">
        <h2>参考实现</h2>
        {solutionError && <ErrorMessage title="代码加载失败" message={solutionError} />}
        {!solution && !solutionError && <p className="loading">代码加载中…</p>}
        {solution && (
          <>
            {solution.bruteForce !== null && (
              <nav className="tabs tabs--small" aria-label="代码版本切换">
                <button
                  type="button"
                  className={codeTab === 'solution' ? 'tab tab--active' : 'tab'}
                  onClick={() => setCodeTab('solution')}
                >
                  优化解 solution.kt
                </button>
                <button
                  type="button"
                  className={codeTab === 'bruteForce' ? 'tab tab--active' : 'tab'}
                  onClick={() => setCodeTab('bruteForce')}
                >
                  暴力解 brute-force.kt
                </button>
              </nav>
            )}
            <CodeBlock
              code={codeTab === 'bruteForce' && solution.bruteForce !== null
                ? solution.bruteForce
                : solution.solution}
              title={codeTab === 'bruteForce' ? 'brute-force.kt' : 'solution.kt'}
            />
          </>
        )}
      </section>

      {detail.applications !== null && (
        <section className="applications-section">
          <h2>现实应用</h2>
          <Markdown>{detail.applications}</Markdown>
        </section>
      )}
    </article>
  )
}

function formatDuration(ms: number): string {
  if (ms < 1) return '< 1 ms'
  if (ms < 1000) return `${Math.round(ms)} ms`
  return `${(ms / 1000).toFixed(2)} s`
}
