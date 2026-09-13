/**
 * Project Euler 041 — Pandigital Prime
 *
 * 优化解：8 位、9 位全数字数的数位和分别为 36、45，必被 3 整除，因此答案至多是 7 位；
 * 从 7654321 递减，先验全数字再判素，首个命中即最大者。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isPandigitalN(n: Int): Boolean {
    val s = n.toString()
    val k = s.length
    val seen = BooleanArray(10)
    for (ch in s) { val d = ch - '0'; if (d == 0 || d > k || seen[d]) return false; seen[d] = true }
    return true
}

fun isPrimeLong(n: Long): Boolean {
    if (n < 2) return false
    if (n < 4) return true
    if (n % 2L == 0L) return false
    var d = 3L
    while (d <= n / d) { if (n % d == 0L) return false; d += 2 }
    return true
}

fun solve(): Long {
    // 8、9 位全数字数的数位和为 36、45，必被 3 整除，故答案至多 7 位
    for (n in 7_654_321 downTo 1) {
        if (!isPandigitalN(n)) continue
        if (isPrimeLong(n.toLong())) return n.toLong()
    }
    return 0
}

fun main() {
    println(solve())
}
