#!/usr/bin/env kotlin
/**
 * Project Euler 254 — Sums of Digit Factorials（数位阶乘和）
 *
 * 思路：
 *   f(n) = Σ (数位阶乘) 只依赖 n 的数位多重集。设 F = f(n)，把 F 写成币值和
 *   F = Σ_{d=1..9} c_d·d!（另有 0! = 1! = 1 的重复币）：币链 1! | 2! | … | 9! 每一步都整除
 *   后一个，所以「位数最少的写法」就是混合进制贪心——c_9 = F div 9!，c_8 = (F mod 9!) div 8!，…
 *   且表示唯一（任何 c_d ≥ d+1 都能进位换成更少的币）。于是给定 F：
 *     · n 的数位恰为「c_d 个数字 d」，最小排列是升序串；
 *     · sf(n) = digitsum(F)；位数 cc(F) = Σ c_d；sg(i) 只与币向量有关：sg = Σ d·c_d。
 *   （0! 与 1! 同为 1：把这一枚币记成数字 1 永远比记成数字 0 小——有数字 1 时它必作首位，
 *   换成 0 后首位变成 ≥ 2 的数字，位数相同、首位更大。故 g(i) 中不出现数字 0。）
 *
 *   所以求 g(i) 归结为：在所有 digitsum(F) = i 的 F 里最小化（cc(F), 排列），
 *   而 sg(i) = Σ d·c_d，无需（也不可能）写出 g(i) 本身。
 *
 *   窗口：F ≤ cc(F)·9!，若某 F 能优于 F_i^*（数位和为 i 的最小整数），则
 *   F ≤ cc(F_i^*)·9! =: hi。主路径对每个 i 用数位 DFS 升序枚举 [F_i^*, hi] 内
 *   数位和为 i 的全部整数，逐个做贪心展开并比较（等位数时比较 (c_1..c_8) 字典序，
 *   越"多小数字"越小）。
 *
 *   量级：i = 150 时 F_i^* ≈ 7×10^16，cc = 192 901 234 587——g(150) 有约 1.9×10^11 位，
 *   暴力/字典序构造全部出局；但 sg(150) = 1736111111221 由币向量直接给出。
 *
 *   第二路径（结构不同）：
 *     · i ≥ 63：数位和为 i 的下一个整数是 next_i = lead·10^q + 9·10^(q-1) − 1
 *       （i = 9q + r，q ≥ 1；r > 0 时 lead = r+1，否则 lead = 1），
 *       而 next_i − F_i^* = 9·10^(q-1) > cc(F_i^*)·9! − F_i^*（逐 i 复核），
 *       窗口内再无别的数位和为 i 的整数 → F_i^* 唯一最优。
 *     · i ≤ 62：直接从 1 线性扫到 B = max cc(F_i^*)·9! = 18 506 880，用"尾 9"技巧
 *       增量维护数位和、用混合进制进位增量维护币向量，逐 F 更新每个 i 的最优 (cc, 排列)。
 *   两条路径给出同一个答案；另有 n ≤ 10^7 的按定义暴力对照（覆盖 g(i) ≤ 10^7 的 i = 1..44）。
 *
 * 旁证：公开答案表（luckytoilet/projecteuler-solutions）254 → 8184523820510；
 *   题面样例 g(5) = 25、g(20) = 267、Σsg(1..20) = 156 全部复现；
 *   暴力对照 44/44 一致。
 *
 * 答案：8184523820510
 * 复杂度：主路径 ≈ 1.13×10^6 个候选（数位 DFS + 超界即停剪枝）；第二路径线性 1.85×10^7 步。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val MAX_I = 150
private const val BASE = 362_880L                  // 9!：最大的一枚币
private const val BRUTE_N = 10_000_000L            // 小规模暴力上界
private const val EXPECTED = 8_184_523_820_510L

private val FACT = longArrayOf(1L, 1L, 2L, 6L, 24L, 120L, 720L, 5_040L, 40_320L, 362_880L)
private val POW10 = LongArray(18).also { arr -> var p = 1L; for (k in 0..17) { arr[k] = p; p *= 10 } }

/** F = Σ c[d]·d! 的贪心（混合进制）展开；c[0] 恒为 0（0! 那一枚记作数字 1）。 */
private fun greedyCounts(F: Long): LongArray {
    val c = LongArray(10)
    var f = F
    for (d in 9 downTo 2) {
        c[d] = f / FACT[d]
        f -= c[d] * FACT[d]
    }
    c[1] = f                                     // 0 或 1
    return c
}

