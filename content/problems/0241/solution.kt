#!/usr/bin/env kotlin
/**
 * Project Euler 241 — Perfection Quotients（完美商）
 *
 * 思路：
 *   p(n) = σ(n)/n。要求 p(n) = k + 1/2，等价于 2σ(n)/n = q 是**奇整数**。
 *
 *   ① q 的取值范围。n ≤ 10^18 至多含 15 个不同素因子（2·3·…·47 = 6.15×10^17 < 10^18，
 *      再乘 53 就超了），故 σ(n)/n ≤ ∏_{p≤47} p/(p−1) < 7.21 < 8；又 σ(n) > n，
 *      故 q = 2σ(n)/n ∈ {3, 5, 7, 9, 11, 13}。对每个 q 单独搜。
 *
 *   ② n 必为偶数（n 奇则 2σ(n) 偶而 qn 奇）。写 n = 2^a·t（a ≥ 1，t 奇），D = 2^{a+1}−1，
 *      σ(n) = D·σ(t)，于是
 *          2·D·σ(t) = q·2^a·t   ⟺   σ(t)/t = q·2^{a−1} / D                  ……(★)
 *      右端约分成既约 A/B（D 奇 ⟹ gcd(2^{a−1}, D) = 1，故 B = D/gcd(q,D) 全是奇因子）。
 *      由 B·σ(t) = A·t 与 gcd(A,B) = 1 得 **B | t**（欧几里得引理）：
 *      「既约分母必整除剩余部分」。于是 m·B ≤ 10^18/2^a 成为规模剪枝。
 *      又 σ(t) > t 要求 A > B。
 *
 *   ③ 素数供给链（本题全部难点）。t = ∏ p_i^{c_i}。由 gcd(p^c, σ(p^c)) = 1 与 2σ(n) = qn：
 *          2σ(p^c)σ(n/p^c) = q·p^c·(n/p^c)  ⟹  p^c | 2σ(n/p^c)
 *      而 n/p^c = 2^a·m_{<i}·m_{>i}（m_{<i}、m_{>i} 分别是 t 里小于 / 大于 p 的部分），
 *      p 奇，故
 *          p_i^{c_i} | D·σ(m_{<i})·σ(m_{>i})                              ……(★★)
 *      读法：**每个素数的幂，要么由 D = 2^{a+1}−1 提供，要么由别的素数幂的 σ 提供**。
 *      麻烦在于「提供者的 σ」可能来自尚未确定的一侧，于是固化顺序被供给关系强制，
 *      而这个顺序**既不是升序也不是降序**——两头剥离都会漏解：
 *        · n = 8910720 = 2^7·3^2·5·7·13·17：7 ∤ 255·σ(3^2)·σ(5)，7 来自 σ(13) = 14；
 *        · n = 17428320 = 2^5·3^2·5·7^2·13·19：链是 3 → 13 → 7 → 19 → 5 之字形，
 *          升序 DFS 与降序 DFS 都漏掉它（10^8 线性筛暴力枚举抓出来的）。
 *      所以正确做法是**不按大小排序，直接在「供给闭合」的素数集合上做集合式 DFS**：
 *      候选 = primes(D·σ(已定部分)) 中尚未使用者，每步任选 P、任选指数 c 固化。
 *
 *   ④ 状态 (m, σ(m) 的分解, 已用素数, A/B)，不变量：t = m·y，gcd(m, y) = 1，
 *      σ(y)/y = A/B，B | y，且 t = m·y ≤ 10^18/2^a。
 *      - 终止：A = B = 1 ⟹ σ(y)/y = 1 ⟹ y = 1（y > 1 时恒有 σ(y) > y），
 *        n = 2^a·m，**直接复核 2σ(n) = qn**（不用中间结论代替判据）。
 *      - 转移：固化 p^c，新既约分数
 *          A'/B' = A·p^c·(p−1) / (B·(p^{c+1}−1))     （再与 gcd 约分）
 *      - 剪枝：m·p^c·B' > 10^18/2^a（B' | y ⟹ y ≥ B'）；gcd(B', m) > 1（y 与 m 互素）；
 *        A' < B'（σ(y) < y 不可能，且 σ(p^c)/p^c 随 c 单调趋近 (p−1)/p，故 c 更大只会
 *        更小，直接 break —— 这就是指数 c 的上界来源）。
 *
 *   σ(n) 可达 ~10^19 超过 Long：m、A、B 全程 java.math.BigInteger；
 *   而**素数本身一定 ≤ t ≤ 10^18 < 2^63**，所以候选表 / 已用表用 Long 键的 TreeMap，
 *   分解结果里超过 10^18 的因子直接丢弃（不可能成为候选）。2^{a+1}−1 ≤ 2^30 与
 *   σ(p^c) ≤ 10^20 用 Pollard-Rho + Miller-Rabin 分解，带缓存。
 *
 * 复杂度：搜索节点数与耗时实测见 analysis.md（节点 ≈ 1.01×10^6，预热后 ≈ 3 s）。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

import java.math.BigInteger
import java.util.TreeMap

private val TWO = BigInteger.valueOf(2)
private val ONE = BigInteger.ONE
private val NM = BigInteger.TEN.pow(18)
private val Q_LIST = intArrayOf(3, 5, 7, 9, 11, 13)
private val SMALL_PRIMES = intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)
private const val MAX_PRIME = 1_000_000_000_000_000_000L   // 10^18：超过它的素数不可能是候选

// ---------------------------------------------------------------- 素因数分解

private fun isPrime(n: Long): Boolean {
    if (n < 2) return false
    for (p in SMALL_PRIMES) {
        if (n == p.toLong()) return true
        if (n % p == 0L) return false
    }
    val n1 = n - 1
    var d = n1
    var s = 0
    while (d % 2L == 0L) { d /= 2L; s++ }
    for (a in SMALL_PRIMES) {
        var x = modPow(a.toLong(), d, n)
        if (x == 1L || x == n1) continue
        var hit = false
        for (i in 1 until s) {
            x = mulMod(x, x, n)
            if (x == n1) { hit = true; break }
        }
        if (!hit) return false
    }
    return true
}

/** 模乘：m ≤ 2^40 时把乘数拆成高 19 位 + 低 19 位（积 < 2^61）；更大的 m
 *  （σ(p^c) 的素因子可达 2×10¹⁸）退回 BigInteger，保证 Miller–Rabin 与 Pollard 全程正确。 */
