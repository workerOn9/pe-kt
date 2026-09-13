package dev.pekt.content

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/** 图表中的一条序列；`ms` 与 `error` 恰有其一（后者表示该语言/运行时不支持该规模）。 */
@Serializable
data class ChartSeries(
    val lang: String,
    val ms: Double? = null,
    val error: String? = null,
)

/** 一张基准图表：同一任务下各语言/运行时的实测耗时。 */
@Serializable
data class ReportChart(
    val id: String,
    val title: String,
    val unit: String = "ms",
    val note: String = "",
    val series: List<ChartSeries> = emptyList(),
)

/** 一份报告：元信息 + Markdown 正文 + 图表数据（正文用 `[[chart:id]]` 标记图表插入点）。 */
@Serializable
data class ReportContent(
    val slug: String,
    val title: String,
    val subtitle: String,
    val measuredAt: String,
    val machine: String,
    val markdown: String,
    val charts: List<ReportChart>,
)

/** `content/benchmarks/<slug>.charts.json` 的结构。 */
@Serializable
private data class ReportManifest(
    val slug: String = "",
    val title: String = "",
    val subtitle: String = "",
    val measuredAt: String = "",
    val machine: String = "",
    val charts: List<ReportChart> = emptyList(),
)

/**
 * 报告索引：启动时扫描 `content/benchmarks/` 下的 Markdown 报告，
 * 每个 `<slug>.md` 可配一份同名 `<slug>.charts.json` 提供图表数据。
 * 缺清单文件时正文仍可渲染（图表为空，标题回退到 Markdown 首个一级标题）。
 */
class ReportIndex private constructor(private val reports: Map<String, ReportContent>) {

    /** 按 slug 取报告，不存在返回 null。 */
    fun get(slug: String): ReportContent? = reports[slug]

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** 扫描 [root]/benchmarks 加载全部报告；目录缺失或无报告时返回空索引。 */
        fun load(root: File = ContentIndex.resolveContentDir()): ReportIndex {
            val dir = File(root, "benchmarks")
            if (!dir.isDirectory) return ReportIndex(emptyMap())
            val reports = dir.listFiles()
                ?.filter { it.isFile && it.name.endsWith(".md") }
                .orEmpty()
                .sortedBy { it.name }
                .mapNotNull { markdownFile ->
                    val slug = markdownFile.name.removeSuffix(".md")
                    val manifest = File(dir, "$slug.charts.json")
                        .takeIf { it.isFile }
                        ?.let { runCatching { json.decodeFromString<ReportManifest>(it.readText()) }.getOrNull() }
                    val markdown = markdownFile.readText().takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    ReportContent(
                        slug = slug,
                        title = manifest?.title?.takeIf { it.isNotBlank() } ?: firstHeading(markdown) ?: slug,
                        subtitle = manifest?.subtitle.orEmpty(),
                        measuredAt = manifest?.measuredAt.orEmpty(),
                        machine = manifest?.machine.orEmpty(),
                        markdown = markdown,
                        charts = manifest?.charts.orEmpty(),
                    )
                }
                .associateBy { it.slug }
            return ReportIndex(reports)
        }

        private fun firstHeading(markdown: String): String? =
            markdown.lineSequence().firstOrNull { it.startsWith("# ") }?.removePrefix("# ")?.trim()
    }
}
