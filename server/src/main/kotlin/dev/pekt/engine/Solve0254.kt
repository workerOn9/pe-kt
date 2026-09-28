package dev.pekt.engine

/**
 * PE 254 — Sums of Digit Factorials（数位阶乘和）。
 *
 * f(n) = Σ 数位阶乘，只依赖 n 的数位多重集。设 F = f(n)：币链 1! | 2! | … | 9!（另有 0! = 1! = 1）
 * 每一步整除后一个，所以把 F 写成 Σ c_d·d! 的「最少币数」写法就是混合进制贪心且唯一；
 * n 的数位恰是「c_d 个数字 d」，最小排列为升序串（数字 0 永不出现：0 与 1 同为 1 币，
 * 数字 1 作首位必小于任何 ≥ 2 的首位）。于是 g(i) 归结为在所有 digitsum(F) = i 的 F 中
 * 最小化 (cc(F) = Σ c_d, 排列)，而 sg(i) = Σ d·c_d 直接由币向量得出（g(i) 可达 1.9×10^11 位，
 * 不可能写出）。
 *
 * F ≤ cc(F)·9!，故优于 F_i^*（数位和为 i 的最小整数）的候选必落在窗口 [F_i^*, cc(F_i^*)·9!] 内；
 * 本实现对每个 i 用数位 DFS 升序枚举窗口内数位和为 i 的整数，逐个贪心展开比较。
 * 与 content/problems/0254/solution.kt 的主路径一致（该目录另有"唯一性证书 + 线性扫描"
 * 的第二路径与 n ≤ 10^7 的暴力对照，以及公开答案表旁证）。
 *
 * 复杂度：约 1.1×10^6 个候选（本机 JIT 后约 40 ms）。答案 8184523820510（Long 内）。
 */
internal fun solve0254Impl(): Long {
    var total = 0L
    for (i in 1..150) {
        val scan = Pe254WindowScan(i)
        scan.solve()
        var sg = 0L
        for (d in 1..9) sg += d.toLong() * scan.best[d]
        total += sg
    }
    require(total == 8_184_523_820_510L) { "PE254 答案异常：$total" }
    return total
}

/** 9!：最大的一枚币。 */
private const val PE254_BASE = 362_880L

private val PE254_FACT = longArrayOf(1L, 1L, 2L, 6L, 24L, 120L, 720L, 5_040L, 40_320L, 362_880L)

/** 窗口 [minWithDigitSum(i), cc·9!] 内的数位 DFS 扫描器。 */
private class Pe254WindowScan(private val target: Int) {
    val best = LongArray(10)
    private val loDig = IntArray(20)
    private val hiDig = IntArray(20)
    private var bestD = 0L
    private var stop = false

    fun solve(): Long {
        val f0 = pe254MinWithDigitSum(target)
        pe254GreedyCounts(f0, best)
        bestD = pe254CoinCount(best)
        val loLen = pe254DigitsMsb(f0, loDig)
        val hiLen = pe254DigitsMsb(bestD * PE254_BASE, hiDig)
        for (len in loLen..hiLen) dfs(len, 0, len == loLen, len == hiLen, target, 0L)
        return bestD
    }

    private fun offer(F: Long) {
        if (F > bestD * PE254_BASE) { stop = true; return }   // 升序枚举：后续只会更大
        val c = LongArray(10)
        pe254GreedyCounts(F, c)
        val d = pe254CoinCount(c)
        if (d < bestD || (d == bestD && pe254ArrangementLess(c, best))) {
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

/** F 的贪心（混合进制）币展开写入 out；out[0] 恒为 0（0! 那一枚记作数字 1）。 */
private fun pe254GreedyCounts(F: Long, out: LongArray) {
    var f = F
    for (d in 9 downTo 2) {
        out[d] = f / PE254_FACT[d]
        f -= out[d] * PE254_FACT[d]
    }
    out[1] = f
    out[0] = 0L
}

private fun pe254CoinCount(c: LongArray): Long {
    var s = 0L
    for (d in 1..9) s += c[d]
    return s
}

private fun pe254MinWithDigitSum(i: Int): Long {
    val q = i / 9
    val r = i % 9
    var v = r.toLong()
    repeat(q) { v = v * 10 + 9 }
    return v
}

private fun pe254DigitsMsb(F: Long, out: IntArray): Int {
    var n = 1
    var x = F
    while (x >= 10) { x /= 10; n++ }
    x = F
    for (k in n - 1 downTo 0) { out[k] = (x % 10).toInt(); x /= 10 }
    return n
}

/** 等位数排列比较：数位全非零时“小数字用得越多”数越小 → (c1..c8) 字典序大者更优。 */
private fun pe254ArrangementLess(a: LongArray, b: LongArray): Boolean {
    for (d in 1..8) if (a[d] != b[d]) return a[d] > b[d]
    return false
}
