package dev.pekt

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProblemsApiTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `list returns all problems sorted by id`() = testApplication {
        application { module() }
        val response = client.get("/api/problems")
        assertEquals(HttpStatusCode.OK, response.status)

        val problems = json.parseToJsonElement(response.bodyAsText()).jsonArray
        assertTrue(problems.size >= 100, "题库应至少 100 题，实际 ${problems.size}")
        val ids = problems.map { it.jsonObject.getValue("id").jsonPrimitive.int }
        assertEquals((1..problems.size).toList(), ids)
        // 列表项不含正文
        assertTrue(problems.none { "statement" in it.jsonObject || "analysis" in it.jsonObject })
    }

    @Test
    fun `detail contains statement and meta title`() = testApplication {
        application { module() }
        val response = client.get("/api/problems/1")
        assertEquals(HttpStatusCode.OK, response.status)

        val detail = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals(
            "Multiples of 3 or 5",
            detail.getValue("meta").jsonObject.getValue("title").jsonPrimitive.content,
        )
        assertTrue(detail.getValue("statement").jsonPrimitive.content.isNotBlank())
        assertTrue(detail.getValue("analysis").jsonPrimitive.content.isNotBlank())
    }

    @Test
    fun `unknown problem returns 404 with unified error format`() = testApplication {
        application { module() }
        val response = client.get("/api/problems/999")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val error = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("not_found", error.getValue("error").jsonPrimitive.content)
        assertTrue(error.containsKey("message"))
    }

    @Test
    fun `solution endpoint returns source containing sumOfMultiples`() = testApplication {
        application { module() }
        val response = client.get("/api/problems/1/solution")
        assertEquals(HttpStatusCode.OK, response.status)

        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertTrue(body.getValue("solution").jsonPrimitive.content.contains("sumOfMultiples"))
    }
}
