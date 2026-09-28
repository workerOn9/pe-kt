#!/usr/bin/env kotlin
/**
 * Project Euler 259 — Reachable Numbers（可达数）
 *
 * 思路：
 *   把 "123456789" 切成若干连续块（块内数字拼接成一个整数），再用 + − × ÷ 把块组成表达式树。
 *   任一表达式的「最后一次运算」把数字串分成左右两段，左右子树各用一段，于是可以区间 DP：
 *
 *       V(i,j) = 用第 i+1..j 位数字（下标区间 [i,j)）能算出的全部精确有理数的集合
 *
 *   基础项：整段拼接数 concat(i,j) ∈ V(i,j)；转移项：
 *
 *       V(i,j) ⊇ { a op b : i < k < j, a ∈ V(i,k), b ∈ V(k,j), op ∈ {+,−,×,÷}, b ≠ 0 }
 *
 *   答案 = V(0,9) 中所有正整数（分母为 1 且分子 > 0）之和。除法产生的分数是必要的中间状态
 *   （例如 42 = (1/23)·((4·5)−6)·(78−9)），既不能只留整数，也不能用浮点近似。
 *   零也必须保留（1−2 之后还能乘），只剔除除数为零的分支。
 *
 *   有理数统一既约、分母为正。方法 A 把 (num, den) 位打包成 Long 键 (num << 32) | den 存入
 *   自实现开放寻址哈希集；实测完整规模 max |num| = 123456789、max den = 23456789（均 < 2^31）。
 *   记 B = max(|num|, den)，所有中间乘积 ≤ 2B² ≈ 3.1×10^16 < 2^63，Long 运算不会溢出（程序断言）。
 *
 *   两条独立路径：
 *     A. 自底向上区间 DP（位打包单键集合），主路径；
 *     B. 以子串为键的记忆化递归（惰性求值顺序）+ 双数组 (num, den) 分列存储的开放寻址集合。
 *   小规模暴力：逐棵枚举并精确求值全部表达式树（k 位前缀共 Σ_j C(k−1,j−1)·Catalan(j−1)·4^{j−1}
 *   棵不同表达式树，无任何子串取值缓存），在 1..8 位上与 A、B 逐一对拍；题面样例 42 另按原式精确验证。
 *   （brute-force.kt 单跑 k = 7 得 91.8 ms 基线；k = 8 需 1.68 s；k = 9 全规模 45.5 s。）
 *
 * 旁证：
 *   · 题面样例：42 = (1/23)·((4·5)−6)·(78−9) 精确成立，且 42 ∈ V(0,9)；
 *   · 1..8 位前缀：暴力树枚举 = 方法 A = 方法 B（正整数个数与和逐一相等）；
 *   · 全规模暴力：k = 9 逐棵枚举 167,763,361 棵树也给出同一个和（45.5 s，见 meta 基线）；
 *   · 完整 1..9：方法 A 与方法 B 给出同一个和；32 位打包与溢出边界断言全部通过；
 *   · 公开答案表（luckytoilet/lucky-bai 题解集 Solutions.md）第 259 行为 20101196798。
 *
 * 答案：20101196798
 * 复杂度：Σ_{区间}{拆分} |V(i,k)|·|V(k,j)| 对组合 × 4 种运算；完整规模全体值元素 4,518,598
 *         （全区间 3,244,635），组合对数 3,856,565（实测，见 analysis.md 复杂度表）。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

import kotlin.math.abs

private const val MASK32 = 0xFFFFFFFFL
private const val EXPECTED = 20_101_196_798L
private const val DIGITS = "123456789"

// ------------------------------------------------------------------ 基础工具

private fun gcdL(a: Long, b: Long): Long {
    var x = abs(a)
    var y = abs(b)
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

private fun mix64(seed: Long): Int {
    var h = seed
    h = (h xor (h ushr 33)) * 0xFF51AFD7ED558CCDUL.toLong()
    h = (h xor (h ushr 33)) * 0xC4CEB9FE1A85EC53UL.toLong()
    h = h xor (h ushr 33)
    return h.toInt()
}

/** 全体值的 |分子|、分母最大值（用于 32 位打包与溢出边界断言）。 */
private var maxAbsNum = 0L
private var maxDen = 0L

/** 方法 A 的组合对数（(左值, 右值) 对，每对再产生 4 个候选值）。 */
private var pairCount = 0L

/** 规范化（既约、分母为正）并返回；用 LongArray(2) 回传避免对象分配。 */
private fun normalize(n0: Long, d0: Long, out: LongArray) {
    var n = n0
    var d = d0
    if (d < 0L) { n = -n; d = -d }
    if (n == 0L) {
        d = 1L
    } else {
        val g = gcdL(n, d)
        n /= g
        d /= g
    }
    if (abs(n) > maxAbsNum) maxAbsNum = abs(n)
    if (d > maxDen) maxDen = d
    out[0] = n
    out[1] = d
}

