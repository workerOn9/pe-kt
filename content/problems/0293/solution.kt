#!/usr/bin/env kotlin
/**
 * Project Euler 293 — Pseudo-Fortunate Numbers（伪幸运数）
 *
 * 题目：偶数 N 若为 2 的幂，或它的不同素因子恰为连续素数，则称为 admissible。
 * 对 admissible 的 N，pseudo-Fortunate 数 M 是「使 N+M 为素数的最小整数 M > 1」。
 * 求所有 N < 10^9 的 admissible 数所对应的不同 M 之和。
 *
 * 建模
 * ────
 * N 是偶数 ⇒ 2 必为它的素因子 ⇒「不同素因子为连续素数」只能是从 2 开始的素数前缀
 * {2, 3, 5, 7, …}。于是 admissible 数恰有两支：
 *   ① N = 2^a（a ≥ 1）：纯 2 的幂；
 *   ② N = 2^a · 3^{e1} · 5^{e2} · … · p_k^{ek}（每个奇素数指数 e_i ≥ 1，素数取连续前缀）。
 * 两者可统一为 DFS：从每个 2^a 出发，按 3,5,7,… 的顺序决定「是否引入下一个素因子」，
 * 引入时指数至少 1；乘积一旦 ≥ 10^9 即剪枝。前缀最多到 23——因为
 *   2·3·5·7·11·13·17·19·23 = 223092870 < 10^9 < 6469693230 = 223092870·29。
 *
 * 找 M（pseudo-Fortunate 数）
 * ──────────────────────────
 * N 是偶数 ⇒ N+2 是偶合数 ⇒ 若 N+M 为素数则 M 只能是 ≥ 3 的奇数，且 N+M 是「大于 N+1
 * 的最小素数」。于是直接从 m = 3 起逐个奇数做确定性素性判定，命中即停。素性判定用
 * Miller–Rabin（判定对象约为 10^9 量级，实测所有候选满足 N+M < 10^9+131 ≪ 3,215,031,751，
 * 底数 {2,3,5,7} 就已确定，代码里多取几个底数作余量）。
 *
 * 对拍
 * ────
 * 1. 题面给出的前 12 个 admissible 数由生成器复现；
 * 2. 题面样例：M(16) = 3、M(630) = 11（631 之后的下一个素数是 641）；
 * 3. 全量用「试除法」重算每个 M（逐候选验证），与 Miller–Rabin 版本集合逐项相等；
 * 4. brute-force.kt 用「枚举所有偶数 + 试除分解」的定义级做法在小范围对拍，
 *    并用「23-光滑超集 + 定义过滤 + 分段筛」独立复核全量。
 *
 * 复杂度
 * ──────
 * admissible 数 A 个（实跑 6656）· 素数间隙期望 O(log N) 个候选 · Miller–Rabin 常数次
 * 模乘 ⇒ 毫秒级。
 *
 * 运行
 * ────
 * OUTDIR=/tmp/kc-0293 bash scripts/kotlinc-shim.sh content/problems/0293/solution.kt
 * java -cp /tmp/kc-0293:<kotlin-stdlib> SolutionKt
 */

private const val BIG_LIMIT = 1_000_000_000L

/** 2 之后允许出现的奇素数前缀（所有 admissible N < 10^9 的奇素因子都不超过 23，见文件头）。 */
private val ODD_PRIMES = longArrayOf(3, 5, 7, 11, 13, 17, 19, 23)

// ───────────────────────────── 素性判定 ─────────────────────────────

private fun powMod(base: Long, exp: Long, mod: Long): Long {
    var b = base % mod
    var e = exp
    var acc = 1L
    while (e > 0L) {
        if (e and 1L == 1L) acc = acc * b % mod
        b = b * b % mod
        e = e shr 1
    }
    return acc
}

/**
 * 确定性 Miller–Rabin。底数 {2,3,5,7} 对 n < 3,215,031,751 已足以判定；这里取到 37
 * 只是留余量（本题实测 n < 10^9+131）。
 */
private fun isPrime(n: Long): Boolean {
    if (n < 2L) return false
    for (p in longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)) {
        if (n % p == 0L) return n == p
    }
    var d = n - 1
    var r = 0
    while (d and 1L == 0L) {
        d = d shr 1
        r++
    }
    for (a in longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)) {
        var x = powMod(a, d, n)
        if (x == 1L || x == n - 1L) continue
        var composite = true
        for (i in 1 until r) {
            x = x * x % n
            if (x == n - 1L) {
                composite = false
                break
            }
        }
        if (composite) return false
    }
    return true
}

/** 试除法素性判定（独立于 Miller–Rabin，用于交叉复核）。 */
private fun isPrimeByTrialDivision(n: Long): Boolean {
    if (n < 2L) return false
    if (n % 2L == 0L) return n == 2L
    var d = 3L
    while (d * d <= n) {
        if (n % d == 0L) return false
        d += 2L
    }
    return true
}

// ───────────────────────── admissible 数生成 ─────────────────────────

/**
 * 生成长度小于 limit 的全部 admissible 数（升序）。
 * 分支 ①：2 的幂；分支 ②：2 的幂 × 从 3 开始的连续素数前缀，每个奇素数指数至少 1。
 */
