#!/usr/bin/env kotlin
/**
 * Project Euler 248 — Euler's Totient Function Equals 13!（欧拉函数等于 13!）
 *
 * 思路：
 *   记 m = 13! = 6227020800 = 2^10·3^5·5^2·7·11·13，要求 φ(n) = m 的第 150000 个 n。
 *   暴力筛够不着：题面已给出第一个解 n = 6227180929，而解一直排到 3.7×10¹⁰。
 *   正确姿势是**逆欧拉枚举**——从 m 的结构直接构造全部解。
 *
 *   **第一步：素因子的必要条件。** 写 n = ∏ p_i^{a_i}（p_i 互异），由 φ 的可乘性
 *
 *       φ(n) = ∏ p_i^{a_i−1}(p_i−1) = m，
 *
 *   于是每个素因子 p 满足 (p−1) | m，且指数 a 满足 p^{a−1}(p−1) | m。
 *   候选素数因此只可能形如 p = d + 1（d | m）；m 有 1584 个约数，其中 459 个给出素数。
 *
 *   **第二步：按素因子严格递增递归，天然去重。** 状态 (rem, minP, n)：rem 是 φ 中尚未
 *   被消费的因子（初始 rem = m），minP 是上一个素因子（保证递增），n 是已选素因子幂之积。
 *
 *       dfs(rem, minP, n)：
 *         rem = 1        → n 就是一个解；
 *         否则对每个候选素数 p > minP、d = p − 1（先要求 d | rem），
 *         对每个指数 e ≥ 1 满足 q = p^{e−1}(p−1) | rem 的，递归 dfs(rem / q, p, n·p^e)。
 *
 *   **为什么遍历的恰好是全部解、且每个解恰好一次。** 设 n 的素因子递增为 p_1 < … < p_k。
 *   沿 n 的分解走：处理完前 i 个素因子后 rem_i = m / ∏_{j≤i} p_j^{a_j−1}(p_j−1)，它恰是
 *   剩余部分的乘积，故 p_{i+1}^{a_{i+1}−1}(p_{i+1}−1) | rem_i 必然成立——DFS 一定走得到 n。
 *   反过来任何走到 rem = 1 的路径，消费掉的因子之积 = m，即 φ(n) = m。递增约束让每个
 *   解只对应一条路径（素因子集合与各指数由 n 唯一决定），无需去重。每个节点只在 rem 的
 *   约数表上扫描（约数表按值缓存，rem 必是 m 的约数），候选数从 459 收到 τ(rem)。
 *
 *   **规模（实测）：** m = 13! 时搜索节点 2612581 个、解 182752 个，最小 6227180929
 *   （与题面一致），最大 37020293310，全部解之和 3127106224790235。
 *   解满足 n ≤ R·m，其中 R = ∏ p/(p−1) ≤ 6（见 analysis.md 的比值上界），
 *   故一切中间量都在 Long 内；代码里仍留 n ≤ 8m 的断言兜底。
 *
 * 旁证：
 *   1. 题面给出的第一个解 φ(6227180929) = 13!，程序按定义试除分解复核。
 *   2. brute-force.kt 走三条与逆欧拉结构无关/弱相关的路线：
 *      · 朴素全量枚举：不用约数表，每个节点扫全部 459 个候选素数，给出同样的
 *        182752 个解、和 3127106224790235、全表 FNV = -9063791935501361070、第 150000 个；
 *      · [0, 10⁸] 全区间线性筛，对 m = 8!/9!/10! 列全解（359/1138/3802 个），与本程序逐项一致；
 *      · 13! 尺度的分段 φ 筛窗口（[6227180929, +2×10⁷) 234 个解、
 *        [23507044290, +2×10⁷) 178 个解）完全绕开逆欧拉逻辑，与本程序窗口内结果逐项一致。
 *   3. 全部 182752 个解用候选素数表逐个分解、按定义复核 φ(n) = 13!，并检查严格递增无重复。
 *
 * 答案：23507044290
 * 复杂度：1584 个约数 / 459 个候选素数；DFS 节点 2.6×10⁶，节点内只扫 rem 的约数表；
 *         预热后单次完整求解的实测耗时见 main 输出。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val M13 = 6_227_020_800L
private const val RANK = 150_000

// ---------------------------------------------------------------- 模算术与素性判定

/** 模乘，m < 2³³：把乘数拆成 16 位高低块，各步中间量 < 2⁵⁰，稳在 Long 内 */
private fun mulMod(a: Long, b: Long, m: Long): Long {
    val b1 = b ushr 16
    val b0 = b and 0xFFFFL
    return (((a * b0) % m) + (((a * b1) % m) shl 16)) % m
}