private fun coinCount(c: LongArray): Long {
    var s = 0L
    for (d in 1..9) s += c[d]
    return s
}

private fun sgOf(c: LongArray): Long {
    var s = 0L
    for (d in 1..9) s += d.toLong() * c[d]
    return s
}

private fun digitSum(F: Long): Int {
    var y = F
    var s = 0
    while (y > 0) { s += (y % 10).toInt(); y /= 10 }
    return s
}

/** 数位和为 i 的最小正整数：i = 9q + r → 数字 r 后跟 q 个 9（r = 0 时就是 q 个 9）。 */
private fun minWithDigitSum(i: Int): Long {
    val q = i / 9
    val r = i % 9
    var v = r.toLong()
    repeat(q) { v = v * 10 + 9 }
    return v
}

/** 数位个数：用阈值/除法循环，不用 toString().length。 */
private fun digitCount(F: Long): Int {
    var x = F
    var n = 1
    while (x >= 10) { x /= 10; n++ }
    return n
}

/** 把 F 的数位写成高位在前，返回位数。 */
private fun digitsMsb(F: Long, out: IntArray): Int {
    val n = digitCount(F)
    var x = F
    for (k in n - 1 downTo 0) { out[k] = (x % 10).toInt(); x /= 10 }
    return n
}

/** 两个等位数排列的字典序比较：数位全非零时"小数字用得越多"数越小 → (c1..c8) 字典序大者更优。 */
private fun arrangementLess(a: LongArray, b: LongArray): Boolean {
    for (d in 1..8) if (a[d] != b[d]) return a[d] > b[d]
    return false
}

// ------------------------------------------------------------------ 主路径：窗口 + 数位 DFS

private class WindowScan(private val target: Int) {
    val best = LongArray(10)
    private val loDig = IntArray(20)
    private val hiDig = IntArray(20)
    var bestD = 0L
    var scanned = 0L
    private var stop = false

    fun solve(): Long {
        val f0 = minWithDigitSum(target)
        System.arraycopy(greedyCounts(f0), 0, best, 0, 10)
        bestD = coinCount(best)
        val hi = bestD * BASE
        val loLen = digitsMsb(f0, loDig)
        val hiLen = digitsMsb(hi, hiDig)
        for (len in loLen..hiLen) dfs(len, 0, len == loLen, len == hiLen, target, 0L)
        return bestD
    }

    private fun offer(F: Long) {
        scanned++
        if (F > bestD * BASE) { stop = true; return }        // 升序枚举：后续只会更大
        val c = greedyCounts(F)
        val d = coinCount(c)
        if (d < bestD || (d == bestD && arrangementLess(c, best))) {
            System.arraycopy(c, 0, best, 0, 10)
            bestD = d
        }
    }

    private fun dfs(len: Int, pos: Int, tightLo: Boolean, tightHi: Boolean, rem: Int, num: Long) {
        if (stop || rem < 0 || rem > 9 * (len - pos)) return
        if (pos == len) {
            if (rem == 0) offer(num)
            return
        }
        var dmin = if (tightLo) loDig[pos] else 0
        val dmax = if (tightHi) hiDig[pos] else 9
        if (pos == 0 && dmin < 1) dmin = 1
        var d = dmin
        while (d <= dmax) {
            dfs(len, pos + 1, tightLo && d == loDig[pos], tightHi && d == hiDig[pos], rem - d, num * 10 + d)
            d++
        }
    }
}

private val winnersA = Array(MAX_I + 1) { LongArray(10) }
private var scannedTotal = 0L
private var scannedMax = 0L
private var scannedMaxI = 0

/** 主路径（最优路径）：逐 i 在窗口 [F_i^*, cc(F_i^*)·9!] 内数位 DFS 升序扫描。 */
private fun solveByWindowDfs(): Long {
    var total = 0L
    scannedTotal = 0L
    scannedMax = 0L
    scannedMaxI = 0
    for (i in 1..MAX_I) {
        val scan = WindowScan(i)
        scan.solve()
        System.arraycopy(scan.best, 0, winnersA[i], 0, 10)
        total += sgOf(scan.best)
        scannedTotal += scan.scanned
        if (scan.scanned > scannedMax) { scannedMax = scan.scanned; scannedMaxI = i }
    }
    return total
}

