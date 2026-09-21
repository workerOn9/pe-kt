/**
 * PE 169 — brute force：直接枚举所有「每个 2 的幂至多用两次」的表示。
 * 只对小 n 可行（方案数增长快），用于与递推对拍：f(10)=5、f(1..10)=[1,1,2,1,3,2,3,1,4,3]。
 */
fun bruteForce(n: Long): Long {
    fun rec(rem: Long, maxPow: Long): Long {
        if (rem == 0L) return 1L
        if (maxPow <= 0) return 0L
        var count = 0L
        for (used in 0..2) {                       // 当前幂用 0/1/2 次
            val take = maxPow * used
            if (take > rem) break
            count += rec(rem - take, maxPow / 2)
        }
        return count
    }
    var top = 1L
    while (top * 2 <= n) top *= 2
    return rec(n, top)
}

fun main() {
    for (n in 1L..10L) print(bruteForce(n).toString() + " ")
    println()
    println("f(10) = " + bruteForce(10))
}