private fun powMod(base: Long, exp: Long, m: Long): Long {
    var r = 1L
    var x = base % m
    var e = exp
    while (e > 0) {
        if (e and 1L == 1L) r = mulMod(r, x, m)
        x = mulMod(x, x, m)
        e = e ushr 1
    }
    return r
}

/** 确定性 Miller–Rabin：底数 2..37 覆盖 n < 3.3×10²⁴，本题只用来判 p = d + 1 ≤ 6.3×10⁹ */
private fun isPrime(n: Long): Boolean {
    if (n < 2) return false
    for (p in longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)) {
        if (n == p) return true
        if (n % p == 0L) return false
    }
    var d = n - 1
    var r = 0
    while (d and 1L == 0L) { d = d ushr 1; r++ }
    for (a in longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)) {
        var x = powMod(a, d, n)
        if (x == 1L || x == n - 1) continue
        var witness = true
        for (i in 1 until r) {
            x = mulMod(x, x, n)
            if (x == n - 1) { witness = false; break }
        }
        if (witness) return false
    }
    return true
}

// ---------------------------------------------------------------- 素数表 / φ 复核 / 分解

private fun primesUpTo(limit: Int): IntArray {
    val comp = BooleanArray(limit + 1)
    val out = ArrayList<Int>()
    for (i in 2..limit) {
        if (!comp[i]) {
            out.add(i)
            if (i.toLong() * i <= limit) {
                var j = i * i
                while (j <= limit) { comp[j] = true; j += i }
            }
        }
    }
    return out.toIntArray()
}

/** 试除到 √n 即可覆盖 n ≤ 3.7×10¹⁰（√n < 2×10⁵）；1e6 的素数表用于分解 m 与按定义复核 */
private val SMALL_PRIMES: IntArray by lazy { primesUpTo(1_000_000) }

/** 按定义计算 φ(n)（试除分解），仅用于复核 */
private fun phiByDefinition(n0: Long): Long {
    var n = n0
    var phi = n0
    for (p in SMALL_PRIMES) {
        val pl = p.toLong()
        if (pl * pl > n) break
        if (n % pl == 0L) {
            while (n % pl == 0L) n /= pl
            phi -= phi / pl
        }
    }
    if (n > 1L) phi -= phi / n
    return phi
}

private fun factorize(n0: Long): Map<Long, Int> {
    val out = LinkedHashMap<Long, Int>()
    var n = n0
    for (p in SMALL_PRIMES) {
        val pl = p.toLong()
        if (pl * pl > n) break
        if (n % pl == 0L) {
            var e = 0
            while (n % pl == 0L) { n /= pl; e++ }
            out[pl] = e
        }
    }
    if (n > 1L) out[n] = (out[n] ?: 0) + 1
    return out
}

private fun divisorsOf(m: Long): LongArray {
    var divs = longArrayOf(1L)
    for ((p, e) in factorize(m)) {
        val add = ArrayList<Long>()
        var pk = 1L
        repeat(e) { pk *= p; for (d in divs) add.add(d * pk) }
        divs = (divs.toMutableList() + add).toLongArray()
    }
    divs.sort()
    return divs
}

/** 全部候选素数 p = d + 1（d | m 且 d + 1 为素数），升序 */
private fun candidatePrimes(m: Long): LongArray {
    val out = ArrayList<Long>()
    for (d in divisorsOf(m)) if (isPrime(d + 1)) out.add(d + 1)
    return out.toLongArray()
}

// ---------------------------------------------------------------- 可增长 Long 数组

private class LongBag(cap: Int = 1 shl 20) {
    var a = LongArray(cap)
        private set
    var size = 0
        private set
    fun add(x: Long) {
        if (size == a.size) a = a.copyOf(a.size * 2)
        a[size++] = x
    }
    fun sorted(): LongArray = a.copyOf(size).also { it.sort() }
}

// ---------------------------------------------------------------- 逆欧拉枚举

