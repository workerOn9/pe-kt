/**
 * PE 174 — 统计 t<=10^6 中「恰有 n 种薄片拼法」的 t 的个数，n=1..10。
 * 用砖 t = n^2 - m^2（同奇偶），把每个 t 的拼法数计入桶，再按桶大小统计。
 * 已由 Python 实算 = 209566（且 N(15)=832 与题面一致）。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
import kotlin.math.sqrt

const val LIM = 1_000_000

fun solve(): Long {
    val reps = IntArray(LIM + 1)
    var n = 3L                                  // 注意：n^2 最大 6.25e10，超出 Int，必须用 Long
    while (n * n - (n - 2) * (n - 2) <= LIM.toLong()) {
        val need = n * n - LIM
        var lo = if (need <= 1) 1L else {
            var s = sqrt(need.toDouble()).toLong()
            while (s * s < need) s++
            while (s > 1 && (s - 1) * (s - 1) >= need) s--
            s
        }
        if (lo % 2 != n % 2) lo++
        var m = lo
        while (m <= n - 2) {
            reps[(n * n - m * m).toInt()]++
            m += 2
        }
        n++
    }
    val bucket = IntArray(16)
    for (t in 1..LIM) {
        val c = reps[t]
        if (c in 1..15) bucket[c]++
    }
    var total = 0L
    for (k in 1..10) total += bucket[k]
    return total
}

fun main() {
    println(solve())
}
