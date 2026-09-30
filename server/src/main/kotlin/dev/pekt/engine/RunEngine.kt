package dev.pekt.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlin.time.measureTimedValue

/** 求解执行超时上限（架构约束：默认 10s）。 */
const val RUN_TIMEOUT_MS = 10_000L

/** 题号存在但尚未注册解法（501 no_solver）。 */
class NoSolverException(val problemId: Int) :
    RuntimeException("题目 $problemId 尚未注册解法（no solver registered）")

/** 求解超过 [RUN_TIMEOUT_MS] 被熔断（504 timeout）。 */
class SolverTimeoutException(val problemId: Int) :
    RuntimeException("题目 $problemId 求解超时（>${RUN_TIMEOUT_MS}ms）")

/**
 * POST /api/problems/{id}/run 的响应体。
 * answer/expected 统一为字符串：绝大多数题是十进制数字（与 meta.json 一致），
 * 少数题（如 PE 284 要求 base-14 小写字母）直接携带原文形式的答案。
 */
@Serializable
data class RunResult(
    val id: Int,
    val answer: String,
    val expected: String,
    val correct: Boolean,
    val durationMs: Long,
)

/**
 * 执行引擎（D-04 v1）：注册表查找 + Dispatchers.Default 线程池隔离 + withTimeout 超时熔断。
 * Phase 2 末期迁移到子进程沙箱，见 docs/04-decisions.md D-04。
 */
object RunEngine {

    /** 统一查找解法：数值注册表 [solvers] 的结果转字符串，或字符串注册表 [stringSolvers]。 */
    fun solverOf(problemId: Int): (() -> String)? =
        solvers[problemId]?.let { numeric -> { numeric().toString() } } ?: stringSolvers[problemId]

    /** 题号是否已注册解法（数值或字符串答案）。 */
    fun hasSolver(problemId: Int): Boolean = problemId in solvers || problemId in stringSolvers

    /** 执行 [problemId] 的注册解法，[expected] 为 meta.json 中的标准答案用于比对。 */
    suspend fun run(problemId: Int, expected: String): RunResult {
        val solver = solverOf(problemId) ?: throw NoSolverException(problemId)
        val timed = try {
            withContext(Dispatchers.Default) {
                withTimeout(RUN_TIMEOUT_MS) { measureTimedValue { solver() } }
            }
        } catch (e: TimeoutCancellationException) {
            throw SolverTimeoutException(problemId)
        }
        return RunResult(
            id = problemId,
            answer = timed.value,
            expected = expected,
            correct = timed.value == expected,
            durationMs = timed.duration.inWholeMilliseconds,
        )
    }
}
