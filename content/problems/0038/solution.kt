/**
 * Project Euler 038 — Pandigital Multiples
 *
 * 优化解：枚举基数 n，按 k=1,2,3… 依次拼接 n×k 直到长度达到 9，恰好 9 位且
 * 为 1–9 全数字则记录；n 超过 4 位时 9×n 已超长，可提前终止。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isPandigital19(s: String): Boolean {
    if (s.length != 9) return false
    val seen = BooleanArray(10)
    for (ch in s) { val d = ch - '0'; if (d == 0 || seen[d]) return false; seen[d] = true }
    return true
}

fun solve(): Long {
    var best = 0L
    for (n in 1..9999) {
        val sb = StringBuilder(); var k = 1
        while (sb.length < 9) { sb.append(n * k); k++ }
        if (sb.length == 9 && isPandigital19(sb.toString())) {
            val v = sb.toString().toLong()
            if (v > best) best = v
        }
    }
    return best
}

fun main() {
    println(solve())
}
