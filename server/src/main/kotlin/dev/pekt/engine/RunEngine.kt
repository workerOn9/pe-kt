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

/** POST /api/problems/{id}/run 的响应体。 */
@Serializable
data class RunResult(
    val id: Int,
    val answer: Long,
    val expected: Long,
    val correct: Boolean,
    val durationMs: Long,
)

/**
 * 执行引擎（D-04 v1）：注册表查找 + Dispatchers.Default 线程池隔离 + withTimeout 超时熔断。
 * Phase 2 末期迁移到子进程沙箱，见 docs/04-decisions.md D-04。
 */
object RunEngine {

    /** 执行 [problemId] 的注册解法，[expected] 为 meta.json 中的标准答案用于比对。 */
    suspend fun run(problemId: Int, expected: Long): RunResult {
        val solver = solvers[problemId] ?: throw NoSolverException(problemId)
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
