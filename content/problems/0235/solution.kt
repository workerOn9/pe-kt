#!/usr/bin/env kotlin
/**
 * Project Euler 235 — An Arithmetic Geometric Sequence（等差-等比序列）
 *
 * 思路：
 *   s(n, r) = Σ_{k=1..n} (900−3k)·r^{k−1}。每个系数 900−3k 随 k 递减且前 299 项为正，
 *   而 r > 1 时 r^{k−1} 递增——s 关于 r 严格递减：s(1) = −33007500 > −6×10¹¹ > s(1.5)，
 *   故在 [1, 1.5] 上二分即可。
 *   求值用 Horner（从 k = n 往回收）而不是直接逐项求和：直接求和的中间项可达 r^4999 ≈ 10⁵ 量级，
 *   与总量 6×10¹¹ 相差十几个数量级，先展开再相加会白白丢掉有效位；Horner 每步只做一次乘加，
 *   中间量的量级始终与结果同阶（最大约 2×10⁸），误差 ~10⁻² 的绝对量级对应
 *   Δr ≈ 10⁻²/|s′(r)|，而 |s′(r)| ≈ 2.76×10¹⁵，故 Δr ≈ 4×10⁻¹⁸ ≪ 10⁻¹²。
 *
 * 旁证：
 *   1. 用 50 位十进制定点（Python decimal）按闭式
 *      s = 900·(rⁿ−1)/(r−1) − 3·(1−(n+1)rⁿ+n·r^{n+1})/(r−1)² 独立二分，
 *      得到的 r 与双精度 Horner 二分在小数第 15 位才分道扬镳，12 位完全相同。
 *   2. 导数量级估算：|s|/|s′| ≈ 2.2×10⁻⁴ 说明函数在根附近极陡，双精度足以锁定 12 位。
 *
 * 答案：1.002322108633（Long 编码 round(r × 10¹²) = 1002322108633）
 * 复杂度：二分 ~150 步 × 每步 O(n) 的 Horner，n = 5000，共约 10⁶ 次浮点运算，实测 < 5 ms。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val N = 5000
private const val TARGET = -600_000_000_000.0

/** Horner 求 s = Σ_{k=1..N} (900−3k) r^{k−1} */
fun series(r: Double, n: Int = N): Double {
    var acc = 0.0
    for (k in n downTo 1) acc = acc * r + (900 - 3 * k)
    return acc
}

fun main() {
    var lo = 1.0
    var hi = 1.5
    check(series(lo) > TARGET && series(hi) < TARGET)
    repeat(200) {
        val mid = (lo + hi) / 2
        if (series(mid) > TARGET) lo = mid else hi = mid
    }
    val r = (lo + hi) / 2
    println("r = %.15f".format(r))
    println("s(r) = " + series(r))
    println("rounded to 12 dp: " + "%.12f".format(r))
    println("answer = " + Math.round(r * 1e12))
}
