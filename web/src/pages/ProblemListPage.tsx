import { useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchProblems } from '../api/client'
import { ApiError } from '../api/client'
import type { ProblemMeta } from '../api/types'
import { ErrorMessage } from '../components/ErrorMessage'

type DifficultyBand = 'all' | 'low' | 'mid' | 'high'

/** 触底自动加载：每屏追加的题目数 */
const PAGE_SIZE = 20

const DIFFICULTY_OPTIONS: { value: DifficultyBand; label: string }[] = [
  { value: 'all', label: '全部难度' },
  { value: 'low', label: '1–2%' },
  { value: 'mid', label: '3–5%' },
  { value: 'high', label: '6%+' },
]

function inBand(difficulty: number, band: DifficultyBand): boolean {
  switch (band) {
    case 'all':
      return true
    case 'low':
      return difficulty <= 2
    case 'mid':
      return difficulty >= 3 && difficulty <= 5
    case 'high':
      return difficulty >= 6
  }
}

export function ProblemListPage() {
  const [problems, setProblems] = useState<ProblemMeta[] | null>(null)
  const [error, setError] = useState<ApiError | null>(null)

  const [query, setQuery] = useState('')
  const [activeTag, setActiveTag] = useState<string | null>(null)
  const [band, setBand] = useState<DifficultyBand>('all')

  useEffect(() => {
    let cancelled = false
    fetchProblems()
      .then((list) => {
        if (!cancelled) {
          setProblems([...list].sort((a, b) => a.id - b.id))
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e : new ApiError(0, 'UNKNOWN', '加载题目列表失败'))
        }
      })
    return () => {
      cancelled = true
    }
  }, [])

  const list = useMemo(() => problems ?? [], [problems])

  const allTags = useMemo(() => {
    const set = new Set<string>()
    for (const p of list) {
      for (const t of p.tags) set.add(t)
    }
    return [...set].sort((a, b) => a.localeCompare(b))
  }, [list])

  // 搜索（题号 / 中文标题 / 英文标题）：不区分大小写，关键词按非字母数字切成若干段，
  // 全部命中才算匹配——英文标题多带逗号与连字符（"Triangular, Pentagonal, and Hexagonal"），
  // 整串 substring 匹配会漏掉「triangular pentagonal」这类自然的逐词输入，词序不同也一样命中。
  // 另外比较一次「去掉全部空白」的紧凑形式，好让 "10001" 也能命中标题里的 "10 001st"
  const filtered = useMemo(() => {
    const tokens = query.toLowerCase().match(/[\p{L}\p{N}]+/gu) ?? []
    return list.filter((p) => {
      if (activeTag !== null && !p.tags.includes(activeTag)) return false
      if (!inBand(p.difficulty, band)) return false
      if (tokens.length > 0) {
        const spaced = `${p.id} ${p.titleZh} ${p.title}`
          .toLowerCase()
          .replace(/[^\p{L}\p{N}]+/gu, ' ')
        const compact = spaced.replace(/\s+/g, '')
        if (!tokens.every((t) => spaced.includes(t) || compact.includes(t))) return false
      }
      return true
    })
  }, [list, query, activeTag, band])

  const filtering = query.trim() !== '' || activeTag !== null || band !== 'all'

  // 触底自动加载：筛选条件变化时重置可见数量
  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE)
  useEffect(() => {
    setVisibleCount(PAGE_SIZE)
  }, [query, activeTag, band])

  const visible = useMemo(() => filtered.slice(0, visibleCount), [filtered, visibleCount])
  const hasMore = visibleCount < filtered.length

  const sentinelRef = useRef<HTMLDivElement | null>(null)
  useEffect(() => {
    const el = sentinelRef.current
    if (!el || !hasMore) return
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((e) => e.isIntersecting)) {
          setVisibleCount((c) => Math.min(c + PAGE_SIZE, filtered.length))
        }
      },
      { rootMargin: '240px' },
    )
    observer.observe(el)
    return () => observer.disconnect()
  }, [hasMore, filtered.length])

  const clearFilters = () => {
    setQuery('')
    setActiveTag(null)
    setBand('all')
  }

  if (error) {
    return <ErrorMessage title="题目列表加载失败" message={error.message} />
  }
  if (!problems) {
    return <p className="loading">加载中…</p>
  }
  if (problems.length === 0) {
    return <p className="empty">暂无题目，后端尚未收录内容。</p>
  }

  return (
    <div className="problem-list">
      <div className="list-toolbar">
        <input
          type="search"
          className="list-toolbar__search"
          placeholder="搜索题号或中英文标题…（Esc 清空）"
          aria-label="搜索题目"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Escape') setQuery('')
          }}
        />
        <select
          className="list-toolbar__select"
          aria-label="难度筛选"
          value={band}
          onChange={(e) => setBand(e.target.value as DifficultyBand)}
        >
          {DIFFICULTY_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      </div>
      <div className="list-toolbar__tags" role="group" aria-label="标签筛选">
        {allTags.map((t) => (
          <button
            key={t}
            type="button"
            className={activeTag === t ? 'filter-chip filter-chip--active' : 'filter-chip'}
            aria-pressed={activeTag === t}
            onClick={() => setActiveTag(activeTag === t ? null : t)}
          >
            {t}
          </button>
        ))}
      </div>

      <p className="problem-list__summary">
        {filtering ? `筛选出 ${filtered.length} / 共 ${list.length} 题。` : `共 ${list.length} 题。`}
      </p>

      <p className="read-more">
        <Link to="/benchmark">延伸阅读：主流编程语言性能对比（同一道题写成六种语言的实测报告）→</Link>
      </p>

      {filtered.length === 0 ? (
        <div className="empty-filter">
          <p>没有符合条件的题目。</p>
          <p>
            <button type="button" className="empty-filter__clear" onClick={clearFilters}>
              清除全部筛选
            </button>
          </p>
        </div>
      ) : (
        <table className="problem-table">
          <thead>
            <tr>
              <th className="col-id">题号</th>
              <th className="col-title">标题</th>
              <th className="col-difficulty">难度</th>
              <th className="col-tags">标签</th>
            </tr>
          </thead>
          <tbody>
            {visible.map((p) => (
              <tr key={p.id}>
                <td className="col-id">{p.id}</td>
                <td className="col-title">
                  <Link to={`/problem/${p.id}`} className="problem-link">
                    {p.titleZh}
                  </Link>
                  <span className="problem-title-en">{p.title}</span>
                </td>
                <td className="col-difficulty">
                  <span className="difficulty-badge">{p.difficulty}%</span>
                </td>
                <td className="col-tags">
                  {p.tags.map((t) => (
                    <span key={t} className="tag">
                      {t}
                    </span>
                  ))}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      {filtered.length > 0 && (
        <div ref={sentinelRef} className="list-sentinel" aria-live="polite">
          {hasMore
            ? `已显示 ${visible.length} / ${filtered.length} 题，下滑加载更多…`
            : filtered.length > PAGE_SIZE
              ? `已加载全部 ${filtered.length} 题。`
              : ''}
        </div>
      )}
    </div>
  )
}
