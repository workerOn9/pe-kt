/**
 * Project Euler 032 — Pandigital Products
 *
 * 优化解：由位数约束直接锁定两种拆分：1 位数×4 位数=4 位数、2 位数×3 位数=4 位数；
 * 只需枚举这两类组合并做 1–9 全数字校验，乘积入集合去重。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isPandigital19(s: String): Boolean {
    if (s.length != 9) return false
    val seen = BooleanArray(10)
    for (ch in s) { val d = ch - '0'; if (d == 0 || seen[d]) return false; seen[d] = true }
    return true
}

fun solve(): Long {
    val products = HashSet<Int>()
    for (a in 1..9) for (b in 1000..9999) {
        val p = a * b
        if (p in 1000..9999 && isPandigital19("$a$b$p")) products.add(p)
    }
    for (a in 10..99) for (b in 100..999) {
        val p = a * b
        if (p in 1000..9999 && isPandigital19("$a$b$p")) products.add(p)
    }
    return products.sumOf { it.toLong() }
}

fun main() {
    println(solve())
}
