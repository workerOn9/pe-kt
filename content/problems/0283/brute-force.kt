#!/usr/bin/env kotlin
/**
 * Project Euler 283 — Integer Sided Triangles with Integral Area/perimeter Ratio
 * 暴力 / 对照实现（与 solution.kt 的约数驱动主路径不共享任何核心代码）
 *
 * 路径 1（窗口暴力，全尺寸；meta 的 bruteForceBaselineMs 口径）
 * ────────────────────────────────────────────────────────
 * 直接照方程 r²(x+y+z) = xyz 解出 z：固定 r = 2k、x 后，
 *   z = r²(x+y)/(xy − r²)，
 * 其中 y 的窗口为 [max(x, ⌈r²/x⌉), (r² + r√(r²+x²))/x]（下界来自 xy ≥ r²，上界来自 z ≥ y）。
 * 逐个 y 试整除并累加周长 2(x+y+z)。窗口长度之和 ≈ Σ_{k≤1000} Σ_x r²/x ≈ 1.15×10^10：
 * 这是「全尺寸可行但很慢」的暴力（本机 3 轮最优 10.4 s），与主路径的约数枚举完全不同。
 *
 * 路径 2（定义级 Heron 枚举，P ≤ 2000）
 * ───────────────────────────────────
 * 完全按定义枚举 a ≤ b ≤ c、周长 P ≤ 2000 的整数边三角形：16A² = P(P−2a)(P−2b)(P−2c) 判整数
 * 面积，A ≡ 0 (mod P) 判面积/周长为正整数。规模外推：三角形数 ≈ P³/144，P = 2000 时约 5.5×10^7
 * 次判定（实测 92.8 ms）；本题全尺寸周长上界达 3.2×10^13（x = 1 的解），定义级枚举不可行，
 * 故它只钉住小周长范围（与 solution.kt 主路径限制到同一范围的周长多重集逐个比对）。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0283/brute-force.kt -d <目录>
 *      java -Xmx2g -cp <目录>:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _：JVM facade 类名是 Brute_forceKt）
 */

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

private const val K_MAX = 1000
private const val SMALL_K = 200
private const val SMALL_P = 2000L
private const val ANSWER = 28038042525570324L

private fun isqrt(n: Long): Long {
    var s = sqrt(n.toDouble()).toLong()
    while (s > 0 && s * s > n) s--
    while ((s + 1) * (s + 1) <= n) s++
    return s
}

/** 路径 1：窗口暴力（方程直接解 z），返回 (解数, 周长之和, 内层迭代次数)。 */
private fun windowBrute(kMax: Int): Triple<Long, Long, Long> {
    var total = 0L
    var count = 0L
    var iters = 0L
    for (k in 1..kMax) {
        val r = 2L * k
        val r2 = r * r
        var x = 1L
        while (x * x <= 3 * r2) {
            var y = max(x, (r2 + x - 1) / x)
            val yMax = (r2 + isqrt(r2 * (r2 + x * x))) / x + 1     // z ≥ y 的上界（+1 兜住取整）
            while (y <= yMax) {
                val a = x * y - r2
                if (a > 0) {
                    iters++
                    val num = r2 * (x + y)
                    if (num % a == 0L) {
                        val z = num / a
                        if (z >= y) {
                            total += 2 * (x + y + z)
                            count++
                        }
                    }
                }
                y++
            }
            x++
        }
    }
    return Triple(count, total, iters)
}

/** 路径 2：定义级 Heron 枚举（周长 ≤ pMax），返回 (解数, 周长之和, 周长列表)。 */
private fun heronBrute(pMax: Long): Triple<Long, Long, List<Long>> {
    var total = 0L
    var count = 0L
    val perims = ArrayList<Long>()
    var a = 1L
    while (3 * a <= pMax) {
        var b = a
        while (a + 2 * b <= pMax) {
            val cMax = min(pMax - a - b, a + b - 1)
            var c = b
            while (c <= cMax) {
                val p = a + b + c
                if (p % 2 == 0L) {                                    // P 奇 ⟹ 16A² 奇，无整数面积
                    val s = p / 2
                    val q = s * (s - a) * (s - b) * (s - c)           // = A²
                    var area = sqrt(q.toDouble()).toLong()
                    while (area > 0 && area * area > q) area--
                    while ((area + 1) * (area + 1) <= q) area++
                    if (area * area == q && area % p == 0L) {
                        val kk = area / p
                        if (kk in 1..1000L) {
                            total += p
                            count++
                            perims.add(p)
                        }
                    }
                }
                c++
            }
            b++
        }
        a++
    }
    return Triple(count, total, perims)
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("  $tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 路径 1：全尺寸窗口暴力（独立算法，全尺寸可行） ----------
    val first = windowBrute(K_MAX)
    println("路径 1（窗口暴力，k ≤ $K_MAX）：解数 ${first.first}，周长之和 ${first.second}，" +
        "内层迭代 ${first.third} 次")
    check(first.second == ANSWER) { "窗口暴力 ${first.second} ≠ $ANSWER" }
    windowBrute(K_MAX)
    val msWindow = bestOf3("窗口暴力（全尺寸 k ≤ $K_MAX，内层 1.14×10^10 次判定）", ANSWER) {
        windowBrute(K_MAX).second
    }

    // 小规模锚点：k ≤ 200 的解数与周长和（与 solution.kt 主路径同一口径的独立复现）
    val small = windowBrute(SMALL_K)
    check(small.first == 222_609L && small.second == 9_050_416_878_652L) {
        "k ≤ $SMALL_K 窗口暴力结果异常：${small.first} / ${small.second}"
    }
    val msSmall = bestOf3("窗口暴力（k ≤ $SMALL_K）", 9_050_416_878_652L) { windowBrute(SMALL_K).second }

    // ---------- 路径 2：定义级 Heron 枚举（小周长范围） ----------
    val heron = heronBrute(SMALL_P)
    println("路径 2（Heron 定义级枚举，P ≤ $SMALL_P）：解数 ${heron.first}，周长之和 ${heron.second}，" +
        "含样例 24 = ${heron.third.contains(24L)}，含 42 = ${heron.third.contains(42L)}")
    check(heron.first == 4631L && heron.second == 5_691_262L) {
        "Heron P ≤ $SMALL_P 结果异常：${heron.first} / ${heron.second}"
    }
    check(heron.third.contains(24L) && heron.third.contains(42L)) { "Heron 结果缺少题面样例周长" }
    val msHeron = bestOf3("Heron 定义级枚举（P ≤ $SMALL_P）", 5_691_262L) {
        heronBrute(SMALL_P).second
    }

    // ---------- 汇总 ----------
    println()
    println("全尺寸窗口暴力总和 = ${first.second}（期望 $ANSWER）")
    println("汇总：窗口暴力 ${"%.1f".format(msWindow)} ms（全尺寸 k ≤ $K_MAX）；" +
        "窗口暴力 ${"%.1f".format(msSmall)} ms（k ≤ $SMALL_K）；" +
        "Heron 定义级 ${"%.1f".format(msHeron)} ms（P ≤ $SMALL_P）")
    println("check() 全部通过")
}
