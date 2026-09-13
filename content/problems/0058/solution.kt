/**
 * Project Euler 058 — Spiral Primes
 *
 * 优化解：只走对角线，不真的构造螺旋。边长为 s（奇数）那一层新增的四个角是
 * s², s²-(s-1), s²-2(s-1), s²-3(s-1)（s² 是奇平方，必为合数，只需判另外三个）。
 * 对角线总数是 2s-1，用「10 × 素数个数 < 总数」判断比例首次跌破 10%，
 * 避免浮点误差。素性用确定性 Miller-Rabin（< 3.2e9 只需底 2,3,5,7），
 * 比逐步试除快两个数量级。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun p058ModPow(base: Long, exp: Long, mod: Long): Long {
    var b = base % mod
    var e = exp
    var r = 1L
    while (e > 0L) {
        if (e and 1L == 1L) r = r * b % mod
        b = b * b % mod
        e = e shr 1
    }
    return r
}

fun p058IsPrime(n: Long): Boolean {
    if (n < 2L) return false
    for (p in longArrayOf(2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L, 29L, 31L, 37L)) {
        if (n == p) return true
        if (n % p == 0L) return false
    }
    var d = n - 1L
    var s = 0
    while (d and 1L == 0L) { d = d shr 1; s++ }
    for (a in longArrayOf(2L, 3L, 5L, 7L)) {
        var x = p058ModPow(a, d, n)
        if (x == 1L || x == n - 1L) continue
        var witness = true
        for (r in 1 until s) {
            x = x * x % n
            if (x == n - 1L) { witness = false; break }
        }
        if (witness) return false
    }
    return true
}

fun solve(): Long {
    var side = 3L
    var primeCount = 0L
    while (true) {
        val corner = side * side
        val step = side - 1L
        for (k in 1..3) if (p058IsPrime(corner - k * step)) primeCount++
        if (10L * primeCount < 2L * side - 1L) return side
        side += 2L
    }
}

fun main() {
    println(solve())
}
