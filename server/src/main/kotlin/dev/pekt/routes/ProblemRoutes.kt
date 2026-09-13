package dev.pekt.routes

import dev.pekt.content.ContentIndex
import dev.pekt.content.ProblemContent
import dev.pekt.content.ProblemMeta
import dev.pekt.engine.NoSolverException
import dev.pekt.engine.RunEngine
import dev.pekt.engine.SolverTimeoutException
import dev.pekt.visualize.Visualization
import dev.pekt.visualize.VisualizationJson
import dev.pekt.visualize.Visualizations
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.log
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/** 统一错误格式：{ "error": "code", "message": "..." }（见 docs/02-architecture.md）。 */
@Serializable
data class ApiError(val error: String, val message: String)

/** 题目不存在或题号非法（404 not_found）。 */
class ProblemNotFoundException(rawId: String) :
    RuntimeException("题目不存在：$rawId")

/** 题目存在但未注册可视化生成器（404 no_visualization）。 */
class NoVisualizationException(id: Int) :
    RuntimeException("题目 $id 暂无可视化")

/** GET /api/problems/{id} 详情：meta + 题面/解析全文 + 可选现实应用板块。 */
@Serializable
data class ProblemDetail(
    val meta: ProblemMeta,
    val statement: String,
    val analysis: String,
    /** 现实应用 Markdown；无 applications.md 时为 null，前端不渲染该模块 */
    val applications: String?,
)

/** GET /api/problems/{id}/solution 响应：参考实现源码 + 可选暴力解源码。 */
@Serializable
data class SolutionResponse(
    val id: Int,
    val solution: String,
    val bruteForce: String?,
)

/**
 * 题目相关路由，挂载在 /api/problems 下。
 */
fun Route.problemRoutes(index: ContentIndex) {
    route("/api/problems") {
        // 题目列表：仅 meta 全字段（不含正文），按 id 升序
        get {
            call.guarded { call.respond(index.list()) }
        }

        // 题目详情：meta + statement + analysis 全文
        get("/{id}") {
            call.guarded {
                val content = call.requireProblem(index)
                call.respond(ProblemDetail(content.meta, content.statement, content.analysis, content.applications))
            }
        }

        // 参考实现源码（bruteForce 缺失时为 null，如 0003）
        get("/{id}/solution") {
            call.guarded {
                val content = call.requireProblem(index)
                call.respond(SolutionResponse(content.meta.id, content.solution, content.bruteForce))
            }
        }

        // 执行求解：进程内 + 超时熔断（D-04 v1）
        post("/{id}/run") {
            call.guarded {
                val content = call.requireProblem(index)
                call.respond(RunEngine.run(content.meta.id, content.meta.answer))
            }
        }

        // 可视化分步数据（协议 v1，docs/06-visualization-protocol.md）
        get("/{id}/visualize") {
            call.guarded {
                val content = call.requireProblem(index)
                val generator = Visualizations.visualizations[content.meta.id]
                    ?: throw NoVisualizationException(content.meta.id)
                val visualization = generator()
                check(visualization.steps.size <= Visualization.MAX_STEPS) {
                    "可视化步骤数 ${visualization.steps.size} 超过协议上限"
                }
                // 用 explicitNulls=false 的专用 Json 序列化（不影响全局 ContentNegotiation 配置）
                call.respondText(VisualizationJson.encodeToString(visualization), ContentType.Application.Json)
            }
        }
    }
}

/**
 * 统一错误处理（替代 StatusPages——该插件不在依赖清单中，不新增依赖）。
 * 把已知异常映射为统一错误格式与对应状态码，未知异常记日志并返回 500。
 */
internal suspend fun ApplicationCall.guarded(block: suspend () -> Unit) {
    try {
        block()
    } catch (e: ProblemNotFoundException) {
        respond(HttpStatusCode.NotFound, ApiError("not_found", e.message ?: "resource not found"))
    } catch (e: ReportNotFoundException) {
        respond(HttpStatusCode.NotFound, ApiError("not_found", e.message ?: "report not found"))
    } catch (e: NoVisualizationException) {
        respond(HttpStatusCode.NotFound, ApiError("no_visualization", e.message ?: "no visualization"))
    } catch (e: NoSolverException) {
        respond(HttpStatusCode.NotImplemented, ApiError("no_solver", e.message ?: "no solver registered"))
    } catch (e: SolverTimeoutException) {
        respond(HttpStatusCode.GatewayTimeout, ApiError("timeout", e.message ?: "solver timed out"))
    } catch (e: Throwable) {
        application.log.error("Unhandled exception", e)
        respond(HttpStatusCode.InternalServerError, ApiError("internal", e.message ?: "internal error"))
    }
}

/** 解析路径参数 id 并查索引；非法或不存在统一抛 [ProblemNotFoundException]（404）。 */
private fun RoutingCall.requireProblem(index: ContentIndex): ProblemContent {
    val raw = parameters["id"] ?: throw ProblemNotFoundException("(missing)")
    val id = raw.toIntOrNull() ?: throw ProblemNotFoundException(raw)
    return index.get(id) ?: throw ProblemNotFoundException(raw)
}
