#!/usr/bin/env kotlin
/**
 * Project Euler 296 — Angular Bisector and Tangent（角平分线与切线）：暴力 / 独立对照
 *
 * 本文件不使用 solution.kt 的「对称化 + 闭式行计数」主路径，提供三条独立路径：
 *   ① 定义级三重循环：a ≤ b ≤ c < a+b、周长 ≤ L、(a+b) | ac 逐一判定（纯定义暴力）。
 *      全量 L = 100000 需 ~L³/48 ≈ 2×10^13 次判定（数十小时），物理不可行，
 *      故缩到 L ≤ 5000（约 2.6×10^9 次判定）。
 *   ② (a,b) 对 + gcd 直接计数：固定 (a,b) 后 c 必须是 q = (a+b)/gcd(a,b) 的倍数，
 *      且落在 [b, min(a+b−1, L−a−b)] 内，闭式数出倍数个数。O(L²) 次带 gcd 的迭代：
 *      全量 L = 100000 约 5.5×10^8 次（≈20 s 量级），缩到 L ≤ 50000 做对拍。
 *   ③ 参数化逐三角形枚举（全量 L = 100000，唯一跑满全尺寸的暴力）：
 *      BE 整数 ⇔ (a+b) | ac，写 a = gα、b = gβ（gcd(α,β)=1，α ≤ β）、c = k(α+β)、
 *      k < g，三边序与周长约束化为 k ≥ ⌈gβ/(α+β)⌉、k ≤ min(g−1, ⌊L/(α+β)⌋−g)。
 *      对每组 (α,β,g) 把 k 逐个枚举并计数 —— 每个合法三角形恰好被点数一次，
 *      共 1 137 208 419 次自增。互素的 (α,β) 用「按素因子标记倍数」的筛代替逐对 gcd。
 *      这是 meta 的 bruteForceBaselineMs 口径（重活，单轮墙钟）。
 *
 * 复现命令
 * ────────
 * cd /Users/samuel/Documents/github/pe-kt
 * OUTDIR=/tmp/kc-0296-bf bash scripts/kotlinc-shim.sh content/problems/0296/brute-force.kt
 * java -cp /tmp/kc-0296-bf:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

private const val LIMIT = 100_000
private const val NMAX = LIMIT / 3 + 1

private val spf = IntArray(NMAX + 1) { it }

private fun initSpf() {
    var i = 2
    while (i * i <= NMAX) {
        if (spf[i] == i) {
            var j = i * i
            while (j <= NMAX) { if (spf[j] == j) spf[j] = i; j += i }
        }
        i++
    }
}

// ───────────── ① 定义级三重循环（缩规模） ─────────────

private fun tripleBrute(L: Int): Long {
    var cnt = 0L
    for (a in 1..L) {
        var b = a
        while (a + 2 * b <= L) {
            val S = a + b
            val cMax = minOf(S - 1, L - S)
            for (c in b..cMax) if ((a.toLong() * c) % S == 0L) cnt++
            b++
        }
    }
    return cnt
}

// ───────────── ② (a,b) 对 + gcd（缩规模） ─────────────

private fun pairBrute(L: Int): Long {
    var cnt = 0L
    for (a in 1..L / 2) {
        var b = a
        while (a + 2 * b <= L) {
            val S = a + b
            val cMax = minOf(S - 1, L - S)
            if (cMax >= b) {
                var x = a
                var y = b
                while (y != 0) { val t = x % y; x = y; y = t }
                val q = S / x
                cnt += (cMax / q - (b - 1) / q).toLong()
            }
            b++
        }
    }
    return cnt
}

// ───────────── ③ 全尺寸：参数化逐三角形枚举 ─────────────

private fun quadrupleBrute(L: Int): Long {
    var total = 0L
    val flag = BooleanArray(L / 6 + 4)
    for (s in 2..L / 3) {                       // s = α+β，最小 3（g+k ≥ 3）→ 从 2 起也无妨
        val M = L / s
        if (M < 3) continue
        val half = s / 2
        java.util.Arrays.fill(flag, 0, half + 1, true)
        var t = s
        while (t > 1) {                          // α 与 s 互素 ⇔ 与 s 的每个素因子互素
            val p = spf[t]
            while (t % p == 0) t /= p
            if (p <= half) { var j = p; while (j <= half) { flag[j] = false; j += p } }
        }
        val ghalf = (M + 1) / 2
        for (alpha in 1..half) {
            if (!flag[alpha]) continue
            var v = 0                            // v = ⌊g·α/s⌋（增量维护）
            var r = alpha                        // r = g·α mod s
            for (g in 2..M - 1) {
                r += alpha
                if (r >= s) { r -= s; v++ }
                var klo = g - v                  // ⌈gβ/s⌉ = g − ⌊gα/s⌋
                if (klo < 1) klo = 1
                val khi = if (g <= ghalf) g - 1 else M - g
                for (k in klo..khi) total++
            }
        }
    }
    return total
}

private fun best(tag: String, expected: Long, rounds: Int, f: () -> Long): Double {
    check(f() == expected) { "$tag 热身轮结果漂移" }      // 先热身一轮（C2 编译），不计时
    var bestMs = Double.MAX_VALUE
    repeat(rounds) { r ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${r + 1} 轮漂移：$out ≠ $expected" }
        if (ms < bestMs) bestMs = ms
    }
    println("$tag：${"%.1f".format(bestMs)} ms（热身 1 轮后取 $rounds 轮最优）")
    return bestMs
}

fun main() {
    initSpf()

    println("== ① 定义级三重循环（a ≤ b ≤ c < a+b，周长 ≤ L，(a+b) | ac 逐一判定）==")
    for ((L, know) in listOf(100 to 376L, 500 to 13445L, 1000 to 61339L, 2000 to 276390L)) {
        val got = tripleBrute(L)
        check(got == know) { "L=$L：三重循环 $got ≠ 预期 $know" }
        println("L = %5d：%d ✓".format(L, got))
    }
    val msTriple = best("三重循环 L = 5000（全量 100000 需 ~2×10^13 次判定，缩规模）", 1988264L, 1) {
        tripleBrute(5000)
    }

    println()
    println("== ② (a,b) 对 + gcd 直接计数（O(L²)）==")
    for ((L, know) in listOf(20000 to 38124651L, 33333 to 112382655L)) {
        val got = pairBrute(L)
        check(got == know) { "L=$L：(a,b) 对计数 $got ≠ 预期 $know" }
        println("L = %5d：%d ✓".format(L, got))
    }
    val msPair = best("(a,b) 对计数 L = 50000（全量约需 5.5×10^8 次带 gcd 迭代，缩规模）", 264469767L, 1) {
        pairBrute(50000)
    }

    println()
    println("== ③ 参数化逐三角形枚举（全量 L = 100000，逐 k 点数）==")
    val got20000 = quadrupleBrute(20000)
    check(got20000 == 38124651L) { "③ 在 L=20000 的中间量不一致：$got20000" }
    println("L = 20000 中间量：$got20000 ✓（与 ② 的独立结果一致）")
    val msQuad = best("全量逐三角形枚举 L = 100000", 1137208419L, 3) { quadrupleBrute(LIMIT) }

    println()
    println("== 结论 ==")
    println("全量（周长 ≤ 100000）合法三角形个数 = ${quadrupleBrute(LIMIT)}")
    println("耗时口径：三重循环 L=5000 ${"%.1f".format(msTriple)} ms；")
    println("          (a,b)+gcd L=50000 ${"%.1f".format(msPair)} ms；")
    println("          全量逐三角形枚举 L=100000 ${"%.1f".format(msQuad)} ms（meta.bruteForceBaselineMs 口径）")
}
