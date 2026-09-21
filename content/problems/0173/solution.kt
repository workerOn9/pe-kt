/**
 * PE 173 — 正方形薄片计数（最多 10^6 块砖）。
 * 外边长 n、孔边长 m（m<n 且与 n 同奇偶），用砖 t = n^2 - m^2。
 * 对每个 n，满足 t<=LIMIT 的 m 是一个前缀区间，用平方根求下界即可 O(1) 计数。
 * 已由 Python 实算 = 1572729（且 ≤100 块得 41 种，与题面一致）。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
import kotlin.math.sqrt

const val LIMIT = 1_000_000L

fun solve(): Long {
    var total = 0L
    var n = 3L
    while (n * n - (n - 2) * (n - 2) <= LIMIT) {
        val need = n * n - LIMIT
        var lo = if (need <= 1) 1L else {
            var s = sqrt(need.toDouble()).toLong()
            while (s * s < need) s++
            while (s > 1 && (s - 1) * (s - 1) >= need) s--
            s
        }
        if (lo % 2 != n % 2) lo++
        if (lo <= n - 2) total += (n - 2 - lo) / 2 + 1
        n++
    }
    return total
}

fun main() {
    println(solve())
}
