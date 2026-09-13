/**
 * Project Euler 090 — Cube Digit Pairs（暴力解）
 *
 * 暴力思路：不做「无序骰子对 i ≤ j」的化简，直接枚举全部 210 × 210 个有序骰子对，
 * 对每个满足条件的对，把规范化后的键（两个掩码排序后拼成字符串）塞进集合去重，
 * 最后数集合大小。多做一倍判定，但代码不需要事先想清「谁是第一个骰子」。
 *
 * 复杂度：O(C(10,6)² × 9) ≈ 4×10^5 次判定，实测 &lt; 5 ms。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d bf.jar && java -jar bf.jar
 */

private val b090Squares = listOf(
    0 to 1, 0 to 4, 0 to 9, 1 to 6, 2 to 5, 3 to 6, 4 to 9, 6 to 4, 8 to 1,
)

private fun b090Has(mask: Int, d: Int): Boolean {
    if ((mask shr d) and 1 == 1) return true
    return (d == 6 || d == 9) && ((mask shr (15 - d)) and 1 == 1)
}

private fun b090Ok(a: Int, b: Int): Boolean {
    for ((x, y) in b090Squares) {
        if ((b090Has(a, x) && b090Has(b, y)) || (b090Has(a, y) && b090Has(b, x))) continue
        return false
    }
    return true
}

fun solveBruteForce(): Long {
    val sets = (0 until 1024).filter { Integer.bitCount(it) == 6 }
    val keys = HashSet<String>()
    for (a in sets) for (b in sets) {
        if (b090Ok(a, b)) {
            val lo = minOf(a, b)
            val hi = maxOf(a, b)
            keys.add("$lo-$hi")
        }
    }
    return keys.size.toLong()
}

fun main() {
    println(solveBruteForce())
}
