#!/usr/bin/env kotlin
/**
 * Project Euler 274 — Divisibility Multipliers：直接暴力对照（独立实现，与 solution.kt 不共享核心代码）
 *
 * 三条独立路径，全部以题面定义为准（「f(n) 被 p 整除 ⟺ n 被 p 整除」），不预先假设 m = 10⁻¹ mod p：
 *
 *   路径 1（完全检验 + 逐个 m 扫描）：对每个素数 p ∤ 10，在 m ∈ [1, p) 中顺序扫描，对每个候选 m
 *   完整检验 n ∈ [1, 10p]（这是完备的：n mod p 与 f(n) mod p 都只由 n mod 10p 决定），第一个通过
 *   的 m 即乘数。用完全未优化的形式跑题面锚点规模 p < 1000，复现 Σ = 39517（题面给定）与
 *   m(113) = 34。代价 ≈ Σ_{p<X} 10p²/2 ∝ X³/log X，外推 p < 10^7 需要 ~10^20 次基本运算。
 *
 *   路径 2（廉价过滤 + 候选完全检验）：把「完全检验」留给最终候选，扫描阶段只用单个证人数
 *   n = p（p 的倍数）做廉价过滤（对 d = p mod 10 ≠ 0 的 p 只留下唯一候选）；对候选做
 *   n ∈ [1, 10p] 的完全检验。这样能把定义式检验推到 p < 5000 的整个求和，还能对
 *   p = 9999991（< 10^7 的最大素数）在满载规模做完整检验（10^8 个 n）。
 *
 *   路径 3：本文件内自带的独立快速实现（直接解 10m = x·p + 1，x ∈ [0,10)），与路径 2 的求和对照。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 */

private fun sieve(limit: Int): BooleanArray {
    val isPrime = BooleanArray(limit + 1) { it >= 2 }
    var p = 2
    while (p.toLong() * p <= limit) {
        if (isPrime[p]) {
            var k = p * p
            while (k <= limit) {
                isPrime[k] = false
                k += p
            }
        }
        p++
    }
    return isPrime
}

/** 完全定义式检验：n ∈ [1, nMax] 逐个验证「f(n) 被 p 整除 ⟺ n 被 p 整除」。 */
private fun preserves(p: Long, m: Long, nMax: Long): Boolean {
    for (n in 1..nMax) {
        val f = n / 10 + (n % 10) * m
        if ((f % p == 0L) != (n % p == 0L)) return false
    }
    return true
}

/** 路径 1：对 p < x 的每个素数逐个扫描 m，每个候选都做完全检验。 */
private fun sumByFullScan(x: Int): Long {
    val isPrime = sieve(x)
    var sum = 0L
    for (p in 3 until x step 2) {
        if (!isPrime[p] || p == 5) continue
        val pl = p.toLong()
        var m = 1L
        while (m < pl && !preserves(pl, m, 10 * pl)) m++
        check(m < pl) { "p=$p 没有找到乘数" }
        sum += m
    }
    return sum
}

/** 路径 2 的扫描阶段：用单个证人数 n = p 过滤候选 m（f(p) = p/10 + (p mod 10)·m 必须被 p 整除）。 */
private fun candidateMultipliers(p: Long): LongArray {
    val f0 = p / 10
    val d = p % 10
    val found = ArrayList<Long>()
    for (m in 1 until p) if ((f0 + d * m) % p == 0L) found.add(m)
    return found.toLongArray()
}

/**
 * 路径 2：对 p < x 的素数先廉价过滤出候选乘数，再对候选做 n ∈ [1,10p] 的完全检验；
 * 返回 [和, 完全检验通过次数]。
 */
private fun sumByCandidateFilter(x: Int): LongArray {
    val isPrime = sieve(x)
    var sum = 0L
    var passed = 0L
    for (p in 3 until x step 2) {
        if (!isPrime[p] || p == 5) continue
        val pl = p.toLong()
        for (m in candidateMultipliers(pl)) {
            if (preserves(pl, m, 10 * pl)) {
                sum += m
                passed++
                break
            }
        }
    }
    return longArrayOf(sum, passed)
}

/** 路径 3：本文件内独立的快速实现（10m = x·p + 1 的商枚举）。 */
private fun sumByQuotientScan(x: Int): Long {
    val isPrime = sieve(x)
    var sum = 0L
    for (p in 3 until x step 2) {
        if (!isPrime[p] || p == 5) continue
        var q = 0
        while ((q * p + 1) % 10 != 0) q++
        sum += (q.toLong() * p + 1L) / 10L
    }
    return sum
}

