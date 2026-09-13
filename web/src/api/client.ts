import type {
  ApiErrorBody,
  ProblemDetail,
  ProblemMeta,
  ReportDetail,
  RunResult,
  Solution,
  Visualization,
} from './types'

/** 携带后端 { error, message } 信息的 API 错误 */
export class ApiError extends Error {
  readonly status: number
  readonly code: string

  constructor(status: number, code: string, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let resp: Response
  try {
    resp = await fetch(path, {
      headers: { Accept: 'application/json' },
      ...init,
    })
  } catch {
    throw new ApiError(0, 'NETWORK_ERROR', '无法连接到后端服务（localhost:8080），请确认后端已启动')
  }

  if (!resp.ok) {
    // 后端统一错误格式：{ error, message }
    let body: ApiErrorBody | null = null
    try {
      body = (await resp.json()) as ApiErrorBody
    } catch {
      // 非 JSON 错误体，忽略
    }
    throw new ApiError(
      resp.status,
      body?.error ?? `HTTP_${resp.status}`,
      body?.message ?? `请求失败（HTTP ${resp.status}）`,
    )
  }

  return (await resp.json()) as T
}

/**
 * 题目列表：内容随镜像发布、运行期不变，会话内共用一次请求
 * （列表页与详情页的「上一题/下一题」都用它）。失败不缓存，下次调用重新发起。
 */
let problemsPromise: Promise<ProblemMeta[]> | null = null

export function fetchProblems(): Promise<ProblemMeta[]> {
  problemsPromise ??= request<ProblemMeta[]>('/api/problems').catch((e: unknown) => {
    problemsPromise = null
    throw e
  })
  return problemsPromise
}

export async function fetchProblem(id: number): Promise<ProblemDetail> {
    // 后端返回 { meta, statement, analysis, applications }，前端拍平为 ProblemDetail
    const raw = await request<{
        meta: ProblemMeta
        statement: string
        analysis: string
        applications: string | null
    }>(`/api/problems/${id}`)
    return {
        ...raw.meta,
        statement: raw.statement,
        analysis: raw.analysis,
        applications: raw.applications,
    }
}

export function fetchSolution(id: number): Promise<Solution> {
  return request<Solution>(`/api/problems/${id}/solution`)
}

export function runProblem(id: number): Promise<RunResult> {
  return request<RunResult>(`/api/problems/${id}/run`, { method: 'POST' })
}

/**
 * 可视化分步数据。题目无可视化时后端返回 404 { error: "no_visualization" }，
 * 调用方可通过 ApiError.code === 'no_visualization' 区分并静默处理。
 */
export function fetchVisualization(id: number): Promise<Visualization> {
  return request<Visualization>(`/api/problems/${id}/visualize`)
}

/** 基准报告（content/benchmarks/<slug>.md + 同名图表数据） */
export function fetchReport(slug: string): Promise<ReportDetail> {
  return request<ReportDetail>(`/api/benchmarks/${slug}`)
}
