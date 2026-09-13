/**
 * Project Euler 086 — Cuboid Route（暴力对照解）
 *
 * 思路：完全不利用「最短路径只由 a+b 与 c 决定」的化简，而是对每个候选最大棱长 M，把 a ≤ b ≤ M 的组合同步枚举，
 * 对每组直接算出 (a+b)²+M² 是否为完全平方数（等价于检验展开后的最短路径是否有整数长度）。
 * 复杂度：O(M³)，M=1818 时约 10^9 次判定。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 */

fun solveBruteForce(): Long {
    val target = 1_000_000L
    var count = 0L
    var maxSide = 0L
    while (count <= target) {
        maxSide++
        var a = 1L
        while (a <= maxSide) {
            var b = a
            while (b <= maxSide) {
                val m = a + b
                val v = m * m + maxSide * maxSide
                val r = Math.sqrt(v.toDouble()).toLong()
                if (r * r == v) count++
                b++
            }
            a++
        }
    }
    return maxSide
}

fun main() {
    println(solveBruteForce())
}
