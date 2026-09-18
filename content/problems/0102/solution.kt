/**
 * Project Euler 102 — Triangle Containment（三角形内含原点）
 *
 * 优化解：三角形是三个内侧半平面的交集。原点严格位于内部，当且仅当
 * A×B、B×C、C×A 三个行列式全正或全负；行列式为 0 表示原点落在边的直线上，必须排除。
 * 坐标用 Long 乘法避免溢出；按行读取数据并校验六个坐标与总行数。
 * 复杂度：O(N) 时间、O(1) 额外空间（流式读取 N=1000 行，每行 6 次乘法、3 次减法）。
 * 需从仓库根目录、server 或题目目录运行（读取 triangles.txt）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun containsOrigin(t: LongArray): Boolean {
    val ab = t[0] * t[3] - t[1] * t[2]
    val bc = t[2] * t[5] - t[3] * t[4]
    val ca = t[4] * t[1] - t[5] * t[0]
    return (ab > 0 && bc > 0 && ca > 0) || (ab < 0 && bc < 0 && ca < 0)
}

fun solve(): Long {
    val file = listOf("content/problems/0102/triangles.txt", "triangles.txt", "../content/problems/0102/triangles.txt")
        .map { java.io.File(it) }.firstOrNull { it.isFile }
        ?: error("找不到 triangles.txt，请在仓库根目录、server 或题目目录运行")
    var rows = 0
    var count = 0L
    file.useLines { lines ->
        lines.forEach { line ->
            val t = line.split(',').map { it.trim().toLong() }.toLongArray()
            require(t.size == 6 && t.all { it in -1000L..1000L }) { "无效三角形坐标：$line" }
            if (containsOrigin(t)) count++
            rows++
        }
    }
    check(rows == 1000) { "应有 1000 个三角形，实际 $rows" }
    return count
}

fun main() { println(solve()) }
