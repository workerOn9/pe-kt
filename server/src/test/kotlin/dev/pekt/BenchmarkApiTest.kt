package dev.pekt

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 基准报告 API：正文与图表数据都要能取到，且每个数据点恰有 ms 或 error 之一
 * （算不出来的运行时不伪造数值）。
 */
class BenchmarkApiTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `report endpoint returns markdown and chart series`() = testApplication {
        application { module() }
        val response = client.get("/api/benchmarks/languages")
        assertEquals(HttpStatusCode.OK, response.status)

        val report = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("languages", report.getValue("slug").jsonPrimitive.content)
        assertTrue(report.getValue("title").jsonPrimitive.content.isNotBlank())
        assertTrue(report.getValue("markdown").jsonPrimitive.content.isNotBlank())

        val charts = report.getValue("charts").jsonArray
        assertTrue(charts.isNotEmpty(), "报告至少应有一张图表")
        for (chart in charts) {
            val obj = chart.jsonObject
            val id = obj.getValue("id").jsonPrimitive.content
            assertTrue(id.isNotBlank(), "图表 id 为空")
            val series = obj.getValue("series").jsonArray
            assertTrue(series.isNotEmpty(), "图表 $id 没有数据点")
            for (point in series) {
                val entry = point.jsonObject
                assertTrue(
                    entry.getValue("lang").jsonPrimitive.content.isNotBlank(),
                    "图表 $id 存在空的 lang 字段",
                )
                val ms = entry["ms"]
                val error = entry["error"]
                val hasMs = ms != null && ms !is JsonNull
                val hasError = error != null && error !is JsonNull
                assertTrue(hasMs != hasError, "图表 $id 的数据点应恰有 ms 或 error 之一：$entry")
            }
        }
    }

    @Test
    fun `unknown report returns 404 with unified error format`() = testApplication {
        application { module() }
        val response = client.get("/api/benchmarks/no-such-report")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val error = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("not_found", error.getValue("error").jsonPrimitive.content)
        assertTrue(error.containsKey("message"))
    }
}
