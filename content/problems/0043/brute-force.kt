/**
 * Project Euler 043 — 暴力解（教学对比用）
 *
 * 用字典序全排列枚举 0–9 的全部 10! 个排列，再逐个校验 7 条整除条件。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    val primes = intArrayOf(0, 0, 0, 2, 3, 5, 7, 11, 13, 17)
    val perm = IntArray(10) { it }
    var sum = 0L
    fun nextPermutation(a: IntArray): Boolean {
        var i = a.size - 2
        while (i >= 0 && a[i] >= a[i + 1]) i--
        if (i < 0) return false
        var j = a.size - 1
        while (a[j] <= a[i]) j--
        val t = a[i]; a[i] = a[j]; a[j] = t
        var l = i + 1; var r = a.size - 1
        while (l < r) { val x = a[l]; a[l] = a[r]; a[r] = x; l++; r-- }
        return true
    }
    do {
        if (perm[0] == 0) continue
        var ok = true
        for (i in 3..9) {
            val num = perm[i - 2] * 100 + perm[i - 1] * 10 + perm[i]
            if (num % primes[i] != 0) { ok = false; break }
        }
        if (ok) { var v = 0L; for (x in perm) v = v * 10 + x; sum += v }
    } while (nextPermutation(perm))
    return sum
}

fun main() {
    println(solveBruteForce())
}
