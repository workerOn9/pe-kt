/**
 * Project Euler 135 — 暴力解（教学对比用）
 *
 * 与 solution.kt 的差距来源：本解不做任何因式分解或因子对推理，直接照题面来 ——
 * 三元组写成 (z + 2d, z + d, z)（z 是最小项、d ≥ 1 是公差），逐个算
 *     n = (z + 2d)² − (z + d)² − z²
 * 把 0 < n < 10⁶ 的解记进计数表；优化解走的是 n = u·v 的因子对枚举（u + v ≡ 0 mod 4、3u > v），
 * 并把有序对折成无序对、一次记完 1 或 2 个解。
 *
 * 截断依据（都不依赖优化解的代数变形，只用到 n 关于 d 的单调性）：
 *   · 固定 z 时 n = 3d² + 2zd − z² 对 d 严格递增（6d + 2z > 0），
 *     故 d 从 n > 0 的最小值 ⌊z/3⌋ + 1 起步，一旦 n ≥ 10⁶ 就可立即停；
 *   · 最小项只需扫到 limit：取 d = ⌊z/3⌋ + 1（写 z = 3q + r）时
 *     n = 12q + 3 + 2r − r² ≥ 4d − 1 = 4q + 3 ≥ z（因 q + 3 ≥ r），
 *     因此每个解的 z ≤ n，凡 n < 10⁶ 的解都不会被漏掉。
 * 彻底朴素地三层枚举三元组是不可行的 —— 公差最大到 250000，量级约 2.5×10¹¹ 次。
 *
 * 复杂度：内层总次数 = 有效解数（1883345）+ 每个 z 一次越界退出判断（999999）= 2883344，空间 O(N)。
 * 相对优化解更贵的地方：每轮都要算三个 Long 的平方差，而不是一次 Int 乘法；
 * 并且最小项这一维保留了完整遍历。
 *
 * 题面样例写成运行时断言：27 恰两解且最小、1155 恰十解且最小。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 按定义逐 (z, d) 统计每个 n（下标即 n）的解数。 */
fun bruteCounts(limit: Int = 1_000_000): IntArray {
    val counts = IntArray(limit)
    var z = 1
    while (z < limit) {
        var d = z / 3 + 1                            // n > 0 的最小公差
        while (true) {
            val x = (z + 2 * d).toLong()
            val y = (z + d).toLong()
            val zl = z.toLong()
            val n = x * x - y * y - zl * zl
            if (n >= limit) break                    // 固定 z 时 n 随 d 严格递增
            counts[n.toInt()]++
            d++
        }
        z++
    }
    return counts
}

fun solveBrute(limit: Int = 1_000_000, target: Int = 10): Int =
    bruteCounts(limit).count { it == target }

fun verifySample() {
    check(34L * 34 - 27L * 27 - 20L * 20 == 27L && 12L * 12 - 9L * 9 - 6L * 6 == 27L)
    val small = bruteCounts(1156)
    check(small[27] == 2) { "27 应恰有 2 个解，实得 ${small[27]}" }
    check((1 until 27).none { small[it] == 2 }) { "27 应是最小的恰两解值" }
    check(small[1155] == 10) { "1155 应恰有 10 个解，实得 ${small[1155]}" }
    check((1 until 1155).none { small[it] == 10 }) { "1155 应是最小的恰十解值" }
    // 诊断信息走 stderr：stdout 的最后一行（也是唯一一行）必须是答案
    System.err.println("1155 的解数 = ${small[1155]}；小于 27 的 n 中没有恰两解的，小于 1155 的 n 中没有恰十解的")
}

fun main() {
    verifySample()
    repeat(3) { solveBrute() }                       // JIT 预热
    val start = System.nanoTime()
    val answer = solveBrute()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
