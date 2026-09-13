/**
 * Project Euler 093 — Arithmetic Expressions
 *
 * 暴力解：对每组四个数字，枚举 4! = 24 种排列、4³ = 64 种运算符组合、以及 4 个操作数的
 * 5 种本质不同的括号结构，共 24·64·5 = 7680 个表达式逐一求值（运算用精确分数，
 * 除以零返回 null 直接丢弃），把正整数结果收进集合，再从头数连续段。
 *
 * 复杂度：时间 O(C(10,4) · 4!·4³·5)，空间 O(1)
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

private data class FracB(val n: Long, val d: Long)

private fun gcdB(a: Long, b: Long): Long {
    var x = if (a < 0) -a else a
    var y = if (b < 0) -b else b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return if (x == 0L) 1L else x
}

private fun fracB(num: Long, den: Long): FracB? {
    if (den == 0L) return null
    var a = num
    var b = den
    if (b < 0) {
        a = -a
        b = -b
    }
    val g = gcdB(a, b)
    return FracB(a / g, b / g)
}

private fun opB(op: Int, x: FracB?, y: FracB?): FracB? {
    if (x == null || y == null) return null
    return when (op) {
        0 -> fracB(x.n * y.d + y.n * x.d, x.d * y.d)
        1 -> fracB(x.n * y.d - y.n * x.d, x.d * y.d)
        2 -> fracB(x.n * y.n, x.d * y.d)
        else -> fracB(x.n * y.d, x.d * y.n)
    }
}

private fun nextPerm(p: IntArray): Boolean {
    var i = p.size - 2
    while (i >= 0 && p[i] >= p[i + 1]) i--
    if (i < 0) return false
    var j = p.size - 1
    while (p[j] <= p[i]) j--
    val t = p[i]; p[i] = p[j]; p[j] = t
    var lo = i + 1
    var hi = p.size - 1
    while (lo < hi) {
        val u = p[lo]; p[lo] = p[hi]; p[hi] = u
        lo++
        hi--
    }
    return true
}

private fun consecutiveForDigits(digits: IntArray): Int {
    val collect = HashSet<Long>()
    val p = intArrayOf(0, 1, 2, 3)
    do {
        val v = arrayOf<FracB?>(
            fracB(digits[p[0]].toLong(), 1L), fracB(digits[p[1]].toLong(), 1L),
            fracB(digits[p[2]].toLong(), 1L), fracB(digits[p[3]].toLong(), 1L),
        )
        for (o1 in 0..3) for (o2 in 0..3) for (o3 in 0..3) {
            val results = listOf(
                opB(o3, opB(o2, opB(o1, v[0], v[1]), v[2]), v[3]), // ((a∘b)∘c)∘d
                opB(o3, opB(o1, v[0], opB(o2, v[1], v[2])), v[3]), // (a∘(b∘c))∘d
                opB(o2, opB(o1, v[0], v[1]), opB(o3, v[2], v[3])), // (a∘b)∘(c∘d)
                opB(o1, v[0], opB(o3, opB(o2, v[1], v[2]), v[3])), // a∘((b∘c)∘d)
                opB(o1, v[0], opB(o2, v[1], opB(o3, v[2], v[3]))), // a∘(b∘(c∘d))
            )
            for (r in results) if (r != null && r.d == 1L && r.n > 0L) collect.add(r.n)
        }
    } while (nextPerm(p))
    var k = 1L
    while (collect.contains(k)) k++
    return (k - 1L).toInt()
}

fun solveBruteForce(): Long {
    var bestLen = -1
    var bestCode = 0L
    for (a in 0..9) for (b in a + 1..9) for (c in b + 1..9) for (d in c + 1..9) {
        val len = consecutiveForDigits(intArrayOf(a, b, c, d))
        if (len > bestLen) {
            bestLen = len
            bestCode = a * 1000L + b * 100L + c * 10L + d
        }
    }
    return bestCode
}

fun main() {
    println(solveBruteForce())
}