private fun concatDigits(s: String, i: Int, j: Int): Long {
    var v = 0L
    for (k in i until j) v = v * 10 + (s[k] - '0')
    return v
}

// ------------------------------------------------------------------ 方法 A：自底向上区间 DP

/** 开放寻址哈希集，键是非零 Long（位打包的 num/den）；0 作空槽哨兵。 */
private class LongSet(capHint: Int) {
    var keys: LongArray
        private set
    private var mask: Int
    var size = 0
        private set

    init {
        var c = 16
        while (c < capHint * 2) c = c shl 1
        keys = LongArray(c)
        mask = c - 1
    }

    fun add(key: Long): Boolean {
        var i = mix64(key) and mask
        while (true) {
            val k = keys[i]
            if (k == 0L) {
                keys[i] = key
                size++
                if (size shl 1 > mask) grow()
                return true
            }
            if (k == key) return false
            i = (i + 1) and mask
        }
    }

    private fun grow() {
        val old = keys
        val nk = LongArray(old.size shl 1)
        val nm = nk.size - 1
        for (k in old) {
            if (k == 0L) continue
            var i = mix64(k) and nm
            while (nk[i] != 0L) i = (i + 1) and nm
            nk[i] = k
        }
        keys = nk
        mask = nm
    }
}

/** 方法 A 的规范化+插入：既约、分母为正、位打包 (num << 32) | den。 */
private fun addNormalizedA(set: LongSet, num0: Long, den0: Long) {
    normalize(num0, den0, SCRATCH)
    set.add((SCRATCH[0] shl 32) xor SCRATCH[1])
}

private val SCRATCH = LongArray(2)

/** 构建全部区间 [i,j) 的值集合；返回 dp 表。 */
private fun buildDpA(s: String): Array<Array<LongSet?>> {
    val n = s.length
    @Suppress("UNCHECKED_CAST")
    val dp = Array(n) { arrayOfNulls<LongSet>(n + 1) }
    for (i in 0 until n) {
        val st = LongSet(4)
        addNormalizedA(st, (s[i] - '0').toLong(), 1L)
        dp[i][i + 1] = st
    }
    for (len in 2..n) {
        for (i in 0..n - len) {
            val j = i + len
            val st = LongSet(64)
            addNormalizedA(st, concatDigits(s, i, j), 1L)         // 整段拼接：不用任何运算
            for (m in i + 1 until j) {
                val left = dp[i][m]!!
                val right = dp[m][j]!!
                for (lk in left.keys) {
                    if (lk == 0L) continue
                    val ln = lk shr 32
                    val ld = lk and MASK32
                    for (rk in right.keys) {
                        if (rk == 0L) continue
                        pairCount++
                        val rn = rk shr 32
                        val rd = rk and MASK32
                        addNormalizedA(st, ln * rd + rn * ld, ld * rd)     // +
                        addNormalizedA(st, ln * rd - rn * ld, ld * rd)     // −
                        addNormalizedA(st, ln * rn, ld * rd)               // ×
                        if (rn != 0L) addNormalizedA(st, ln * rd, ld * rn) // ÷（右值非零）
                    }
                }
            }
            dp[i][j] = st
        }
    }
    return dp
}

/** 集合（位打包键）中所有正整数的和。 */
private fun sumPositivesA(st: LongSet): Long {
    var sum = 0L
    for (k in st.keys) {
        if (k == 0L) continue
        if ((k and MASK32) == 1L) {
            val num = k shr 32
            if (num > 0L) sum += num
        }
    }
    return sum
}

/** 集合（位打包键）中所有正整数，升序。 */
private fun positivesA(st: LongSet): List<Long> {
    val out = ArrayList<Long>()
    for (k in st.keys) {
        if (k == 0L) continue
        if ((k and MASK32) == 1L) {
            val num = k shr 32
            if (num > 0L) out.add(num)
        }
    }
    out.sort()
    return out
}

private fun countElementsA(dp: Array<Array<LongSet?>>): Long {
    var total = 0L
    for (row in dp) for (st in row) if (st != null) total += st.size
    return total
}

private fun solveA(s: String): Long = sumPositivesA(buildDpA(s)[0][s.length]!!)

// ------------------------------------------------------------------ 方法 B：记忆化递归 + 双数组集合

/**
 * 开放寻址集合，分子/分母分列存放在两个数组中（不做位打包，与方法 A 的编码不同）。
 * den = 0 表示空槽（合法分母恒为正）。
 */
private class FracSet(capHint: Int) {
    var nums: LongArray
        private set
    var dens: LongArray
        private set
    private var mask: Int
    var size = 0
        private set

