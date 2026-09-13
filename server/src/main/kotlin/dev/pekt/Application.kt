package dev.pekt

import dev.pekt.content.ContentIndex
import dev.pekt.content.ReportIndex
import dev.pekt.routes.problemRoutes
import dev.pekt.routes.reportRoutes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.options
import io.ktor.server.routing.routing
import java.io.File

fun main() {
    embeddedServer(Netty, port = resolvePort(), host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

/**
 * 监听端口：本地开发默认 8080；容器平台注入 `PORT` 时以其为准。
 * Vercel 容器服务的流量默认打向 80 端口，`Dockerfile.vercel` 因此声明 `PORT=80`。
 */
private fun resolvePort(): Int = System.getenv("PORT")?.toIntOrNull() ?: 8080

/**
 * 宽松 CORS：允许任意来源，方便前端 Vite 开发服务器跨域。
 * ktor-server-cors 不在依赖清单中，这里用自定义插件手工加响应头。
 */
private val PermissiveCors = createApplicationPlugin("PermissiveCors") {
    onCall { call ->
        call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
        call.response.header(HttpHeaders.AccessControlAllowMethods, "GET, POST, OPTIONS")
        call.response.header(HttpHeaders.AccessControlAllowHeaders, "Content-Type, Authorization")
    }
}

/**
 * 前端构建产物目录定位（单端口部署，D-07）：环境变量 `PEKT_WEB_DIST` 优先，
 * 否则逐一探测候选相对路径（覆盖从仓库根 / server/ / Gradle 深层工作目录启动的场景）。
 * 返回 null 表示未找到（开发模式：前端走 Vite 开发服务器，不启用静态托管）。
 */
private fun resolveWebDistDir(env: Map<String, String> = System.getenv()): File? {
    env["PEKT_WEB_DIST"]?.takeIf { it.isNotBlank() }?.let { return File(it).canonicalFile }
    return listOf("web/dist", "../web/dist", "../../web/dist")
        .map { File(it) }
        .firstOrNull { dir -> dir.isDirectory && File(dir, "index.html").isFile }
        ?.canonicalFile
}

fun Application.module() {
    // 内容索引：启动时扫描加载（D-05：v1 无数据库）
    val contentIndex = ContentIndex.load()
    // 报告索引：content/benchmarks/ 下的 Markdown 报告 + 图表数据
    val reportIndex = ReportIndex.load()

    install(ContentNegotiation) {
        json()
    }
    install(PermissiveCors)

    // 注：统一错误格式 { "error": "code", "message": "..." } 由 routes 包内的 guarded {} 处理
    // （ktor-server-status-pages 不在依赖清单中，不新增依赖）。

    routing {
        get("/health") {
            call.respondText("""{"status":"ok"}""", ContentType.Application.Json)
        }
        // CORS 预检请求统一放行
        options("/{...}") {
            call.respond(HttpStatusCode.OK)
        }
        problemRoutes(contentIndex)
        reportRoutes(reportIndex)

        // 单端口部署（D-07）：找到前端构建产物时，在 8080 直接托管 web/dist。
        // 注意：Ktor 3.1.3 的 staticFiles("/", dir) 在根路径下无法匹配子路径
        // （实测被通配路由抢走，见 git 记录），因此用显式通配路由手工服务静态文件：
        // 存在的文件直接返回，不存在则回退 index.html（SPA fallback）。
        // Ktor 路由常量段（/health、/api/...）优先于通配段，API 路由不受影响。
        val webDist = resolveWebDistDir()
        if (webDist != null) {
            val indexHtml = File(webDist, "index.html")
            get("/") {
                call.respondFile(indexHtml)
            }
            get("/{path...}") {
                val relative = call.parameters.getAll("path").orEmpty().joinToString("/")
                val file = File(webDist, relative).canonicalFile
                if (relative.isNotBlank() && file.isFile && file.path.startsWith(webDist.path)) {
                    call.respondFile(file)
                } else {
                    call.respondFile(indexHtml)
                }
            }
            log.info("静态托管已启用：{}", webDist.absolutePath)
        } else {
            log.info("未找到 web/dist（可设 PEKT_WEB_DIST 指定），静态托管未启用（开发模式）")
        }
    }
}
