/**
 * Project Euler 038 — 暴力解（教学对比用）
 *
 * 枚举上界放宽到 99999 且不限制拼接结果长度，靠全数字判定自然过滤。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun isPandigital19(s: String): Boolean {
    if (s.length != 9) return false
    val seen = BooleanArray(10)
    for (ch in s) { val d = ch - '0'; if (d == 0 || seen[d]) return false; seen[d] = true }
    return true
}

fun solveBruteForce(): Long {
    var best = 0L
    for (n in 1..99999) {
        val sb = StringBuilder(); var k = 1
        while (sb.length < 9) { sb.append(n * k); k++ }
        if (isPandigital19(sb.toString())) {
            val v = sb.toString().toLong()
            if (v > best) best = v
        }
    }
    return best
}

fun main() {
    println(solveBruteForce())
}
