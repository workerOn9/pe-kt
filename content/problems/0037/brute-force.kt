/**
 * Project Euler 037 — 暴力解（教学对比用）
 *
 * 对每个候选数反复做字符串截取并调用素性判定，截取方式不同但本质相同，
 * 因字符串构造与重复判定而更慢。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun isPrimeLong(n: Long): Boolean {
    if (n < 2) return false
    if (n < 4) return true
    if (n % 2L == 0L) return false
    var d = 3L
    while (d <= n / d) { if (n % d == 0L) return false; d += 2 }
    return true
}

fun solveBruteForce(): Long {
    var sum = 0L; var count = 0; var n = 11
    while (count < 11) {
        val s = n.toString()
        var ok = isPrimeLong(n.toLong())
        for (i in 1 until s.length) {
            if (!isPrimeLong(s.substring(i).toLong())) ok = false
            if (!isPrimeLong(s.substring(0, s.length - i).toLong())) ok = false
        }
        if (ok) { sum += n; count++ }
        n++
    }
    return sum
}

fun main() {
    println(solveBruteForce())
}
