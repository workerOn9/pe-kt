/**
 * PE 179 — 1 < n < 10^7 中满足 d(n) = d(n+1) 的整数个数。
 *
 * 原理：埃氏倍数筛法直接累加约数贡献。
 * 开辟大小为 10^7 + 1 的 IntArray（占内存约 40MB），
 * 对每个 i 从 1 到 10^7，将其倍数位置 j = i, 2i, 3i, ... 计数加 1。
 * 筛完后，线性扫描 2 .. 10^7 - 1，统计 d[n] == d[n+1] 的个数。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
fun solve179(limit: Int = 10_000_000): Long {
    val d = IntArray(limit + 1)
    for (i in 1..limit) {
        var j = i
        while (j <= limit) {
            d[j]++
            j += i
        }
    }
    var count = 0L
    for (n in 2 until limit) {
        if (d[n] == d[n + 1]) {
            count++
        }
    }
    return count
}

fun main() {
    // 题面样例：n = 14 时 d(14) == d(15) == 4
    val small = solve179(20)
    check(small > 0) { "must find matches below 20 (including 14)" }
    val answer = solve179()
    println(answer)
}