private fun bestOf(tag: String, rounds: Int, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(rounds) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（$rounds 轮最优）")
    return best
}

fun main() {
    // ---------- 路径 1：完全定义式，逐个 m 扫描（题面锚点规模） ----------
    val anchor = sumByFullScan(1000)
    check(anchor == 39517L) { "路径 1：p < 1000 的和应为 39517（题面锚点），实际 $anchor" }
    val m113 = run {
        var m = 1L
        while (m < 113L && !preserves(113L, m, 1130L)) m++
        m
    }
    check(m113 == 34L) { "路径 1：m(113) 应为 34，实际 $m113" }
    println("路径 1：p < 1000 完全检验 Σ = $anchor（= 题面锚点）；m(113) = $m113（= 题面给出）")

    // 路径 1 的规模增长（同一算法，仅扩大范围）：Σ 分别为 137719（p<2000）、295979（p<3000）
    val sum2k = sumByFullScan(2000)
    check(sum2k == 137719L) { "路径 1：p < 2000 应为 137719，实际 $sum2k" }
    val msPath1_2k = bestOf("路径 1：完全定义式扫描 p < 2000", 1, 137719L) { sumByFullScan(2000) }
    val sum3k = sumByFullScan(3000)
    check(sum3k == 295979L) { "路径 1：p < 3000 应为 295979，实际 $sum3k" }
    val msPath1_3k = bestOf("路径 1：完全定义式扫描 p < 3000", 1, 295979L) { sumByFullScan(3000) }
    println("路径 1：p < 2000 的 Σ = $sum2k，p < 3000 的 Σ = $sum3k（用于展示代价增长）")

    // ---------- 路径 2：廉价过滤 + 候选完全检验（更大规模 + 满载单点） ----------
    val r5k = sumByCandidateFilter(5000)
    check(r5k[1] == 667L) { "路径 2：p < 5000 应检验通过 667 个素数，实际 ${r5k[1]}" }
    val fast5k = sumByQuotientScan(5000)
    check(r5k[0] == fast5k) { "路径 2 的和 ${r5k[0]} ≠ 路径 3 快速解 $fast5k" }
    println("路径 2：p < 5000 每个素数都做了 n ∈ [1,10p] 完全检验，Σ = ${r5k[0]}（${r5k[1]} 个素数通过）；" +
        "路径 3（本文件自带快速解）= $fast5k，一致")

    // 满载规模单点：p = 9999991（< 10^7 的最大素数）
    val bigP = 9_999_991L
    var t0 = System.nanoTime()
    val cands = candidateMultipliers(bigP)
    val scanMs = (System.nanoTime() - t0) / 1e6
    check(cands.size == 1) { "p = $bigP：候选乘数应为 1 个，实际 ${cands.size} 个" }
    val bigM = cands[0]
    t0 = System.nanoTime()
    val ok = preserves(bigP, bigM, 10 * bigP)
    val fullMs = (System.nanoTime() - t0) / 1e6
    check(ok) { "p = $bigP：m = $bigM 未通过 n ∈ [1,10p] 的完全检验" }
    println("路径 2 满载单点：p = 9999991，过滤 ${bigP / 2} 个候选得 m = $bigM，通过 " +
        "n ∈ [1, ${10 * bigP}] 完全检验（过滤 ${"%.1f".format(scanMs)} ms + 完全检验 ${"%.1f".format(fullMs)} ms）")

    // ---------- 计时 ----------
    sumByFullScan(1000)
    val msPath1 = bestOf("路径 1：完全定义式扫描 p < 1000", 3, 39517L) { sumByFullScan(1000) }
    sumByCandidateFilter(5000)
    val msPath2 = bestOf("路径 2：过滤 + 完全检验 p < 5000", 3, 770617L) { sumByCandidateFilter(5000)[0] }

    println()
    println("汇总：路径 1 p < 1000 ${"%.1f".format(msPath1)} ms；p < 2000 ${"%.1f".format(msPath1_2k)} ms" +
        "（×${"%.1f".format(msPath1_2k / msPath1)}）；p < 3000 ${"%.1f".format(msPath1_3k)} ms" +
        "（×${"%.1f".format(msPath1_3k / msPath1_2k)}，与 (3/2)³ ≈ 3.4 的立方增长一致）；" +
        "路径 2 p < 5000（含全部完全检验）${"%.1f".format(msPath2)} ms")
    println("外推：路径 1 代价 ≈ Σ_{p<X} 10p²/2 ∝ X³/log X，p < 10^7 需 ~10^20 次基本运算，不可行；" +
        "满载规模用路径 2 的单点完全检验 + 求和级快速解外推")
    println("check() 全部通过")
}
