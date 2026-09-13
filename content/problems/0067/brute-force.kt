import java.io.File

/**
 * Project Euler 067 — 暴力解（教学对比用）
 *
 * 100 行三角形共有 2⁹⁹ 条路径，直接枚举不可能（这正是题目的考点）；
 * 这里给出最朴素的自顶向下递归 + 记忆化：嵌套 List 取值、HashMap 查表、函数调用，
 * 与 solution.kt 的自底向上原地数组 DP 是同一递推、不同常数与空间代价。
 * 需从题目目录运行（读取同目录的 triangle.txt）：
 *   kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(path: String = "triangle.txt"): Long {
    val tri = File(path).readLines()
        .filter { it.isNotBlank() }
        .map { line -> line.trim().split(Regex("\\s+")).map(String::toInt) }
    val memo = HashMap<Long, Long>()
    fun best(r: Int, c: Int): Long {
        if (r == tri.size - 1) return tri[r][c].toLong()
        val key = r.toLong() * 1000L + c
        val cached = memo[key]
        if (cached != null) return cached
        val v = tri[r][c] + maxOf(best(r + 1, c), best(r + 1, c + 1))
        memo[key] = v
        return v
    }
    return best(0, 0)
}

fun main() {
    println(solveBruteForce())
}
