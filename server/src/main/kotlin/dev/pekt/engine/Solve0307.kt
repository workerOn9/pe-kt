package dev.pekt.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * PE 307 — Chip Defects（芯片缺陷）：k 个缺陷独立等概率分布在 n 块芯片上，
 * p(k,n) = 「存在某块芯片带 ≥3 个缺陷」的概率。求 p(20000, 1000000)，四舍五入到 10 位小数。
 *
 * 本题答案不是整数（0.7311720251），故返回 String 而非 Long：
 * 四舍五入到 10 位小数的定长小数字符串，恰好 10 位小数。
 *
 * 推导（详见 content/problems/0307/solution.kt 头部与 0307/analysis.md）：
 *   总分配数 k^n（缺陷可区分），补事件「每块芯片至多 2 个缺陷」的单芯片 EGF 为 f(x) = 1 + x + x²/2，
 *   方案数 = k!·[x^k] f(x)^n。令 g = f^n = Σ c_j x^j，由 f·g' = n·f'·g 比较 x^j 系数得
 *
 *     (j+1)·c_{j+1} = (n-j)·c_j + (n - (j-1)/2)·c_{j-1},   c_0 = 1, c_{-1} = 0
 *
 *   两边乘 j! 化成整数递推（A_j = j!·c_j = j 个可区分缺陷投到 n 块芯片、每块至多 2 个的方案数）：
 *
 *     A_{j+1} = (n-j)·A_j + [j(2n-j+1)/2]·A_{j-1},   A_0 = 1, A_1 = n
 *
 *   自检 j=1：A_2 = (n-1)n + n = n²，与「2 个缺陷任投 n 块共 n² 种」一致；j=0：A_1 = n。
 *   于是 p(k,n) = 1 - A_k / n^k。
 *
 * 数值：n^k = 10^120000、k! ≈ 10^77326，量级远超 64 位；直接构造大整数再相除慢且难保精度。
 * BigDecimal 内部即「尾数 × 10^-scale」的归一化表示，MathContext(40) 固定 40 位有效数字而 scale 可任意大，
 * 故 A_k 与 n^k 都只留 40 位有效数字即可，不溢出也不丢位。
 *
 * 最终答案（实测输出）：p(20000, 1000000) ≈ 0.731172025128296 → "0.7311720251"。
 */
internal fun solve0307Impl(): String {
    val mc = MathContext(40)
    val n = 1_000_000
    val k = 20_000

    var aPrev = BigDecimal.ONE                       // A_0
    var aCur = BigDecimal(n)                         // A_1
    for (j in 1 until k) {
        val coeff = j.toLong() * (2L * n - j + 1L) / 2L      // j(2n-j+1)/2，恒整数
        val next = BigDecimal((n - j).toLong()).multiply(aCur, mc)
            .add(BigDecimal(coeff).multiply(aPrev, mc), mc)
        aPrev = aCur
        aCur = next
    }

    val good = aCur                                  // A_k = c_k · k!
    val total = BigDecimal(n).pow(k, mc)             // n^k = 10^120000
    val p = BigDecimal.ONE.subtract(good.divide(total, mc), mc)
    return p.setScale(10, RoundingMode.HALF_UP).toPlainString()
}