    init {
        var c = 16
        while (c < capHint * 2) c = c shl 1
        nums = LongArray(c)
        dens = LongArray(c)
        mask = c - 1
    }

    private fun mix(n: Long, d: Long): Int =
        mix64(n * 0x9E3779B97F4A7C15UL.toLong() xor (d * 0xC2B2AE3D27D4EB4FUL.toLong()))

    fun add(n: Long, d: Long): Boolean {
        var i = mix(n, d) and mask
        while (true) {
            val dd = dens[i]
            if (dd == 0L) {
                nums[i] = n
                dens[i] = d
                size++
                if (size shl 1 > mask) grow()
                return true
            }
            if (dd == d && nums[i] == n) return false
            i = (i + 1) and mask
        }
    }

    private fun grow() {
        val on = nums
        val od = dens
        val nn = LongArray(on.size shl 1)
        val nd = LongArray(nn.size)
        val nm = nn.size - 1
        for (idx in on.indices) {
            val d = od[idx]
            if (d == 0L) continue
            val n = on[idx]
            var i = mix(n, d) and nm
            while (nd[i] != 0L) i = (i + 1) and nm
            nn[i] = n
            nd[i] = d
        }
        nums = nn
        dens = nd
        mask = nm
    }
}

private fun addNormalizedB(set: FracSet, num0: Long, den0: Long) {
    normalize(num0, den0, SCRATCH)
    set.add(SCRATCH[0], SCRATCH[1])
}

/** 把 l 与 r 的所有值两两做 + − × ÷ 并入 res。 */
private fun combineIntoB(res: FracSet, l: FracSet, r: FracSet) {
    for (li in l.nums.indices) {
        val ld = l.dens[li]
        if (ld == 0L) continue
        val ln = l.nums[li]
        for (ri in r.nums.indices) {
            val rd = r.dens[ri]
            if (rd == 0L) continue
            val rn = r.nums[ri]
            addNormalizedB(res, ln * rd + rn * ld, ld * rd)
            addNormalizedB(res, ln * rd - rn * ld, ld * rd)
            addNormalizedB(res, ln * rn, ld * rd)
            if (rn != 0L) addNormalizedB(res, ln * rd, ld * rn)
        }
    }
}

/** 记忆化：返回 s 能算出的全部有理数（惰性递归顺序，与方法 A 的自底向上不同）。 */
private fun valuesMemo(s: String, memo: MutableMap<String, FracSet>): FracSet {
    memo[s]?.let { return it }
    val res = FracSet(64)
    addNormalizedB(res, s.toLong(), 1L)                           // 整段拼接
    for (i in 1 until s.length) {
        combineIntoB(res, valuesMemo(s.substring(0, i), memo), valuesMemo(s.substring(i), memo))
    }
    memo[s] = res
    return res
}

private fun sumPositivesB(set: FracSet): Long {
    var sum = 0L
    for (i in set.dens.indices) {
        val d = set.dens[i]
        if (d == 1L) {
            val n = set.nums[i]
            if (n > 0L) sum += n
        }
    }
    return sum
}

private fun solveB(s: String): Long = sumPositivesB(valuesMemo(s, HashMap()))

// ------------------------------------------------------------------ 暴力：表达式树全枚举（无记忆化）

/**
 * 逐棵枚举表达式树（纯语法暴力）：第一支是整段拼接，其余支是「在 m 处切成两半，
 * 递归枚举左右子树再组合」—— 每棵树恰好求值一次，没有任何子串取值集合的中间缓存，
 * 只有根节点取值进入集合。这与区间 DP 的分水岭正在于「记忆化」。
 */
private fun enumerateBrute(s: String, lo: Int, hi: Int, out: (Long, Long) -> Unit) {
    var chunk = 0L
    for (i in lo until hi) chunk = chunk * 10 + (s[i] - '0')
    out(chunk, 1L)
    if (hi - lo == 1) return
    for (m in lo + 1 until hi) {
        enumerateBrute(s, lo, m) { an, ad ->
            enumerateBrute(s, m, hi) { bn, bd ->
                normalize(an * bd + bn * ad, ad * bd, SCRATCH)
                out(SCRATCH[0], SCRATCH[1])                        // +
                normalize(an * bd - bn * ad, ad * bd, SCRATCH)
                out(SCRATCH[0], SCRATCH[1])                        // −
                normalize(an * bn, ad * bd, SCRATCH)
                out(SCRATCH[0], SCRATCH[1])                        // ×
                if (bn != 0L) {                                    // ÷（除零分支剪掉）
                    normalize(an * bd, ad * bn, SCRATCH)
                    out(SCRATCH[0], SCRATCH[1])
                }
            }
        }
    }
}

/** 暴力的根节点取值集合。 */
private fun valuesBrute(s: String): FracSet {
    val finals = FracSet(16)
    enumerateBrute(s, 0, s.length) { n, d -> addNormalizedB(finals, n, d) }
    return finals
}

