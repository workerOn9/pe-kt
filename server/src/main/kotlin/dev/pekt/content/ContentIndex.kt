package dev.pekt.content

import kotlinx.serialization.json.Json
import java.io.File

/**
 * 单题完整内容：元数据 + Markdown 正文 + 源码文本。
 * 缺失文件容错：statement/analysis/solution 缺失时为空串，bruteForce 缺失时为 null（如 0003）；
 * applications（现实应用）为可选板块，缺失为 null，前端不渲染该模块。
 */
data class ProblemContent(
    val meta: ProblemMeta,
    val statement: String,
    val analysis: String,
    val solution: String,
    val bruteForce: String?,
    val applications: String?,
)

/**
 * 内容索引：启动时扫描 content/problems/ 下所有题目目录并加载进内存（D-05：v1 无数据库）。
 */
class ContentIndex private constructor(private val problems: Map<Int, ProblemContent>) {

    /** 题目列表（仅元数据），按 id 升序。 */
    fun list(): List<ProblemMeta> = problems.values.map { it.meta }.sortedBy { it.id }

    /** 按题号取完整内容，不存在返回 null。 */
    fun get(id: Int): ProblemContent? = problems[id]

    companion object {
        /**
         * 内容根目录候选相对路径（按序探测）：
         * 覆盖从 server/ 子项目目录（`../content`）、更深层工作目录（`../../content`，如 Gradle 测试）
         * 以及仓库根目录（`content`）启动的场景。
         */
        private val CANDIDATE_DIRS = listOf("../content", "../../content", "content")

        /**
         * 定位内容根目录：优先环境变量 `PEKT_CONTENT_DIR`，否则逐一探测 [CANDIDATE_DIRS]。
         * 探测标准是目录存在且含 `problems/` 子目录。
         */
        fun resolveContentDir(env: Map<String, String> = System.getenv()): File {
            env["PEKT_CONTENT_DIR"]?.takeIf { it.isNotBlank() }?.let { return File(it).canonicalFile }
            return CANDIDATE_DIRS
                .map { File(it) }
                .firstOrNull { dir -> dir.isDirectory && File(dir, "problems").isDirectory }
                ?.canonicalFile
                ?: error("未找到 content 目录，请设置环境变量 PEKT_CONTENT_DIR（已尝试：$CANDIDATE_DIRS）")
        }

        /** 扫描 [root]/problems 加载全部题目；缺 meta.json 或 meta.json 解析失败的目录跳过。 */
        fun load(root: File = resolveContentDir()): ContentIndex {
            val json = Json { ignoreUnknownKeys = true }
            val problemsDir = File(root, "problems")
            val problems = problemsDir.listFiles()
                ?.filter { it.isDirectory }
                .orEmpty()
                .mapNotNull { dir ->
                    val metaFile = File(dir, "meta.json")
                    if (!metaFile.isFile) return@mapNotNull null
                    val meta = runCatching { json.decodeFromString<ProblemMeta>(metaFile.readText()) }
                        .getOrElse { return@mapNotNull null }
                    ProblemContent(
                        meta = meta,
                        statement = File(dir, "statement.md").takeIf { it.isFile }?.readText().orEmpty(),
                        analysis = File(dir, "analysis.md").takeIf { it.isFile }?.readText().orEmpty(),
                        solution = File(dir, "solution.kt").takeIf { it.isFile }?.readText().orEmpty(),
                        bruteForce = File(dir, "brute-force.kt").takeIf { it.isFile }?.readText(),
                        applications = File(dir, "applications.md").takeIf { it.isFile }?.readText(),
                    )
                }
                .associateBy { it.meta.id }
            return ContentIndex(problems)
        }
    }
}
