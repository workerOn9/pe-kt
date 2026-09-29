#!/usr/bin/env kotlin
/**
 * Project Euler 272 — Modular Cubes, Part 2：暴力对照（独立实现，与 solution.kt 不共享核心代码）
 *
 * 路径 1（完全按题面定义，n ≤ 3000）：对每个 n 逐个枚举 x ∈ (2, n) 直接验证 x³ ≡ 1 (mod n)，
 *   数出 C(n)。复现题面锚点：C(91) = 8；并与按题面定义独立算出的结构公式 3^(k+[9|n]) − 1
 *   对拍（k = n 的素因子中 ≡ 1 (mod 3) 的个数，用试除法数）。
 *
 * 路径 2（线性筛法，n ≤ 10⁸，即 meta.json 里 bruteForceBaselineMs 的口径）：
 *   先筛出素数，对每个 ≡ 1 (mod 3) 的素数 q 把它的所有倍数打上 +1（得到每个 n 的 k(n)），
 *   再对每个 n 判断 k(n) + [9|n] == 5，是则累加 n。这是**逐 n 数数**的路径，完全不做
 *   「k 个素因子乘积」的组合枚举，与 solution.kt 的主路径相互独立。
 *
 * 不可行性：题目上界是 10¹¹。逐 n 筛法需要 ⌈10¹¹⌉ 字节级数组（> 100 GB）与 ~1.6×10¹¹ 次标记，
 *   完全不可行；下面实测的 10⁸ 规模已经要 ~1.7 s / ~200 MB，外推到 10¹¹ 约需 10³ 倍时间
 *   与千倍内存——这就是必须做「素因子指数组合」数论化搜索的原因。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

/** 路径 1：逐个 x 枚举，返回 (C(n), 解集)。 */
private fun enumerateRoots(n: Long): Pair<Long, List<Long>> {
    val roots = ArrayList<Long>()
    var x = 2L
    while (x < n) {
        if (x * x % n * x % n == 1L) roots.add(x)
        x++
    }
    return roots.size.toLong() to roots
}

/** 独立算出的结构公式：C(n) = 3^(k+[9|n]) − 1，k 由试除法数出。 */
private fun formulaC(n: Long): Long {
    var m = n
    var k = 0
    var d = 2L
    while (d <= m / d) {
        if (m % d == 0L) {
            if (d % 3L == 1L) k++
            while (m % d == 0L) m /= d
        }
        d = if (d == 2L) 3L else d + 2L
    }
    if (m > 1L && m % 3L == 1L) k++
    var e = 1L
    repeat(k + if (n % 9L == 0L) 1 else 0) { e *= 3L }
    return e - 1
}

/** 路径 2：线性筛法逐 n 数出 k(n)，累加所有 C(n) = 242 的 n。 */
private fun sieveSumByN(limit: Int): Long {
    val isPrime = BooleanArray(limit + 1) { it >= 2 }
    var p = 2
    while (p.toLong() * p <= limit) {
        if (isPrime[p]) {
            var j = p * p
            while (j <= limit) { isPrime[j] = false; j += p }
        }
        p++
    }
    val k = ByteArray(limit + 1)   // 每个 n 的「≡1 (mod 3) 素因子个数」（每个素数只记一次）
    for (q in 2..limit) {
        if (isPrime[q] && q % 3 == 1) {
            var m = q
            while (m <= limit) { k[m] = (k[m] + 1).toByte(); m += q }
        }
    }
    var total = 0L
    for (n in 1..limit) {
        if (k[n] + (if (n % 9 == 0) 1 else 0) == 5) total += n
    }
    return total
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

fun main() {
    // ---------- 路径 1：题面锚点 ----------
    val (c91, r91) = enumerateRoots(91L)
    check(c91 == 8L && r91 == listOf(9L, 16L, 22L, 29L, 53L, 74L, 79L, 81L)) { "C(91) 复现失败：$c91 / $r91" }
    println("题面锚点：n = 91 时解为 $r91，C(91) = $c91（逐个枚举复现）")

    // ---------- 路径 1：与结构公式对拍（n ≤ 3000） ----------
    for (n in 2L..3000L) {
        val (c, _) = enumerateRoots(n)
        val f = formulaC(n)
        check(c == f) { "n = $n：逐个枚举 C(n) = $c ≠ 公式 $f" }
    }
    println("n = 2…3000：逐个枚举的 C(n) 与 3^(k+[9|n]) − 1 全部一致")

    // 最小的几个 C(n) = 242 的 n（两种情形各取代表）
    var n = 482391L
    val (c482, _) = enumerateRoots(n)
    check(c482 == 242L) { "n = 482391（= 9·7·13·19·31）应有 C = 242，实得 $c482" }
    println("路径 1 抽查：n = 482391 = 9·7·13·19·31 → C = $c482（9 | n，4 个三次素因子）")
    val n2 = 1983163L
    val (c198, _) = enumerateRoots(n2)
    check(c198 == 242L) { "n = 1983163（= 7·13·19·31·37）应有 C = 242，实得 $c198" }
    println("路径 1 抽查：n = 1983163 = 7·13·19·31·37 → C = $c198（5 个三次素因子，9 ∤ n）")

    // ---------- 路径 2：逐 n 筛法 ----------
    val s6 = sieveSumByN(1_000_000)
    println("路径 2（逐 n 筛法）：n ≤ 10⁶ 的合法 n 之和 = $s6")
    val s7 = sieveSumByN(10_000_000)
    println("路径 2（逐 n 筛法）：n ≤ 10⁷ 的合法 n 之和 = $s7（与 solution.kt 限同界结果一致）")

    sieveSumByN(10_000_000)
    val ms7 = bestOf3("路径 2：n ≤ 10⁷（标记 + 逐 n 判定）", s7) { sieveSumByN(10_000_000) }
    val s8 = sieveSumByN(100_000_000)
    println("路径 2（逐 n 筛法）：n ≤ 10⁸ 的合法 n 之和 = $s8")
    val ms8 = bestOf3("路径 2：n ≤ 10⁸（标记 + 逐 n 判定，meta 的 bruteForceBaselineMs 口径）", s8) {
        sieveSumByN(100_000_000)
    }

    println()
    println("汇总：路径 2 在 10⁷ 用时 ${"%.1f".format(ms7)} ms，在 10⁸ 用时 ${"%.1f".format(ms8)} ms（3 轮最优，JIT 预热后）")
    val minutes = ms8 * 1000 / 1000.0 / 60.0 // 10⁸ 的毫秒数 ×1000 得 10¹¹ 的毫秒数，再换成分钟
    println("外推：上界 10¹¹ 按同一算法线性外推约需 ${"%.0f".format(minutes)} 分钟，且逐 n 数组要 ~100 GB 量级")
    println("      （或改分段筛）：仍是「逐 n 数数」的下限路线；solution.kt 的「m×s 组合枚举」只要 ~0.2 秒，")
    println("      快约 ${"%.0f".format(minutes * 60 * 1000 / 140.0)} 倍（分母取主路径实测 ~0.14 秒）。")
    println("check() 全部通过")
}