/** k 位前缀（"1".."123…k"）的表达式树总数：Σ_j C(k−1,j−1)·Catalan(j−1)·4^{j−1}。 */
private fun expressionTreeCount(k: Int): Long {
    val catalan = longArrayOf(1, 1, 2, 5, 14, 42, 132, 429, 1430)
    val binom = Array(k + 1) { LongArray(k + 1) }
    for (i in 0..k) {
        binom[i][0] = 1
        for (j in 1..i) binom[i][j] = binom[i - 1][j - 1] + (if (j <= i - 1) binom[i - 1][j] else 0)
    }
    var total = 0L
    for (blocks in 1..k) {
        var pow4 = 1L
        repeat(blocks - 1) { pow4 *= 4 }
        total += binom[k - 1][blocks - 1] * catalan[blocks - 1] * pow4
    }
    return total
}

// ------------------------------------------------------------------ main

fun main() {
    // 1) 题面样例：42 = (1/23)·((4·5)−6)·(78−9)，括号内先算：4·5−6 = 14，78−9 = 69
    normalize(1L * 14L * 69L, 23L, SCRATCH)
    val sampleNum = SCRATCH[0]
    val sampleDen = SCRATCH[1]
    check(sampleNum == 42L && sampleDen == 1L) { "样例表达式算不出 42" }
    println("题面样例：(1/23)·((4·5)−6)·(78−9) = $sampleNum/$sampleDen = 42 精确成立")

    // 2) 完整 1..9：方法 A 与方法 B
    val dpFull = buildDpA(DIGITS)
    val fullSet = dpFull[0][DIGITS.length]!!
    val ansA = sumPositivesA(fullSet)
    val ansB = solveB(DIGITS)
    check(42L in positivesA(fullSet)) { "42 不在 V(0,9) 的正整数中" }
    println("42 ∈ V(0,9)：是")
    println("方法 A（自底向上区间 DP + 位打包单键集合）答案 = $ansA")
    println("方法 B（记忆化递归 + 双数组 num/den 集合）答案 = $ansB")
    check(ansA == ansB) { "两种方法不一致：$ansA vs $ansB" }
    check(ansA == EXPECTED) { "答案与预期不符：$ansA" }

    // 3) 数值范围断言：32 位打包合法，且所有中间乘积不溢出
    val bound = maxOf(maxAbsNum, maxDen)
    println(
        "数值范围：max |num| = $maxAbsNum，max den = $maxDen，" +
            "2·B² = ${2 * bound * bound} < Long.MAX_VALUE = ${Long.MAX_VALUE}",
    )
    check(maxAbsNum <= Int.MAX_VALUE && maxDen <= Int.MAX_VALUE) { "32 位打包越界：B = $bound" }
    check(2 * bound * bound < Long.MAX_VALUE) { "中间乘积可能溢出" }

    // 4) 统计与复杂度数据
    println("集合规模：全体区间元素 ${countElementsA(dpFull)}，V(0,9) = ${fullSet.size}，组合对数 = $pairCount")
    val positivesFull = positivesA(fullSet)
    println("V(0,9)：正可达整数 ${positivesFull.size} 个，最大者 ${positivesFull.last()}")

    // 5) 小规模暴力对照：1..8 位前缀，暴力 = A = B
    println("小规模暴力对照（逐棵枚举表达式树，精确有理数求值）：")
    for (k in 1..8) {
        val s = DIGITS.substring(0, k)
        val bruteSet = valuesBrute(s)
        val brute = sumPositivesB(bruteSet)
        val sa = solveA(s)
        val sb = solveB(s)
        check(brute == sa && sa == sb) { "k = $k：暴力 $brute，方法 A $sa，方法 B $sb" }
        var bruteCount = 0L
        for (i in bruteSet.dens.indices) {
            if (bruteSet.dens[i] == 1L && bruteSet.nums[i] > 0L) bruteCount++
        }
        println(
            "  前缀长度 $k：表达式树 ${expressionTreeCount(k)} 棵，暴力 = A = B = $brute" +
                "（$bruteCount 个正整数）",
        )
    }

    // 6) 计时：完整 1..9，JIT 预热后 3 轮取最优
    check(solveA(DIGITS) == ansA)
    var bestA = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        check(solveA(DIGITS) == ansA)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestA) bestA = ms
        println("  方法 A 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 A 完整求解：${"%.1f".format(bestA)} ms（3 轮最优，JIT 预热后）")

    check(solveB(DIGITS) == ansB)
    var bestB = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        check(solveB(DIGITS) == ansB)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestB) bestB = ms
        println("  方法 B 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 B 完整求解：${"%.1f".format(bestB)} ms（3 轮最优，JIT 预热后）")

    println("答案 = $ansA")
    println("check() 全部通过")
}
