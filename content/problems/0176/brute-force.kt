/**
 * PE 176 — brute force：对给定 b 直接枚举全部因子对 (u,v)（uv=b²，u<v，
 * 同奇偶），数出三角形个数；与公式 (d(b²)−1)/2 / (d(b²/4)−1)/2 对拍。
 */
private fun countTrianglesBrute(b: Long): Long {
    val sq = b * b
    var cnt = 0L
    var u = 1L
    while (u * u < sq) {
        if (sq % u == 0L) {
            val v = sq / u
            if (u < v && (u + v) % 2 == 0L) cnt++
        }
        u++
    }
    return cnt
}

fun main() {
    // 对拍：暴力计数 vs 公式
    fun d2(n: Long): Int {
        var x = n; var cnt = 1L; var p = 2L
        while (p * p <= x) {
            if (x % p == 0L) {
                var e = 0
                while (x % p == 0L) { x /= p; e++ }
                cnt *= (2 * e + 1)
            }
            p += if (p == 2L) 1L else 2L
        }
        if (x > 1L) cnt *= 3
        return cnt.toInt()
    }
    fun countFormula(b: Long): Long =
        if (b % 2 == 1L) (d2(b) - 1L) / 2 else {
            var x = b / 2 * (b / 2); var cnt = 1; var p = 2L
            while (p * p <= x) {
                if (x % p == 0L) {
                    var e = 0
                    while (x % p == 0L) { x /= p; e++ }
                    cnt *= (e + 1)
                }
                p += if (p == 2L) 1L else 2L
            }
            if (x > 1L) cnt *= 2
            (cnt - 1L) / 2
        }
    for (b in 1L..400L) {
        val a = countTrianglesBrute(b)
        val f = countFormula(b)
        check(a == f) { "mismatch at b=$b: brute=$a formula=$f" }
    }
    println("all b in 1..400 match")
}
