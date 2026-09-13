/**
 * Project Euler 035 — Circular Primes
 *
 * 优化解：先做筛法再逐个旋转检查；多位数的循环素数不可能含偶数位或 5，
 * 先按位快速排除，再对剩下的候选做旋转素性判定。
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

fun isCircularPrime(n: Int, isP: BooleanArray): Boolean {
    if (!isP[n]) return false
    val s = n.toString()
    for (i in 1 until s.length) {
        val r = s.substring(i) + s.substring(0, i)
        if (!isP[r.toInt()]) return false
    }
    return true
}

fun solve(): Long {
    val N = 1_000_000
    val isP = sieveBool(N)
    var count = 0L
    for (n in 2 until N) {
        val s = n.toString()
        if (s.length > 1 && s.any { (it - '0') % 2 == 0 || it == '5' }) continue
        if (isCircularPrime(n, isP)) count++
    }
    return count
}

fun main() {
    println(solve())
}
