package dev.pekt

import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunApiTest {

    private val json = Json { ignoreUnknownKeys = true }

    /** 题号 → meta.json 中的标准答案（逐一取自 content/problems/00XX/meta.json）。 */
    private val expectedAnswers = mapOf(
        1 to 233168L,
        2 to 4613732L,
        3 to 6857L,
        4 to 906609L,
        5 to 232792560L,
        6 to 25164150L,
        7 to 104743L,
        8 to 23514624000L,
        9 to 31875000L,
        10 to 142913828922L,
        11 to 70600674L,
        12 to 76576500L,
        13 to 5537376230L,
        14 to 837799L,
        15 to 137846528820L,
        16 to 1366L,
        17 to 21124L,
        18 to 1074L,
        19 to 171L,
        20 to 648L,
        21 to 31626L,
        22 to 871198282L,
        23 to 4179871L,
        24 to 2783915460L,
        25 to 4782L,
    )

    @Test
    fun `run registered solvers returns correct answers`() = testApplication {
        application { module() }
        for ((id, expected) in expectedAnswers) {
            val response = client.post("/api/problems/$id/run")
            assertEquals(HttpStatusCode.OK, response.status, "problem $id should run")

            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals(expected, body.getValue("answer").jsonPrimitive.long, "problem $id answer")
            assertEquals(expected, body.getValue("expected").jsonPrimitive.long, "problem $id expected")
            assertTrue(body.getValue("correct").jsonPrimitive.boolean, "problem $id should be correct")
            // 正常题目应在超时上限（10s）内完成，不触发熔断
            assertTrue(body.getValue("durationMs").jsonPrimitive.long < 10_000L, "problem $id duration")
        }
    }

    @Test
    fun `run unknown problem returns 404`() = testApplication {
        application { module() }
        val response = client.post("/api/problems/999/run")
        assertEquals(HttpStatusCode.NotFound, response.status)

        val error = json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("not_found", error.getValue("error").jsonPrimitive.content)
    }
}
