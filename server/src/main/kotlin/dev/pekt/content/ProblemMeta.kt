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
    /**
     * 题号的标准答案。绝大多数题为十进制数字字符串（如 "233168"）；
     * 少数题以题目要求的原文形式存放（如 PE 284 的 base-14 小写字母 "5a411d7b"）。
     * 存量 meta.json 中的 JSON 数字由解析侧的 lenient 模式读入为字符串。
     */
    val answer: String,
    val solvedBy: Long? = null,
    val bruteForceBaselineMs: Double? = null,
    val optimizedBaselineMs: Double? = null,
    val hasVisualization: Boolean = false,
    val sourceUrl: String = "",
    val fetchedAt: String = "",
)
