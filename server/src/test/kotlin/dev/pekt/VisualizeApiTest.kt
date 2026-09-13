package dev.pekt

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VisualizeApiTest {

    private val json = Json { ignoreUnknownKeys = true }

    /** 请求可视化端点并做协议级基础校验（version=1、kind、steps 非空、≤300）。 */
    private suspend fun getVisualization(client: HttpClient, id: Int, kind: String): JsonObject {
        val response = client.get("/api/problems/$id/visualize")
        assertEquals(HttpStatusCode.OK, response.status, "problem $id should have visualization")

        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(1, body.getValue("version").jsonPrimitive.int, "problem $id version")
        assertEquals(id, body.getValue("problemId").jsonPrimitive.int, "problem $id problemId")
        assertEquals(kind, body.getValue("kind").jsonPrimitive.content, "problem $id kind")
        val steps = body.getValue("steps").jsonArray
        assertTrue(steps.isNotEmpty(), "problem $id steps should be non-empty")
        assertTrue(steps.size <= 300, "problem $id steps ${steps.size} exceeds protocol limit 300")
        // 步骤序号从 0 开始连续编号
        steps.forEachIndexed { i, step ->
            assertEquals(i, step.jsonObject.getValue("i").jsonPrimitive.int, "problem $id step index")
        }
        return body
    }

    /** 重放所有 steps 的 updates，返回 grid 单元格最终状态（index → state）。 */
    private fun replayGridStates(body: JsonObject): Map<Int, String> {
        val states = mutableMapOf<Int, String>()
        for (step in body.getValue("steps").jsonArray) {
            for (update in step.jsonObject.getValue("updates").jsonArray) {
                val u = update.jsonObject
                states[u.getValue("index").jsonPrimitive.int] = u.getValue("state").jsonPrimitive.content
            }
        }
        return states
    }

    @Test
    fun `visualize 007 sieve returns grid1d with 25 primes`() = testApplication {
        application { module() }
        val body = getVisualization(client, 7, "grid1d")

        assertEquals(10, body.getValue("scene").jsonObject.getValue("columns").jsonPrimitive.int)
        assertEquals(99, body.getValue("scene").jsonObject.getValue("cells").jsonArray.size)

        // 重放步骤后，prime 状态单元格数 = 100 以内素数个数 = 25
        val states = replayGridStates(body)
        assertEquals(25, states.values.count { it == "prime" }, "100 以内应有 25 个素数")
    }

    @Test
    fun `visualize 014 collatz returns sequence starting at 27`() = testApplication {
        application { module() }
        val body = getVisualization(client, 14, "sequence")

        val scene = body.getValue("scene").jsonObject
        assertEquals(27L, scene.getValue("start").jsonPrimitive.long)

        // 27 → 1 共 111 步转移，加上起点 append 共 112 步；最后一个 append 值为 1
        val steps = body.getValue("steps").jsonArray
        assertEquals(112, steps.size)
        val appends = steps.map {
            it.jsonObject.getValue("updates").jsonArray.single().jsonObject.getValue("append").jsonPrimitive.long
        }
        assertEquals(27L, appends.first())
        assertEquals(1L, appends.last())
        assertEquals(9232L, appends.max(), "轨道峰值应为 9232")
    }

    @Test
    fun `visualize 011 grid max product returns grid2d with answer window on path`() = testApplication {
        application { module() }
        val body = getVisualization(client, 11, "grid2d")

        val scene = body.getValue("scene").jsonObject
        assertEquals(20, scene.getValue("rows").jsonPrimitive.int)
        assertEquals(false, scene.getValue("triangle").jsonPrimitive.boolean)
        assertEquals(400, scene.getValue("cells").jsonArray.size)

        // 重放步骤后，onPath 格子恰好 4 个（答案窗口）
        val states = replayGridStates(body)
        val onPath = states.filterValues { it == "onPath" }.keys
        assertEquals(4, onPath.size, "最终应只有答案窗口的 4 格在 onPath")

        // 答案窗口 4 格的值之积 = 原题答案 70600674（"看到的 = 真实执行的"）
        val valueByIndex = scene.getValue("cells").jsonArray.associate {
            it.jsonObject.getValue("index").jsonPrimitive.int to it.jsonObject.getValue("value").jsonPrimitive.long
        }
        assertEquals(70600674L, onPath.fold(1L) { acc, i -> acc * valueByIndex.getValue(i) })
    }

    @Test
    fun `visualize 015 lattice paths returns grid2d with f(0,0) label 70`() = testApplication {
        application { module() }
        val body = getVisualization(client, 15, "grid2d")

        val scene = body.getValue("scene").jsonObject
        assertEquals(5, scene.getValue("rows").jsonPrimitive.int, "4×4 网格共 5×5 个格点")
        assertEquals(false, scene.getValue("triangle").jsonPrimitive.boolean)
        assertEquals(25, scene.getValue("cells").jsonArray.size)

        // 重放所有 steps 的 label 更新，顶点 (0,0)（index 0）最终 DP 值 = C(8,4) = 70
        val labels = mutableMapOf<Int, String>()
        for (step in body.getValue("steps").jsonArray) {
            for (update in step.jsonObject.getValue("updates").jsonArray) {
                val u = update.jsonObject
                u["label"]?.let { labels[u.getValue("index").jsonPrimitive.int] = it.jsonPrimitive.content }
            }
        }
        assertEquals("70", labels[0], "顶点 (0,0) 最终 label 应为 70")

        // 重放步骤后起点 (0,0) 在 onPath
        val states = replayGridStates(body)
        assertEquals("onPath", states[0])
    }

    @Test
    fun `visualize 018 path dp returns grid2d with 15-cell optimal path summing to 1074`() = testApplication {
        application { module() }
        val body = getVisualization(client, 18, "grid2d")

        val scene = body.getValue("scene").jsonObject
        assertEquals(15, scene.getValue("rows").jsonPrimitive.int)
        assertEquals(true, scene.getValue("triangle").jsonPrimitive.boolean)

        // 重放步骤后 onPath 单元格恰好 15 个（每行一个）
        val states = replayGridStates(body)
        val onPath = states.filterValues { it == "onPath" }.keys
        assertEquals(15, onPath.size)

        // 路径上单元格的值之和 = 原题答案 1074（"看到的 = 真实执行的"）
        val valueByIndex = scene.getValue("cells").jsonArray.associate {
            it.jsonObject.getValue("index").jsonPrimitive.int to it.jsonObject.getValue("value").jsonPrimitive.long
        }
        assertEquals(1074L, onPath.sumOf { valueByIndex.getValue(it) })
    }

    @Test
    fun `visualize problem without generator returns 404 no_visualization`() = testApplication {
        application { module() }
        val response = client.get("/api/problems/1/visualize")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val error = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("no_visualization", error.getValue("error").jsonPrimitive.content)
        assertTrue(error.containsKey("message"))
    }

    @Test
    fun `visualize unknown problem returns 404 not_found`() = testApplication {
        application { module() }
        val response = client.get("/api/problems/999/visualize")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val error = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("not_found", error.getValue("error").jsonPrimitive.content)
    }
}