// ------------------------------------------------------------------ 第二路径：唯一性证明 + 线性扫描

/** 数位和为 i 的、大于 F_i^* 的最小整数。 */
private fun nextWithDigitSum(i: Int): Long {
    val q = i / 9
    val r = i % 9
    if (q == 0) return (i + 9).toLong()            // i ≤ 8：F_i^* = i，下一个是 1 后跟 i-1
    val lead = if (r > 0) r + 1 else 1
    return lead * POW10[q] + 9 * POW10[q - 1] - 1
}

private fun solveByCertificateAndSweep(): Long {
    val baseWinners = Array(MAX_I + 1) { LongArray(10) }
    val marked = BooleanArray(MAX_I + 1)           // 需要扫描的 i（唯一性证明不成立）
    var sweepBound = 0L
    sweepBoundUsed = 0L
    for (i in 1..MAX_I) {
        val c = greedyCounts(minWithDigitSum(i))
        baseWinners[i] = c
        val hi = coinCount(c) * BASE
        marked[i] = nextWithDigitSum(i) <= hi      // 下一个候选落在窗口内 → 证书失败
        if (marked[i] && hi > sweepBound) sweepBound = hi
    }
    sweepBoundUsed = sweepBound

    // 线性扫描 F = 1..sweepBound：增量数位和 + 增量混合进制进位
    val bestD = LongArray(MAX_I + 1) { Long.MAX_VALUE }
    val bestC = Array(MAX_I + 1) { LongArray(10) }
    val cur = LongArray(10)
    var ds = 0
    var cc = 0L
    var f = 0L
    while (f < sweepBound) {
        f++
        ds++                                        // 数位和增量：+1，并扣掉尾 9
        var x = f - 1
        while (x % 10L == 9L) { ds -= 9; x /= 10 }
        cur[1]++                                    // 币向量增量：1! 进一位，按 2..9 进制进位
        cc++
        var d = 1
        while (d <= 8 && cur[d] == (d + 1).toLong()) {
            cc -= (d + 1).toLong()
            cur[d] = 0
            cur[d + 1]++
            cc++
            d++
        }
        if (ds in 1..MAX_I && marked[ds] &&
            (cc < bestD[ds] || (cc == bestD[ds] && arrangementLess(cur, bestC[ds])))
        ) {
            bestD[ds] = cc
            System.arraycopy(cur, 0, bestC[ds], 0, 10)
        }
    }

    var total = 0L
    for (i in 1..MAX_I) {
        val c = if (marked[i]) {
            require(bestD[i] != Long.MAX_VALUE) { "i = $i 在扫描范围内无候选" }
            bestC[i]
        } else {
            baseWinners[i]
        }
        total += sgOf(c)
    }
    certCount = (1..MAX_I).count { !marked[it] }
    scanCount = (1..MAX_I).count { marked[it] }
    lastCertI = (1..MAX_I).first { !marked[it] }
    return total
}

private var certCount = 0
private var scanCount = 0
private var lastCertI = 0
private var sweepBoundUsed = 0L

// ------------------------------------------------------------------ 小规模暴力对照

/** 按定义逐个 n 计算：f(n) = Σ 数位阶乘，sf(n) = f(n) 的数位和，记录每个 sf 首次出现的 n。 */
private fun bruteSmall(nMax: Long): LongArray {
    val best = LongArray(200)
    var n = 1L
    while (n <= nMax) {
        var x = n
        var f = 0L
        while (x > 0) { f += FACT[(x % 10).toInt()]; x /= 10 }
        var y = f
        var s = 0
        while (y > 0) { s += (y % 10).toInt(); y /= 10 }
        if (s < best.size && best[s] == 0L) best[s] = n
        n++
    }
    return best
}

/** 把币向量拼成最小排列（升序串，位数 ≤ 18 时可用 Long 表示）。 */
private fun materialize(c: LongArray): Long {
    var v = 0L
    for (d in 1..9) repeat(c[d].toInt()) { v = v * 10 + d }
    return v
}

// ------------------------------------------------------------------ main

