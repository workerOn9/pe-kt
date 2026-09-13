package dev.pekt

import dev.pekt.content.ContentIndex
import dev.pekt.content.ProblemMeta
import dev.pekt.engine.RunEngine
import dev.pekt.engine.solvers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 内容资产校验：遍历 content/problems/ 每个题目目录，保证
 * 目录命名、meta.json 字段、必备文件、meta.id 一致性全部成立；
 * 有注册解法的题目实跑一遍引擎，断言结果与 meta.answer 一致。
 */
class ContentValidationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val problemsDir: File by lazy {
        File(ContentIndex.resolveContentDir(), "problems").also {
            assertTrue(it.isDirectory, "problems 目录不存在：${it.absolutePath}")
        }
    }

    private val problemDirs: List<File> by lazy {
        problemsDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedBy { it.name }
            .orEmpty()
            .also { assertTrue(it.isNotEmpty(), "content/problems 下没有任何题目目录") }
    }

    private val metas: Map<File, ProblemMeta> by lazy {
        problemDirs.associateWith { dir ->
            val metaFile = File(dir, "meta.json")
            assertTrue(metaFile.isFile, "${dir.name}: 缺 meta.json")
            json.decodeFromString<ProblemMeta>(metaFile.readText())
        }
    }

    @Test
    fun `目录数量不少于 25 且目录名为四位数字`() {
        assertTrue(problemDirs.size >= 25, "content/problems 下至少有 25 个题目目录（v1 基线），实际 ${problemDirs.size}")
        for (dir in problemDirs) {
            assertTrue(dir.name.matches(Regex("\\d{4}")), "目录名须为四位数字：${dir.name}")
        }
    }

    @Test
    fun `meta 字段完整且 id 与目录名一致`() {
        for ((dir, meta) in metas) {
            assertTrue(meta.id > 0, "${dir.name}: id 必须为正数")
            assertTrue(meta.title.isNotBlank(), "${dir.name}: title 为空")
            assertTrue(meta.titleZh.isNotBlank(), "${dir.name}: titleZh 为空")
            assertTrue(meta.tags.isNotEmpty(), "${dir.name}: tags 为空")
            val dirNumber = dir.name.toInt()
            assertEquals(dirNumber, meta.id, "${dir.name}: meta.id 与目录名数字不一致")
        }
        // id 全局唯一
        val ids = metas.values.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "meta.id 存在重复")
    }

    @Test
    fun `statement、analysis、solution 文件存在且非空`() {
        for (dir in problemDirs) {
            for (name in listOf("statement.md", "analysis.md", "solution.kt")) {
                val file = File(dir, name)
                assertTrue(file.isFile, "${dir.name}: 缺 $name")
                assertTrue(file.readText().isNotBlank(), "${dir.name}: $name 内容为空")
            }
            // applications.md（现实应用）为可选板块；存在时必须非空
            val applications = File(dir, "applications.md")
            if (applications.isFile) {
                assertTrue(applications.readText().isNotBlank(), "${dir.name}: applications.md 内容为空")
            }
        }
    }

    /**
     * 显示公式围栏（`$$`）两端必须独占一行。`remark-math` 沿用 micromark 的代码围栏语义：
     * `$$` 后若在同一行跟了内容，那段内容会被当作围栏信息串丢弃，公式块还会一路吞到下一个
     * 独占一行的 `$$`，整页渲染成红字原文（0065 等 7 题踩过）。单行 `$$…$$` 合法但解析为
     * 行内公式，`\begin{align}`、`\tag` 这类只能用于 display 的命令不能用在这个形式里。
     * 前端 [web/src/components/Markdown.tsx] 已按同一规则兜底规范化，这条校验守住内容的规范写法。
     * `statement.en.md` 是题面原文存档、前端不渲染，不参与校验。
     */
    @Test
    fun `显示公式围栏两端独占一行`() {
        val renderedNames = listOf("statement.md", "analysis.md", "applications.md")
        val reports = File(ContentIndex.resolveContentDir(), "benchmarks")
            .listFiles().orEmpty().filter { it.isFile && it.name.endsWith(".md") }
        val files = problemDirs.flatMap { dir -> renderedNames.map { File(dir, it) } } + reports

        val violations = files.filter { it.isFile }.flatMap { file ->
            displayMathFenceViolations(file.readText()).map { "${file.path}：$it" }
        }
        assertTrue(
            violations.isEmpty(),
            "显示公式围栏写法不合规，`$$` 需两端独占一行：\n" + violations.joinToString("\n"),
        )
    }

    /** 返回不合规描述（含行号）；代码围栏内的内容不参与。 */
    private fun displayMathFenceViolations(text: String): List<String> {
        val violations = mutableListOf<String>()
        var inCodeFence = false
        var inDisplayMath = false
        text.lineSequence().forEachIndexed { index, raw ->
            val line = raw.trim()
            val lineNo = index + 1
            when {
                inCodeFence -> if (line.startsWith("```") || line.startsWith("~~~")) inCodeFence = false
                line.startsWith("```") || line.startsWith("~~~") -> inCodeFence = true
                inDisplayMath -> if (line.endsWith("$$")) {
                    if (line != "$$") violations += "第 $lineNo 行收尾的 `$$` 后面必须独占一行（块内不能有别的字）"
                    inDisplayMath = false
                }
                !line.startsWith("$$") -> Unit
                line == "$$" -> inDisplayMath = true
                line.length > 4 && line.endsWith("$$") -> Unit // 单行 $$…$$：按行内公式渲染，可接受
                else -> {
                    violations += "第 $lineNo 行开头的 `$$` 后面不能跟内容"
                    inDisplayMath = true
                }
            }
        }
        if (inDisplayMath) violations += "文件末尾有未收尾的显示公式块"
        return violations
    }

    @Test
    fun `已注册解法的答案与引擎实跑结果一致`() = runBlocking {
        for ((dir, meta) in metas) {
            if (meta.id !in solvers) continue // 无 solver 的题目跳过（允许）
            val result = RunEngine.run(meta.id, meta.answer)
            assertTrue(
                result.correct,
                "${dir.name}: 引擎答案 ${result.answer} != meta.answer ${meta.answer}（耗时 ${result.durationMs}ms）",
            )
        }
    }

    /**
     * 基准报告（content/benchmarks/）正文里的 `[[chart:id]]` 标记与 charts.json 的图表 id
     * 必须互为子集：缺任一方向都会在页面上留下空洞占位或死数据。
     */
    @Test
    fun `报告正文的图表标记与图表清单一一对应`() {
        val benchmarksDir = File(ContentIndex.resolveContentDir(), "benchmarks")
        if (!benchmarksDir.isDirectory) return
        val markerPattern = Regex("""\[\[chart:([A-Za-z0-9_-]+)]]""")
        val reports = benchmarksDir.listFiles().orEmpty().filter { it.isFile && it.name.endsWith(".md") }
        assertTrue(reports.isNotEmpty(), "content/benchmarks 下没有报告文件")

        for (md in reports) {
            val slug = md.name.removeSuffix(".md")
            val chartsFile = File(benchmarksDir, "$slug.charts.json")
            val declared = if (chartsFile.isFile) {
                json.parseToJsonElement(chartsFile.readText()).jsonObject["charts"]
                    ?.jsonArray
                    ?.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.content }
                    ?.toSet()
                    .orEmpty()
            } else {
                emptySet()
            }
            val referenced = markerPattern.findAll(md.readText())
                .map { it.groupValues[1] }
                .toSet()

            assertTrue(
                referenced - declared == emptySet<String>(),
                "$slug: 正文引用了未定义的图表 ${referenced - declared}",
            )
            assertTrue(
                declared - referenced == emptySet<String>(),
                "$slug: charts.json 里的图表没有被正文引用 ${declared - referenced}",
            )
        }
    }
}
