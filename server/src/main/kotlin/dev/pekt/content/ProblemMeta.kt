package dev.pekt.content

import kotlinx.serialization.Serializable

/**
 * `content/problems/XXXX/meta.json` 的反序列化模型。
 *
 * 字段名与 meta.json 保持一致；可选字段提供默认值以容错，
 * 未知字段由调用方配置 `ignoreUnknownKeys = true` 忽略。
 */
@Serializable
data class ProblemMeta(
    val id: Int,
    val title: String,
    val titleZh: String = "",
    val difficulty: Int = 0,
    val difficultyLevel: String = "",
    val tags: List<String> = emptyList(),
    val answer: Long,
    val solvedBy: Long? = null,
    val bruteForceBaselineMs: Double? = null,
    val optimizedBaselineMs: Double? = null,
    val hasVisualization: Boolean = false,
    val sourceUrl: String = "",
    val fetchedAt: String = "",
)
