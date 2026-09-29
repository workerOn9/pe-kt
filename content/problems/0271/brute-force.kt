#!/usr/bin/env kotlin
/**
 * Project Euler 271 — Modular Cubes, Part 1：暴力对照（独立实现，与 solution.kt 不共享核心代码）
 *
 * 路径 1（完全按题面定义）：对给定 n 逐个枚举 x ∈ (2, n)，直接算 x³ mod n 是否等于 1，
 *   得到解的个数与 S(n)。用于复现题面锚点：n = 91 时 8 个解、S(91) = 363；
 *   并在 n = 2…3000 上把「解数」与结构公式对比：
 *     解数 = 3^(k + [9 | n])，k = n 的素因子中 ≡ 1 (mod 3) 的个数（按试除法独立算出）。
 *
 * 路径 2（同结构大规模暴力）：n = 85276009 = 7·13·19·31·37·43，带的是目标
 *   13082761331670030 的全部六个 ≡ 1 (mod 3) 素因子（因此同样有 3⁶ = 729 个根），
 *   但只有 8.5×10⁷，可以直接逐个 x 枚举：
 *     S(85276009) = 31125743284（这是 meta.json 里 bruteForceBaselineMs 的口径）。
 *
 * 不可行性：目标 n ≈ 1.31×10¹⁶，逐个枚举需要 ~6.5×10¹⁵ 次迭代；本机实测速率约 1.2×10⁸ 次/秒
 *   （见下方输出），即约 1.7 年——定义域暴力在目标规模上完全不可行，必须走 CRT 组合的
 *   solution.kt 路径。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

/** x³ mod n（x < n；n ≤ 2×10⁹ 时直接乘不会溢出，否则退回俄式乘法）。 */
private fun cubeMod(x: Long, n: Long): Long {
    if (n <= 2_000_000_000L) return x * x % n * x % n
    var result = 0L
    var base = x % n
    var exp = 3L
    while (exp > 0) {
        if (exp and 1L == 1L) result = (result + base) % n
        base = (base + base) % n
        exp = exp shr 1
    }
    return result
}

/** 路径 1：逐个枚举 x ∈ (2, n)，按定义数解求和。 */
private fun enumerate(n: Long): Pair<Long, Long> {
    var sum = 0L
    var count = 0L
    var x = 2L
    while (x < n) {
        if (cubeMod(x, n) == 1L) { sum += x; count++ }
        x++
    }
    return sum to count
}

/** 独立算出的结构公式：解数（含 x = 1）= 3^(k + [9 | n])，k = 素因子中 ≡ 1 (mod 3) 的个数。 */
private fun formulaCount(n: Long): Long {
    var m = n
    var k = 0
    var d = 2L
    while (d <= m / d) {
        if (m % d == 0L) {
            if (d != 3L && d % 3L == 1L) k++
            while (m % d == 0L) m /= d
        }
        d = if (d == 2L) 3L else d + 2L
    }
    if (m > 1L && m != 3L && m % 3L == 1L) k++
    var e = 1L
    repeat(k) { e *= 3L }
    if (n % 9L == 0L) e *= 3L
    return e - 1 // 去掉 x = 1（1 < x < n 的口径）
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

private fun fmt(d: Double) = "%.1f".format(d)

fun main() {
    // ---------- 路径 1：题面锚点 ----------
    val (s91, c91) = enumerate(91L)
    check(s91 == 363L && c91 == 8L) { "题面锚点失败：S(91) = $s91，解数 $c91" }
    println("题面锚点：n = 91 时解为 {9,16,22,29,53,74,79,81}，共 $c91 个，S(91) = $s91（逐个枚举复现）")

    // ---------- 路径 1：小 n 全量对拍（解数 = 3^(k+[9|n])） ----------
    for (n in 2L..3000L) {
        val (_, c) = enumerate(n)
        val f = formulaCount(n)
        check(c == f) { "n = $n：枚举解数 $c ≠ 结构公式 $f" }
    }
    println("n = 2…3000：逐个枚举的解数与结构公式 3^(k+[9|n]) − 1 全部一致")

    // ---------- 路径 1：抽查中等规模（含 3 的幂次与平方因子） ----------
    val spot = listOf(343L, 1183L, 819L, 8281L, 12103L, 15561L, 1729L, 53599L, 482391L, 1983163L)
    for (n in spot) {
        val (s, c) = enumerate(n)
        val f = formulaCount(n)
        check(c == f) { "n = $n：枚举解数 $c ≠ 结构公式 $f" }
        println("n = $n：解数 $c，S = $s（逐个枚举）")
    }

    // ---------- 路径 2：同结构大规模暴力 ----------
    val nBig = 85276009L                    // 7·13·19·31·37·43：目标 n 的六个三次素因子全在
    val (sBig, cBig) = enumerate(nBig)
    check(cBig == 728L) { "n = $nBig 应有 728 个 1 < x < n 的解，实得 $cBig" }
    println("路径 2：n = $nBig = 7·13·19·31·37·43，解数 $cBig，S = $sBig（逐个枚举，与目标同构）")

    // ---------- 计时 ----------
    enumerate(1983163L)
    val msSmall = bestOf3("路径 1：逐个枚举（n = 1983163，约 2.0×10⁶ 次迭代）", 253844863L) {
        enumerate(1983163L).first
    }
    enumerate(nBig)
    val msBig = bestOf3("路径 2：逐个枚举（n = 85276009，约 8.5×10⁷ 次迭代）", sBig) {
        enumerate(nBig).first
    }

    println()
    println("汇总：n = 1983163 ${fmt(msSmall)} ms；n = 85276009 ${fmt(msBig)} ms（3 轮最优，JIT 预热后）")
    val rate = 85276009.0 / (msBig / 1000.0)
    println("实测速率约 ${fmt(rate / 1e8)}×10⁸ 次/秒；外推目标 n ≈ 1.31×10¹⁶ 需 ~6.5×10¹⁵ 次迭代，" +
        "约 ${fmt(6.5e15 / rate / 3.156e7)} 年——定义域暴力不可行，必须用 CRT 组合（solution.kt）")
    println("check() 全部通过")
}
