/**
 * Project Euler 024 — Lexicographic Permutations
 *
 * 优化解：因子进制（factoradic）逐位确定，O(n²)。
 * 第 k 个排列（1 起始）等价于把 k-1 按 (n-1)!, (n-2)!, ..., 1! 逐位分解，
 * 每一步的商就是当前位在剩余数字中的下标。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(index: Long = 1_000_000): String {
    val remaining = (0..9).toMutableList()
    var k = index - 1                         // 转成 0 起始的序号
    var fact = 1L
    for (i in 2..9) fact *= i                 // 9!，首位的块大小
    val sb = StringBuilder()
    while (remaining.isNotEmpty()) {
        val pick = (k / fact).toInt()         // 越过 pick 个块，落在第 pick 个剩余数字上
        sb.append(remaining.removeAt(pick))
        k %= fact
        if (remaining.isNotEmpty()) fact /= remaining.size
    }
    return sb.toString()
}

fun main() {
    println(solve())
}