/**
 * 全部 n > 1 使 φ(n) = m，升序返回。递归细节见文件头「思路」。
 * 节点内遍历 rem 的约数表（rem 必为 m 的约数，故约数表可预缓存），
 * 只对 d + 1 为素数的 d 展开；指数 e 逐个试 q = p^{e−1}(p−1)。
 */
private fun inversePhi(m: Long): LongArray {
    if (m <= 1L) error("本题 m > 1")

    val divs = divisorsOf(m)
    val primeShift = HashMap<Long, Boolean>(divs.size * 2)
    for (d in divs) primeShift[d] = isPrime(d + 1)

    // 每个约数 r 的约数表（含 1 与 r），只建一次
    val divsOf = HashMap<Long, LongArray>(divs.size * 2)
    for (r in divs) {
        val list = ArrayList<Long>()
        for (d in divs) {
            if (d > r) break
            if (r % d == 0L) list.add(d)
        }
        divsOf[r] = list.toLongArray()
    }

    val sol = LongBag(1 shl 18)
    var nodes = 0L
    var checks = 0L

    fun dfs(rem: Long, minP: Long, n: Long) {
        nodes++
        if (rem == 1L) { sol.add(n); return }
        for (d in divsOf[rem]!!) {          // d = p − 1 的候选
            checks++
            val p = d + 1
            if (p <= minP) continue         // 保持素因子严格递增
            if (primeShift[d] != true) continue
            var q = d                        // q = p^{e−1}(p−1)
            var pe = p                       // pe = p^e
            while (q <= rem && rem % q == 0L) {
                val nr = rem / q
                val nn = n * pe
                if (nn <= 0L || nn > 8L * m) error("中间量越界：n = $nn（不应发生）")
                if (nr == 1L) sol.add(nn) else dfs(nr, p, nn)
                if (q > rem / p) break       // 下一个 q 必然 > rem，且防乘溢出
                q *= p
                pe *= p
            }
        }
    }

    dfs(m, 1L, 1L)
    lastNodeCount = nodes
    lastCandidateChecks = checks
    return sol.sorted()
}

private var lastNodeCount = 0L
private var lastCandidateChecks = 0L

// ---------------------------------------------------------------- 校验工具

/** FNV-1a 64 位哈希（与 brute-force.kt 同算法，用来压缩全表比对） */
private fun fnv(xs: LongArray): Long {
    var h = -3_750_763_034_362_895_579L     // 0xCBF29CE484222325
    for (x in xs) h = (h xor x) * 1_099_511_628_211L
    return h
}

/** 用候选素数表把 n 分解、按定义复核 φ(n) = m；返回不合法解的个数 */
private fun verifyByDefinition(all: LongArray, m: Long): Int {
    val cands = candidatePrimes(m)
    var bad = 0
    for (n in all) {
        var rest = n
        var phi = 1L
        for (p in cands) {
            if (p > rest) break
            if (rest % p == 0L) {
                var pa = 1L                       // p^a
                while (rest % p == 0L) { rest /= p; pa *= p }
                phi *= (pa / p) * (p - 1)         // p^{a−1}(p−1)
            }
        }
        if (rest != 1L || phi != m) bad++        // 残余必为 1（所有素因子都在候选表内）
    }
    return bad
}

private fun checkSmallCase(m: Long, count: Int, sum: Long, first: Long, last: Long, name: String) {
    val xs = inversePhi(m)
    check(xs.size == count) { "$name 解数 ${xs.size} != $count" }
    check(xs.sum() == sum) { "$name 解之和 ${xs.sum()} != $sum" }
    check(xs.first() == first) { "$name 最小解 ${xs.first()} != $first" }
    check(xs.last() == last) { "$name 最大解 ${xs.last()} != $last" }
    println("  · φ(n) = $name：${xs.size} 个解，最小 ${xs.first()}，最大 ${xs.last()}，和 ${xs.sum()}（与 [0,10⁸] 全区间 φ 筛一致）")
}

private fun windowStats(all: LongArray, lo: Long, hi: Long): Triple<Int, Long, Long> {
    val win = all.filter { it in lo until hi }
    return Triple(win.size, win.sum(), fnv(win.toLongArray()))
}

// ---------------------------------------------------------------- main

