package dev.pekt.routes

import dev.pekt.content.ReportIndex
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/** 报告 slug 不存在（404 not_found）。 */
class ReportNotFoundException(rawSlug: String) :
    RuntimeException("报告不存在：$rawSlug")

/**
 * 报告路由，挂载在 /api/benchmarks 下。报告是内容资产（content/benchmarks/），
 * 前端按 Markdown 正文里的 `[[chart:id]]` 标记插入图表。
 */
fun Route.reportRoutes(index: ReportIndex) {
    route("/api/benchmarks") {
        get("/{slug}") {
            call.guarded {
                val slug = call.parameters["slug"] ?: throw ReportNotFoundException("(missing)")
                val report = index.get(slug) ?: throw ReportNotFoundException(slug)
                call.respond(report)
            }
        }
    }
}
