package dev.pekt.problems

/**
 * Problem 199: Iterative Circle Packing
 *
 * 思路：阿波罗尼奥斯垫片（Apollonian gasket），用带符号曲率的笛卡尔定理
 *   (k1+k2+k3+k4)^2 = 2(k1^2+k2^2+k3^2+k4^2)  ⟹  k4 = k1+k2+k3 ± 2√(k1k2+k2k3+k3k1)
 *   大圆曲率取 -1，内部圆取正。每个空隙记录为四元组 (a,b,c,对侧圆)：
 *   两个根中「对侧圆」已知，取另一个即为新填入的圆；新空隙 (a,b,n) 的对侧是 c。
 *   未覆盖面积占比 = 1 - Σ 1/k^2（大圆半径为 1），无需圆心坐标。
 *   校验：第 3 轮 108 空隙、未覆盖率 0.06790342 与题面逐位吻合。
 *
 * 答案：0.00396087（存储为放大 10^8 的整数 396087）
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private class Gap(val a: Double, val b: Double, val c: Double, val across: Double)

fun solve199(): Long {
    val kIn = 1.0 / (2 * Math.sqrt(3.0) - 3)   // 内圆曲率 = 1 + 2/√3
    val kOut = -1.0                            // 大圆（取负）
    var gaps = ArrayList<Gap>(64)
    repeat(3) { gaps.add(Gap(kOut, kIn, kIn, kIn)) }  // 3 个角隙，对侧 = 第三个内圆
    gaps.add(Gap(kIn, kIn, kIn, kOut))                 // 中心隙，对侧 = 大圆
    var area = 3.0 / (kIn * kIn)                // Σ r_i^2，初始三个内圆

    for (iter in 0 until 10) {
        val next = ArrayList<Gap>(gaps.size * 3)
        for (g in gaps) {
            val s = g.a + g.b + g.c
            val sq = Math.sqrt(g.a * g.b + g.b * g.c + g.c * g.a)
            val kp = s + 2 * sq
            val km = s - 2 * sq
            // 两根之一恰为已知的对侧圆，取另一个
            val n = if (Math.abs(kp - g.across) > Math.abs(km - g.across)) kp else km
            area += 1.0 / (n * n)
            next.add(Gap(g.a, g.b, n, g.c))
            next.add(Gap(g.b, g.c, n, g.a))
            next.add(Gap(g.c, g.a, n, g.b))
        }
        gaps = next
    }
    val frac = 1.0 - area
    return Math.round(frac * 1e8)
}

fun main() {
    var best = Long.MAX_VALUE
    var ans = 0L
    repeat(5) {
        val t = System.nanoTime()
        ans = solve199()
        best = minOf(best, System.nanoTime() - t)
    }
    println("answer=$ans formatted=0.${ans.toString().padStart(8, '0')} bestMs=${"%.2f".format(best / 1e6)}")
}