private fun mulMod(a: Long, b: Long, m: Long): Long {
    if (m <= (1L shl 40)) {
        val b1 = b ushr 19
        val b0 = b and ((1L shl 19) - 1)
        return (((a * b0) % m) + (((a * b1) % m) shl 19)) % m
    }
    return BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)).mod(BigInteger.valueOf(m)).toLong()
}

private fun modPow(b: Long, e: Long, m: Long): Long {
    var r = 1L
    var base = b % m
    var exp = e
    while (exp > 0) {
        if (exp and 1L == 1L) r = mulMod(r, base, m)
        base = mulMod(base, base, m)
        exp = exp shr 1
    }
    return r
}

private fun pollardRho(n: Long): Long {
    if (n % 2L == 0L) return 2L
    val rnd = java.util.concurrent.ThreadLocalRandom.current()
    while (true) {
        val c = rnd.nextLong(1, n - 1)
        var x = 2L
        var y = 2L
        var d = 1L
        while (d == 1L) {
            x = (mulMod(x, x, n) + c) % n
            y = (mulMod(y, y, n) + c) % n
            y = (mulMod(y, y, n) + c) % n
            d = gcd(x - y, n)
        }
        if (d != n) return d
    }
}

private fun gcd(a: Long, b: Long): Long {
    var x = if (a < 0) -a else a
    var y = b
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

private val factCache = HashMap<Long, LongArray>()

private fun factorRec(x: Long, out: ArrayList<Long>) {
    if (x <= 1L) return
    if (isPrime(x)) { out += x; return }
    val d = pollardRho(x)
    factorRec(d, out)
    factorRec(x / d, out)
}

/** 完整分解成素因子多重集（Long），只保留 ≤ 10^18 的因子，带缓存。 */
private fun factor(n: Long): LongArray {
    if (n <= 1L) return LongArray(0)
    factCache[n]?.let { return it }
    val raw = ArrayList<Long>()
    var x = n
    for (p in SMALL_PRIMES) {
        val pl = p.toLong()
        while (x % pl == 0L) { raw += pl; x /= pl }
    }
    if (x > 1L) factorRec(x, raw)
    raw.sort()
    // 压缩成 (素数, 指数) 交替的数组，并丢掉 > 10^18 的素数
    val keep = ArrayList<Long>(raw.size)
    var i = 0
    while (i < raw.size) {
        val p = raw[i]
        var c = 0
        while (i < raw.size && raw[i] == p) { c++; i++ }
        if (p <= MAX_PRIME) { keep += p; keep += c.toLong() }
    }
    val res = keep.toLongArray()
    factCache[n] = res
    return res
}

/** 分解 n（BigInteger）成 (素数, 指数) 交替的 Long 数组。 */
private fun factorBI(n: BigInteger): LongArray = factor(n.toLong())

// ---------------------------------------------------------------- 供给链 DFS

private var qCur = 0L
private var pow2aL = 0L               // 2^a
private var dCurL = 0L                // 2^{a+1} - 1
private var dCur = ONE
private var tMax = NM
private var facD = LongArray(0)
private var windowPrimes = LongArray(0)
private var nodes = 0L
private val solutions = ArrayList<BigInteger>()

private fun mergeCandidates(fs: LongArray): LongArray {
    val set = TreeMap<Long, Int>()
    var i = 0
    while (i < facD.size) { set.merge(facD[i], facD[i + 1].toInt(), Int::plus); i += 2 }
    i = 0
    while (i < fs.size) { set.merge(fs[i], fs[i + 1].toInt(), Int::plus); i += 2 }
    for (p in windowPrimes) set.merge(p, 1, Int::plus)
    val out = LongArray(set.size * 2)
    var j = 0
    for ((p, e) in set) { out[j] = p; out[j + 1] = e.toLong(); j += 2 }
    return out
}

private fun usedContains(used: LongArray, p: Long): Boolean {
    var lo = 0
    var hi = used.size / 2 - 1
    while (lo <= hi) {
        val mid = (lo + hi) ushr 1
        val v = used[mid * 2]
        when {
            v == p -> return true
            v < p -> lo = mid + 1
            else -> hi = mid - 1
        }
    }
    return false
}

/**
 * @param m   t 已确定的部分
 * @param fs  σ(m) 的分解 (素数, 指数)*
 * @param used t 已固化的素数（有序）
 * @param aVal,bVal 剩余部分 y 的 σ(y)/y 的既约分数
 */
private fun dfs(m: BigInteger, fs: LongArray, used: LongArray, aVal: BigInteger, bVal: BigInteger) {
    nodes++
    if (aVal == ONE && bVal == ONE) {
        val n = pow2aL.toBigInteger().multiply(m)
        val twoSigN = TWO.multiply(dCur).multiply(sigmaOf(m))
        if (twoSigN == BigInteger.valueOf(qCur).multiply(n)) solutions += n
        return
    }
    if (m.multiply(bVal) > tMax) return
    val cand = mergeCandidates(fs)
    var i = 0
    while (i < cand.size) {
        val p = cand[i]
        val pl = BigInteger.valueOf(p)
        if ((p and 1L) == 1L && !usedContains(used, p)) {
            val pm1 = pl.subtract(ONE)
            var pPow = ONE            // p^c
            var pw = ONE             // p^c（数值）
            while (true) {
                pPow = pPow.multiply(pl)
                pw = pw.multiply(pl)
                if (m.multiply(pw) > tMax) break
                val pPow1 = pPow.multiply(pl)
                val geoSum = pPow1.subtract(ONE).divide(pm1)          // σ(p^c)
                val num = aVal.multiply(pPow).multiply(pm1)
                val den = bVal.multiply(pPow1.subtract(ONE))
                if (num < den) break
                val g = num.gcd(den)
                val a2 = num.divide(g)
                val b2 = den.divide(g)
                val m2 = m.multiply(pw)
                if (m2.multiply(b2) > tMax) continue
                if (b2.gcd(m) != ONE) continue                      // B' | y 而 gcd(m, y) = 1
                val fs2 = mergeFacs(fs, factorBI(geoSum))
                val used2 = insertUsed(used, p)
                dfs(m2, fs2, used2, a2, b2)
            }
        }
        i += 2
    }
}

/** 把两个已排序的 (素数, 指数)* 数组按指数相加合并。 */
private fun mergeFacs(a: LongArray, b: LongArray): LongArray {
    var i = 0
    var j = 0
    val out = ArrayList<Long>(a.size + b.size + 4)
    while (i < a.size && j < b.size) {
        when {
            a[i] < b[j] -> { out += a[i]; out += a[i + 1]; i += 2 }
            a[i] > b[j] -> { out += b[j]; out += b[j + 1]; j += 2 }
            else -> { out += a[i]; out += a[i + 1] + b[j + 1]; i += 2; j += 2 }
        }
    }
    while (i < a.size) { out += a[i]; out += a[i + 1]; i += 2 }
    while (j < b.size) { out += b[j]; out += b[j + 1]; j += 2 }
    return out.toLongArray()
}

/** 把 p 插入已排序的素数表（去重）。 */
private fun insertUsed(used: LongArray, p: Long): LongArray {
    var lo = 0
    var hi = used.size / 2
    while (lo < hi) {
        val mid = (lo + hi) ushr 1
        if (used[mid * 2] < p) lo = mid + 1 else hi = mid
    }
    if (lo < used.size / 2 && used[lo * 2] == p) return used
    val out = LongArray(used.size + 2)
    System.arraycopy(used, 0, out, 0, lo * 2)
    out[lo * 2] = p
    System.arraycopy(used, lo * 2, out, lo * 2 + 2, used.size - lo * 2)
    return out
}

private fun sigmaOf(n: BigInteger): BigInteger {
    var sig = ONE
    val f = factorBI(n)
    var i = 0
    while (i < f.size) {
        val p = f[i].toBigInteger()
        val e = f[i + 1].toInt()
        var pw = ONE
        var geo = ONE
        for (k in 0 until e) { pw = pw.multiply(p); geo = geo.add(pw) }
        sig = sig.multiply(geo)
        i += 2
    }
    return sig
}

/** 独立复核：2σ(n)/n 必须是奇整数。 */
private fun verify(n: BigInteger): Boolean {
    val v = TWO.multiply(sigmaOf(n))
    return v.mod(n).signum() == 0 && v.divide(n).testBit(0)
}

/** 补一个「窗口」：把 ≤ W 的所有奇素数也当候选，用来堵住供给环漏洞。 */
private fun sieveOddPrimes(w: Int): LongArray {
    if (w < 3) return LongArray(0)
    val comp = BooleanArray(w + 1)
    val out = ArrayList<Long>()
    for (i in 3..w) {
        if (comp[i]) continue
        out += i.toLong()
        var j = i.toLong() * i
        while (j <= w) { comp[j.toInt()] = true; j += i }
    }
    return out.toLongArray()
}

private fun solve(limit: BigInteger, window: Int = 0): List<BigInteger> {
    nodes = 0
    solutions.clear()
    windowPrimes = sieveOddPrimes(window)
    for (qi in Q_LIST) {
        qCur = qi.toLong()
        for (a in 1..59) {
            val pow2a = 1L shl a
            if (pow2a > limit.toLong()) break
            tMax = limit.divide(pow2a.toBigInteger())
            dCurL = (1L shl (a + 1)) - 1
            dCur = dCurL.toBigInteger()
            if (dCur > tMax) break
            val a0 = qCur.toBigInteger().multiply((1L shl (a - 1)).toBigInteger())
            if (a0 < dCur) continue
            facD = factor(dCurL)
            val g = a0.gcd(dCur)
            pow2aL = pow2a
            dfs(ONE, LongArray(0), LongArray(0), a0.divide(g), dCur.divide(g))
        }
    }
    return solutions.distinct().sorted()
}

fun main() {
    var bound = 1.0
    for (p in intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47)) bound *= p.toDouble() / (p - 1)
    println("max sigma(n)/n (n <= 1e18) = $bound  ->  q in {3, 5, 7, 9, 11, 13}")

    // 预热：让 JIT 编译 + 分解缓存热起来，再测
    repeat(3) { solve(BigInteger.TEN.pow(17)) }
    var best = Long.MAX_VALUE
    var res: List<BigInteger> = emptyList()
    repeat(5) {
        val t0 = System.nanoTime()
        res = solve(NM)
        val ms = (System.nanoTime() - t0) / 1_000_000
        if (ms < best) best = ms
    }
    println("n <= 1e18 的解（${res.size} 个）:")
    res.forEach { println("  $it") }
    var sum = BigInteger.ZERO
    res.forEach { sum = sum.add(it) }
    println("sum = $sum")
    println("搜索节点数 = $nodes   预热后耗时 = ${best} ms   复核全部通过 = ${res.all { verify(it) }}")
}
