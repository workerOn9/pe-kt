/**
 * Project Euler 026 — Reciprocal Cycles
 *
 * 优化解：只枚举素数分母：1/d 的循环节长度等于 10 模 d（约去 2、5 因子后）的乘法阶，
 * 合数的阶整除其素因子幂的阶，故最大值必在素数处取得。从 999 向下搜索，
 * 用 10^(p-1) ≡ 1 (mod p) 的因式消去法求阶，阶 ≤ p-1 时可提前剪枝。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun sieveBool(limit: Int): BooleanArray {
    val composite = BooleanArray(limit + 1)
    val result = BooleanArray(limit + 1) { it >= 2 }
    if (limit < 2) return result
    var p = 2
    while (p.toLong() * p <= limit) {
        if (!composite[p]) {
            var m = p * p
            while (m <= limit) { composite[m] = true; m += p }
        }
        p++
    }
    for (i in 2..limit) result[i] = !composite[i]
    return result
}

fun distinctPrimeFactors(n0: Long): List<Long> {
    var n = n0; val out = ArrayList<Long>(); var d = 2L
    while (d * d <= n) {
        if (n % d == 0L) { out.add(d); while (n % d == 0L) n /= d }
        d++
    }
    if (n > 1) out.add(n)
    return out
}

fun modPowL(base: Long, exp: Long, mod: Long): Long {
    var b = base % mod; var e = exp; var r = 1L
    while (e > 0) { if (e and 1L == 1L) r = r * b % mod; b = b * b % mod; e = e shr 1 }
    return r
}

fun solve(): Long {
    val isP = sieveBool(1000)
    var bestD = 0; var bestLen = 0
    for (d in 999 downTo 2) {
        if (!isP[d]) continue
        if (d - 1 <= bestLen) break
        var order = d - 1L
        for (q in distinctPrimeFactors(order)) {
            while (order % q == 0L && modPowL(10L, order / q, d.toLong()) == 1L) order /= q
        }
        if (order > bestLen) { bestLen = order.toInt(); bestD = d }
    }
    return bestD.toLong()
}

fun main() {
    println(solve())
}
