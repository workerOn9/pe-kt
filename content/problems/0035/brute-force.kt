/**
 * Project Euler 035 — 暴力解（教学对比用）
 *
 * 对 1..999999 的每个素数都做全部旋转并判定（没有按位预筛），旋转判定次数更多。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
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

fun solveBruteForce(): Long {
    val N = 1_000_000
    val isP = sieveBool(N)
    var count = 0L
    for (n in 2 until N) if (isCircularPrime(n, isP)) count++
    return count
}

fun main() {
    println(solveBruteForce())
}
