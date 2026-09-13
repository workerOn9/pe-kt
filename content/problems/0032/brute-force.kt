/**
 * Project Euler 032 — 暴力解（教学对比用）
 *
 * 不利用位数约束，直接枚举 1..99 与 1..9999 的所有乘数对，暴力拼接字符串判定。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun isPandigital19(s: String): Boolean {
    if (s.length != 9) return false
    val seen = BooleanArray(10)
    for (ch in s) { val d = ch - '0'; if (d == 0 || seen[d]) return false; seen[d] = true }
    return true
}

fun solveBruteForce(): Long {
    val products = HashSet<Int>()
    for (a in 1..99) for (b in 1..9999) {
        val p = a.toLong() * b
        if (p > 99999L) continue
        if (isPandigital19("$a$b$p")) products.add(p.toInt())
    }
    return products.sumOf { it.toLong() }
}

fun main() {
    println(solveBruteForce())
}