fun main() {
    // ---- 主路径（最优路径）：JIT 预热后 3 轮计时
    val ansA0 = solveByWindowDfs()
    check(ansA0 == EXPECTED) { "主路径答案异常：$ansA0" }
    var bestA = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val a = solveByWindowDfs()
        val ms = (System.nanoTime() - t0) / 1e6
        check(a == EXPECTED)
        if (ms < bestA) bestA = ms
        println("  主路径（窗口 + 数位 DFS）第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("主路径：候选总数 $scannedTotal（最多的一题 i = $scannedMaxI 有 $scannedMax 个）")
    println("主路径（窗口 + 数位 DFS）答案 = $ansA0，${"%.1f".format(bestA)} ms（3 轮最优，JIT 预热后）")

    // ---- 第二路径（结构不同）：唯一性证书 + 线性扫描
    val ansB0 = solveByCertificateAndSweep()
    check(ansB0 == EXPECTED) { "第二路径答案异常：$ansB0" }
    var bestB = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val b = solveByCertificateAndSweep()
        val ms = (System.nanoTime() - t0) / 1e6
        check(b == EXPECTED)
        if (ms < bestB) bestB = ms
        println("  第二路径（证书 + 线性扫描）第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("第二路径（证书 + 线性扫描）答案 = $ansB0，${"%.1f".format(bestB)} ms（3 轮最优，JIT 预热后）")
    println("  证书成立（F_i^* 唯一）的 i = $certCount 个（i ≥ $lastCertI）；扫描 i = $scanCount 个，扫描上界 B = $sweepBoundUsed")
    check(ansA0 == ansB0)

    // ---- 内部一致性：证书候选的数位和、以及每个 i 的获胜币向量都对应数位和为 i 的 F
    for (i in 1..MAX_I) {
        val nx = nextWithDigitSum(i)
        check(nx > minWithDigitSum(i) && digitSum(nx) == i) { "证书候选异常：i = $i，next = $nx" }
    }
    for (i in 1..MAX_I) {
        var F = 0L
        for (d in 1..9) F += winnersA[i][d] * FACT[d]
        check(digitSum(F) == i) { "i = $i 的获胜币向量展开后数位和不是 i" }
    }
    println("内部一致性：next_i 的数位和、获胜币向量的 f 值数位和全部等于 i（i = 1..150）")

    // ---- 题面样例
    val g5 = materialize(winnersA[5])
    val g20 = materialize(winnersA[20])
    check(g5 == 25L) { "g(5) = $g5" }
    check(g20 == 267L) { "g(20) = $g20" }
    var sum20 = 0L
    for (i in 1..20) sum20 += sgOf(winnersA[i])
    check(sum20 == 156L) { "Σsg(1..20) = $sum20" }
    println("题面样例：g(5) = $g5，g(20) = $g20，Σsg(1..20) = $sum20")

    // ---- 小规模暴力对照（n ≤ 10^7，覆盖 g(i) ≤ 10^7 的全部 i）
    val t0 = System.nanoTime()
    val brute = bruteSmall(BRUTE_N)
    val bruteMs = (System.nanoTime() - t0) / 1e6
    var matched = 0
    var maxCheckedI = 0
    for (i in 1..MAX_I) {
        val d = coinCount(winnersA[i])
        if (d > 18) continue
        val g = materialize(winnersA[i])
        if (g <= BRUTE_N) {
            check(brute[i] == g) { "i = $i：暴力 ${brute[i]} ≠ 结构 $g" }
            matched++
            if (i > maxCheckedI) maxCheckedI = i
        }
    }
    var bruteSum20 = 0L
    for (i in 1..20) {
        var y = brute[i]
        while (y > 0) { bruteSum20 += y % 10; y /= 10 }
    }
    println("暴力对照：n ≤ $BRUTE_N（${"%.1f".format(bruteMs)} ms）与结构解在 i = 1..$maxCheckedI 上全部一致（$matched 个 i）；" +
        "暴力 Σsg(1..20) = $bruteSum20")

    // ---- 结构统计（写入 analysis.md 的数据）
    var maxSg = 0L
    var maxSgI = 0
    var maxD = 0L
    for (i in 1..MAX_I) {
        val sg = sgOf(winnersA[i])
        if (sg > maxSg) { maxSg = sg; maxSgI = i }
        if (coinCount(winnersA[i]) > maxD) maxD = coinCount(winnersA[i])
    }
    println("sg 最大者：i = $maxSgI，sg = $maxSg；最大位数 cc = $maxD（i = 150，g(150) 约 1.9×10^11 位）")

    println("check() 全部通过，答案 = $ansA0")
}
