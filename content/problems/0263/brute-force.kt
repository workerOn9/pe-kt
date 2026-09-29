#!/usr/bin/env kotlin
/**
 * Project Euler 263 — An Engineers' Dream Come True：直接暴力对照（独立实现）
 *
 * 完全按定义来做，不使用 solution.kt 的任何剪枝/建模技巧：
 *   · 实用数判定按定义（枚举全部因数、升序贪心检验 d_i ≤ 1 + Σ_{j<i} d_j，等价于子集和可达性）；
 *   · 素数表用朴素的整段埃氏筛（只存奇数位），扫描所有奇数 p，逐位检查窗口
 *     [p, p+18] 是否恰好包含 p, p+6, p+12, p+18 四个素数（不预先使用 p ≡ 1,11 (mod 30) 的结论）；
 *   · 命中候选后依次判定 n−8, n−4, n, n+4, n+8（n = p+9）是否全为实用数。
 *
 * 规模：整段筛到 2.5×10⁸，可以在该范围内复现题面锚点（首个 sexy pair (23,29)）
 * 并给出第一个工程师的天堂 n = 219 869 980；外推到 1.15×10⁹ 时标记/扫描量约 ×4.6。
 *
 * 输出：
 *   · 实用数定义与因数贪心判定的逐步比对（n ≤ 3000）；
 *   · 首个 sexy pair、≤10⁸ 与 ≤2.5×10⁸ 的候选数、第一个 paradise；
 *   · JIT 预热后 3 轮最优毫秒数（meta.bruteForceBaselineMs 的数据来源，口径为 2.5×10⁸）。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

private const val LIMIT = 250_000_000L
private const val LIMIT_1E8 = 100_000_000L

private fun sieveBasePrimes(n: Int): IntArray {
    val comp = BooleanArray(n + 1)
    val out = ArrayList<Int>()
    for (i in 2..n) {
        if (!comp[i]) {
            out.add(i)
            if (i.toLong() * i <= n) {
                var j = i * i
                while (j <= n) { comp[j] = true; j += i }
            }
        }
    }
    return out.toIntArray()
}

private val BASE: IntArray = sieveBasePrimes(16_000)

/** 按定义：枚举全部因数，升序贪心（等价于「1..n 全部可表」）。 */
private fun isPracticalByDefinition(n: Long): Boolean {
    if (n == 1L) return true
    if (n and 1L == 1L) return false
    var m = n
    val ps = IntArray(16)
    val es = IntArray(16)
    var k = 0
    for (pr in BASE) {
        val p = pr.toLong()
        if (p * p > m) break
        if (m % p == 0L) {
            var e = 0
            while (m % p == 0L) { m /= p; e++ }
            ps[k] = pr
            es[k] = e
            k++
        }
    }
    if (m > 1L) { ps[k] = m.toInt(); es[k] = 1; k++ }
    var divs = LongArray(1) { 1L }
    for (i in 0 until k) {
        val pr = ps[i].toLong()
        val e = es[i]
        val next = LongArray(divs.size * (e + 1))
        var idx = 0
        var pk = 1L
        for (ee in 0..e) {
            for (d in divs) next[idx++] = d * pk
            pk *= pr
        }
        divs = next
    }
    divs.sort()
    var sum = divs[0]
    for (i in 1 until divs.size) {
        if (divs[i] > sum + 1) return false
        sum += divs[i]
    }
    return true
}

/** 子集和可达性（定义直译），只适合小 n。 */
private fun isPracticalBySubsetSum(n: Int): Boolean {
    val reach = BooleanArray(n + 1)
    reach[0] = true
    for (d in 1..n) {
        if (n % d == 0) for (s in n downTo d) if (reach[s - d]) reach[s] = true
    }
    for (s in 1..n) if (!reach[s]) return false
    return true
}

private data class Brute(
    val candidates: LongArray,
    val paradises: LongArray,
    val firstSexy: Long,
)

private fun brute(limit: Long): Brute {
    val nbits = (limit / 2).toInt() + 1
    val bits = LongArray(nbits / 64 + 1) { -1L }
    bits[0] = bits[0] and 1L.inv()                        // 数 1
    for (q in BASE) {
        if (q == 2) continue
        val ql = q.toLong()
        if (ql * ql > limit) break
        var idx = ((ql * ql - 1) / 2).toInt()
        while (idx < nbits) {
            bits[idx ushr 6] = bits[idx ushr 6] and (1L shl (idx and 63)).inv()
            idx += q
        }
    }
    fun prime(x: Long): Boolean {
        if (x == 2L) return true
        if (x < 2L || x and 1L == 0L) return false
        val idx = ((x - 1) / 2).toInt()
        return bits[idx ushr 6] and (1L shl (idx and 63)) != 0L
    }
    // 首个 sexy pair
    var firstSexy = 0L
    var s = 3L
    while (firstSexy == 0L && s + 6 <= limit) {
        if (prime(s) && prime(s + 6) && !prime(s + 2) && !prime(s + 4)) firstSexy = s
        s += 2
    }
    // 扫描所有奇数 p 的窗口
    val candidates = ArrayList<Long>()
    val paradises = ArrayList<Long>()
    var p = 3L
    while (p + 18 <= limit) {
        if (prime(p) && prime(p + 6) && prime(p + 12) && prime(p + 18) &&
            !prime(p + 2) && !prime(p + 4) && !prime(p + 8) &&
            !prime(p + 10) && !prime(p + 14) && !prime(p + 16)
        ) {
            candidates.add(p)
            val n = p + 9
            if (isPracticalByDefinition(n - 8) && isPracticalByDefinition(n - 4) &&
                isPracticalByDefinition(n) && isPracticalByDefinition(n + 4) &&
                isPracticalByDefinition(n + 8)
            ) paradises.add(n)
        }
        p += 2
    }
    return Brute(candidates.toLongArray(), paradises.toLongArray(), firstSexy)
}

private fun bestOf3(tag: String, f: () -> Brute): Pair<Double, Brute> {
    var best = Double.MAX_VALUE
    var result: Brute? = null
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val r = f()
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) { best = ms; result = r }
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best to result!!
}

fun main() {
    // 定义锚点：实用数三种判定在 1..3000 一致
    for (n in 1..3000) {
        check(isPracticalBySubsetSum(n) == isPracticalByDefinition(n.toLong())) {
            "n=$n：子集和定义与因数贪心判定不一致"
        }
    }
    check(isPracticalByDefinition(6)) { "6 应为实用数" }
    println("实用数：1..3000 上子集和定义与因数贪心判定逐一一致；6 为实用数")

    val r = brute(LIMIT)
    check(r.firstSexy == 23L) { "首个 sexy pair 应为 (23,29)，实得 (${r.firstSexy},${r.firstSexy + 6})" }
    println("首个 sexy pair = (${r.firstSexy}, ${r.firstSexy + 6})，与题面一致")

    val c1 = r.candidates.count { it <= LIMIT_1E8 }
    val c2 = r.candidates.size
    println("候选四素数窗口：≤10⁸ 有 $c1 个，≤2.5×10⁸ 有 $c2 个")
    println("≤2.5×10⁸ 内的工程师天堂：${r.paradises.toList()}")

    // 预热后计时
    brute(10_000_000L)
    val (ms, _) = bestOf3("暴力整段筛 + 定义判定 ≤2.5×10⁸") { brute(LIMIT) }
    println()
    println("bruteForceBaselineMs 取 ≤2.5×10⁸ 的 ${"%.1f".format(ms)} ms（口径：缩小规模 2.5×10⁸，外推到 1.15×10⁹ 约 ×4.6）")
}