private fun main() {
    println("13! = $M13 = ${factorize(M13).entries.joinToString("·") { (p, e) -> if (e == 1) "$p" else "$p^$e" }}")
    val cands = candidatePrimes(M13)
    println("候选素数（p − 1 | 13!）共 ${cands.size} 个：${cands.take(12)} … ${cands.takeLast(4)}")

    // ---- 自检 1：题面给出的第一个解 ----
    val phiFirst = phiByDefinition(6_227_180_929L)
    check(phiFirst == M13) { "φ(6227180929) = $phiFirst != 13!" }
    println("自检：φ(6227180929) = $phiFirst = 13!（题面给出的第一个解）")

    // ---- 自检 2：小模数与 [0,10⁸] 全区间 φ 筛逐项一致（对照值来自 brute-force.kt） ----
    println("小模数对照：")
    checkSmallCase(40_320L, 359, 39_577_362L, 40_723L, 210_210L, "8!")
    checkSmallCase(362_880L, 1_138, 1_145_089_419L, 364_087L, 1_891_890L, "9!")
    checkSmallCase(3_628_800L, 3_802, 38_464_195_715L, 3_632_617L, 19_969_950L, "10!")

    // ---- 正式求解 ----
    val all = inversePhi(M13)
    check(all.size == 182_752) { "解数 ${all.size} 与朴素枚举 182752 不符" }
    check(all.first() == 6_227_180_929L) { "最小解 ${all.first()} 与题面不符" }
    check(all.last() == 37_020_293_310L) { "最大解 ${all.last()} 与朴素枚举不符" }
    check(all.sum() == 3_127_106_224_790_235L) { "解之和 ${all.sum()} 与朴素枚举不符" }
    check(fnv(all) == -9_063_791_935_501_361_070L) { "全表 FNV 与朴素枚举不符" }
    for (i in 1 until all.size) check(all[i] > all[i - 1]) { "第 $i 个解未严格递增" }
    println("全部解：${all.size} 个，最小 ${all.first()}，最大 ${all.last()}，和 ${all.sum()}")
    println("搜索节点数 ${lastNodeCount}，候选检查 ${lastCandidateChecks} 次，全表 FNV = ${fnv(all)}（与朴素全量枚举一致）")

    // ---- 自检 3：两个 13! 尺度的分段 φ 筛窗口逐项一致（对照值来自 brute-force.kt） ----
    val w1 = windowStats(all, 6_227_180_929L, 6_247_180_929L)
    check(w1 == Triple(234, 1_459_915_031_194L, -1_268_233_085_750_631_297L)) { "窗口 1 不符：$w1" }
    val w2 = windowStats(all, 23_507_044_290L, 23_527_044_290L)
    check(w2 == Triple(178, 4_185_841_643_692L, -4_678_337_864_209_862_779L)) { "窗口 2 不符：$w2" }
    println("分段 φ 筛窗口对照：")
    println("  · [6227180929, +2×10⁷)：234 个解，和 ${w1.second}，FNV ${w1.third}")
    println("  · [23507044290, +2×10⁷)：178 个解，和 ${w2.second}，FNV ${w2.third}")

    // ---- 自检 4：全部解按定义复核 ----
    val bad = verifyByDefinition(all, M13)
    check(bad == 0) { "有 $bad 个解不满足 φ(n) = 13!" }
    println("全量复核：${all.size} 个解逐个按定义验证 φ(n) = 13!，全部通过")

    // ---- 答案 ----
    val answer = all[RANK - 1]
    check(phiByDefinition(answer) == M13) { "答案的 φ 复核失败" }
    val below = all.take(RANK - 1).size
    println("第 $RANK 个解（答案）= $answer，其 φ = ${phiByDefinition(answer)}，小于它的解恰有 $below 个")

    // ---- 计时：JIT 预热后 3 次完整求解（含排序）取中位数 ----
    inversePhi(M13)
    inversePhi(M13)
    val times = DoubleArray(3)
    for (k in times.indices) {
        val t0 = System.nanoTime()
        val again = inversePhi(M13)
        times[k] = (System.nanoTime() - t0) / 1e6
        check(fnv(again) == fnv(all)) { "计时循环结果漂移" }
    }
    val ms = times.sorted()[1]
    println("optimized: ${"%.0f".format(ms)} ms/次（JIT 预热后 3 次 ${times.joinToString("/") { "%.0f".format(it) }}，取中位）")
    println("check() 全部通过")
}
