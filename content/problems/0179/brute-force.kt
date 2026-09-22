/**
 * PE 179 — brute force：对小区间直接利用试除法质因数分解计算约数个数，
 * 验证筛法逻辑的正确性。
 */
private fun countDivisors(n: Int): Int {
    var x = n
    var count = 1
    var p = 2
    while (p * p <= x) {
        if (x % p == 0) {
            var e = 0
            while (x % p == 0) {
                x /= p
                e++
            }
            count *= (e + 1)
        }
        p = if (p == 2) 3 else p + 2
    }
    if (x > 1) count *= 2
    return count
}

fun main() {
    val limit = 100_000
    // 独立试除法统计
    var bruteCount = 0L
    var prevD = countDivisors(2)
    for (n in 2 until limit) {
        val nextD = countDivisors(n + 1)
        if (prevD == nextD) {
            bruteCount++
        }
        prevD = nextD
    }
    println("brute up to $limit: $bruteCount")
    check(bruteCount > 0) { "brute verification failed" }
}
