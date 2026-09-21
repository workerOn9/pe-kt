/**
 * PE 174 — brute force：对小块数上限逐对 (n,m) 枚举并统计次数，与优化解在小规模上对拍。
 */
fun countsUpTo(limit: Int): Map<Int, Int> {
    val reps = HashMap<Int, Int>()
    var n = 3
    while (n * n - (n - 2) * (n - 2) <= limit) {
        var m = n - 2
        while (m >= 1) {
            val t = n * n - m * m
            if (t <= limit) reps[t] = (reps[t] ?: 0) + 1
            m -= 2
        }
        n++
    }
    return reps
}

fun main() {
    val reps = countsUpTo(20000)
    val bucket = HashMap<Int, Int>()
    for (c in reps.values) bucket[c] = (bucket[c] ?: 0) + 1
    println("limit=20000 时 N(1..5) = " + (1..5).map { bucket[it] ?: 0 })
}
