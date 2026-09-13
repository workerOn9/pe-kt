/**
 * Project Euler 090 — Cube Digit Pairs
 *
 * 思路：一个骰子面集合就是 {0..9} 的 6 元子集，用 10 位掩码表示，共 C(10,6)=210 个。
 * 6 与 9 可翻转，所以「某骰子能否显示数字 d」= 该位已置，或 d∈{6,9} 且另一位(9↔6)已置。
 * 9 个平方数各自要求一对数字，两骰子谁放十位谁放个位都可。骰子作为有序的两个面集合
 * 已在题意中被视为同一种摆放，故只枚举无序骰子对 i ≤ j 并逐一验证。
 *
 * 复杂度：O(C(10,6)^2 × 9) ≈ 2×10^5 次判定，实测 &lt; 1 ms。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private val p090Squares = listOf(
    0 to 1, 0 to 4, 0 to 9, 1 to 6, 2 to 5, 3 to 6, 4 to 9, 6 to 4, 8 to 1,
)

/** 掩码 mask 的骰子能否摆出数字 d（6/9 可翻转）。 */
private fun p090Has(mask: Int, d: Int): Boolean {
    if ((mask shr d) and 1 == 1) return true
    return (d == 6 || d == 9) && ((mask shr (15 - d)) and 1 == 1)
}

private fun p090Ok(a: Int, b: Int): Boolean {
    for ((x, y) in p090Squares) {
        if ((p090Has(a, x) && p090Has(b, y)) || (p090Has(a, y) && p090Has(b, x))) continue
        return false
    }
    return true
}

fun solve(): Long {
    val sets = (0 until 1024).filter { Integer.bitCount(it) == 6 }
    var count = 0L
    for (i in sets.indices) for (j in i until sets.size) if (p090Ok(sets[i], sets[j])) count++
    return count
}

fun main() {
    println(solve())
}