private fun admissibleBelow(limit: Long): LongArray {
    val out = ArrayList<Long>(8192)

    fun extend(idx: Int, cur: Long) {
        if (idx == ODD_PRIMES.size) return
        val p = ODD_PRIMES[idx]
        var v = cur
        while (v <= (limit - 1) / p) {          // 保证 v*p < limit，且不溢出
            v *= p
            out.add(v)
            extend(idx + 1, v)
        }
    }

    var two = 2L
    while (two < limit) {
        out.add(two)                            // 分支 ①：2 的幂
        extend(0, two)                          // 分支 ②：以 2^a 打底的素数前缀
        if (two > (limit - 1) / 2) break        // 下一个 2 倍会达到/超过 limit
        two *= 2
    }

    out.sort()
    return out.toLongArray()
}

// ─────────────────────── pseudo-Fortunate 数 ───────────────────────

/** M(N)：N 为偶数，N+2 是偶合数，故只需从 m = 3 起枚举奇数。 */
private fun minM(n: Long): Int {
    var m = 3
    while (!isPrime(n + m)) m += 2
    return m
}

/** 同样的定义，但素性判定换成试除法（用于全量交叉复核）。 */
private fun minMByTrialDivision(n: Long): Int {
    var m = 3
    while (!isPrimeByTrialDivision(n + m)) m += 2
    return m
}

/** 返回所有 admissible N < limit 对应的不同 M（升序去重）。 */
private fun distinctMs(limit: Long, trialDivision: Boolean = false): IntArray {
    val ns = admissibleBelow(limit)
    val ms = IntArray(ns.size)
    for (i in ns.indices) {
        ms[i] = if (trialDivision) minMByTrialDivision(ns[i]) else minM(ns[i])
    }
    ms.sort()
    val out = ArrayList<Int>(ms.size)
    for (m in ms) if (out.isEmpty() || out.last() != m) out.add(m)
    return out.toIntArray()
}

private fun sumOfMs(limit: Long, trialDivision: Boolean = false): Long =
    distinctMs(limit, trialDivision).sumOf { it.toLong() }

// ───────────────────────────── 计时工具 ─────────────────────────────

private fun best(tag: String, expected: Long, rounds: Int = 3, f: () -> Long): Double {
    var bestMs = Double.MAX_VALUE
    repeat(rounds) { r ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${r + 1} 轮漂移：$out ≠ $expected" }
        if (ms < bestMs) bestMs = ms
    }
    println("$tag：${"%.3f".format(bestMs)} ms（$rounds 轮最优，JIT 预热后）")
    return bestMs
}

fun main() {
    println("== 1. admissible 数生成器：题面前 12 个 ==")
    val ns = admissibleBelow(BIG_LIMIT)
    val first12 = ns.take(12).joinToString(", ")
    println("生成器前 12 个：$first12")
    check(
        ns.size >= 12 &&
            ns[0] == 2L && ns[1] == 4L && ns[2] == 6L && ns[3] == 8L &&
            ns[4] == 12L && ns[5] == 16L && ns[6] == 18L && ns[7] == 24L &&
            ns[8] == 30L && ns[9] == 32L && ns[10] == 36L && ns[11] == 48L,
    ) { "前 12 个 admissible 数与题面不符" }
    println("题面期望：2, 4, 6, 8, 12, 16, 18, 24, 30, 32, 36, 48  ✓")
    println("N < 10^9 的 admissible 数共 ${ns.size} 个，最大 ${ns.last()}")

    println()
    println("== 2. 题面样例 ==")
    check(ns.binarySearch(630L) >= 0) { "630 应为 admissible 数" }
    println("M(16) = ${minM(16)}（期望 3）")
    println("M(630) = ${minM(630)}（期望 11；631 之后的下一个素数是 641）")

    println()
    println("== 3. 小规模表（供 brute-force.kt 对拍）==")
    for (limit in longArrayOf(100_000L, 1_000_000L)) {
        val a = admissibleBelow(limit).size
        val s = sumOfMs(limit)
        val sTrial = sumOfMs(limit, trialDivision = true)
        check(s == sTrial)
        println("N < $limit：admissible ${a} 个，不同 M 之和 = $s（试除法复核同值）")
    }

    println()
    println("== 4. 全量：Miller–Rabin 版 vs 试除法版 ==")
    val msCold = distinctMs(BIG_LIMIT)
    val msTrial = distinctMs(BIG_LIMIT, trialDivision = true)
    check(msCold.contentEquals(msTrial)) {
        "两套素性判定给出的 M 集合不一致：${msCold.toList()} vs ${msTrial.toList()}"
    }
    val sum = msCold.sumOf { it.toLong() }
    println("不同 M 共 ${msCold.size} 个，范围 [${msCold.first()}, ${msCold.last()}]")
    println("不同 M 全表：${msCold.joinToString(", ")}")
    println("两套素性判定（Miller–Rabin vs 试除法）在全部 ${ns.size} 个 N 上给出的 M 集合完全一致 ✓")

    println()
    println("== 5. 计时 ==")
    best("生成器（admissibleBelow，全量）", ns.size.toLong()) { admissibleBelow(BIG_LIMIT).size.toLong() }
    val msOpt = best("主路径：DFS 生成 + Miller–Rabin（含去重求和）", sum) { sumOfMs(BIG_LIMIT) }
    best("交叉复核：DFS 生成 + 试除法（全量）", sum) { sumOfMs(BIG_LIMIT, trialDivision = true) }

    println()
    println("== 6. 结果 ==")
    println("N < 10^9 的全部 admissible 数对应的不同 pseudo-Fortunate 数之和 = $sum")
    println("主路径耗时 ${"%.3f".format(msOpt)} ms")
}
