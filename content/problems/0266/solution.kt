#!/usr/bin/env kotlin
/**
 * Project Euler 266 — Pseudo Square Root（伪平方根）
 *
 * 思路
 * ────
 * 记 p 为所有小于 190 的素数之积（共 42 个素数，p ≈ 10^72.6）。p 无平方因子，所以它的每个
 * 因数都是若干素数的乘积；PSR(p) 就是「不超过 √p 的最大子集乘积」。
 *
 * 直接枚举 2^42 ≈ 4.4×10^12 个子集不可行，采用折半枚举（meet in the middle）：
 *   1. 把 42 个素数分成两半：A = 前 21 个（2…73），B = 后 21 个（79…181），各枚举 2^21 个子集
 *      乘积，递推式 products[i] = products[i & (i-1)] · primes[最低位为 1 的下标]，O(2^21)。
 *   2. 令 S = ⌊√p⌋（BigInteger 牛顿迭代：x ← (x + p/x)/2）。注意 a·b ≤ √p ⟺ a·b ≤ S，
 *      因为 a·b 是整数。
 *   3. 把 A、B 都按精确值排序（BigInteger 全序，全程不用浮点，避免边界误判）。
 *   4. 单调双指针：把 A 升序遍历。a 递增时阈值 S/a 单调递减，所以「满足 a·b ≤ S 的最大 b 在
 *      B 中的下标」只减不增。初始 j 指向 B 末尾，对每个 a：while (a·B[j] > S) j--，
 *      此时 B[j] 就是本 a 下的最优 b（B 升序 + 下标更高的 b 全部对这个 a 非法）。
 *      b = 1 总可行，故 j 不会越界。
 *   5. 对所有 a 的候选 a·B[j] 取最大者即为 PSR(p)；答案 = 该值 mod 10^16。
 *
 *   为什么「每个 a 取局部最大 b」就够：任何子集乘积都可唯一拆成 a·b（a 来自 A 的子集、b 来自
 *   B 的子集），全体候选是 a·b ≤ S 的所有组合；固定 a 时取最大可行 b 最优，再对 a 取最大即
 *   全局最优。
 *
 * 复杂度
 * ──────
 * 枚举 2·2^21 个子集乘积 O(2^21)；两次排序 O(2^21·21) 次 BigInteger 比较；
 * 双指针 O(2^21) 次大整数乘法（数约 105/137 bit，乘积约 242 bit）。
 * 内存 O(2^21)（每半 2^21 个 BigInteger）。相比 2^42 全枚举是指数级加速。
 *
 * 验证
 * ────
 * 1. 题面锚点：PSR(3102) = 47、PSR(12) = 3（按定义从 ⌊√n⌋ 向下试除复现）；
 * 2. 缩小规模三方对照：在 5、6、10、15、20、22 个素数（2,3,5 / 2…7 / <30 / <50 / <71 / <80）
 *    六组上，全子集暴力、方法 A（折半精确双指针）、方法 B（另一划分 + 对数引导的精确搜索）
 *    结果全部一致；
 * 3. 全规模下方法 A 与方法 B 结果一致，且结果同时满足 d | p、d ≤ S、p/d > S（说明 d 之上
 *    没有别的因数，即 d 就是 PSR(p)）。
 *
 * 答案：1096883702440585（PSR(p) mod 10^16，PSR(p) 本身是 73 位乘积的 37 位因数）
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0266/solution.kt && java -cp … SolutionKt
 */

import java.math.BigInteger
import java.util.Arrays

private const val LIMIT = 190
private val TEN16 = BigInteger.valueOf(10_000_000_000_000_000L)

/** 埃氏筛：返回小于 [limit] 的全部素数。 */
private fun primesBelow(limit: Int): IntArray {
    val composite = BooleanArray(limit)
    val out = ArrayList<Int>()
    for (i in 2 until limit) {
        if (!composite[i]) {
            out.add(i)
            var j = i.toLong() * i
            while (j < limit) {
                composite[j.toInt()] = true
                j += i
            }
        }
    }
    return out.toIntArray()
}

/** ⌊√n⌋（牛顿迭代，全程整数运算）。 */
private fun isqrt(n: BigInteger): BigInteger {
    if (n.signum() == 0) return BigInteger.ZERO
    var x = BigInteger.ONE.shiftLeft((n.bitLength() + 1) / 2)
    while (true) {
        val y = x.add(n.divide(x)).shiftRight(1)
        if (y >= x) return x
        x = y
    }
}

/** 枚举 primes[from, to) 的全部 2^(to-from) 个子集乘积（含空集 1），按「最低位」递推。 */
private fun subsetProducts(primes: IntArray, from: Int, to: Int): Array<BigInteger> {
    val n = 1 shl (to - from)
    val arr = arrayOfNulls<BigInteger>(n)
    arr[0] = BigInteger.ONE
    for (i in 1 until n) {
        val low = i and (i - 1)                       // 去掉最低位为 1 的那一位
        val bit = Integer.numberOfTrailingZeros(i)    // 新加入的素数下标
        arr[i] = arr[low]!!.multiply(BigInteger.valueOf(primes[from + bit].toLong()))
    }
    @Suppress("UNCHECKED_CAST")
    return arr as Array<BigInteger>
}

private fun productOf(primes: IntArray): BigInteger {
    var p = BigInteger.ONE
    for (q in primes) p = p.multiply(BigInteger.valueOf(q.toLong()))
    return p
}

/**
 * 方法 A（主路径）：折半 [0, split) / [split, n) + 全 BigInteger 精确排序 + 单调双指针。
 * 返回不超过 [s] 的最大子集乘积。
 */
private fun solveTwoPointer(primes: IntArray, split: Int, s: BigInteger): BigInteger {
    val a = subsetProducts(primes, 0, split)
    val b = subsetProducts(primes, split, primes.size).filter { it <= s }.toTypedArray()
    Arrays.sort(a)
    Arrays.sort(b)
    var j = b.size - 1
    var best = BigInteger.ONE
    for (x in a) {
        while (x.multiply(b[j]) > s) j--            // b = 1 必然可行，j 不会越界
        val v = x.multiply(b[j])
        if (v > best) best = v
    }
    return best
}

/**
 * 方法 B（独立复核）：换一个折半划分，并把对数只当「路标」：对每个 b ∈ B′ 先用 double 对数
 * 二分定位候选下标，再在窗口内做精确比较并沿两侧扩张（找到 a[i]·b ≤ S < a[i+1]·b 的紧夹位置）。
 * 正确性只依赖 A′ 的精确有序性，不依赖浮点精度。
 */
private fun solveLogGuided(primes: IntArray, split: Int, s: BigInteger, logS: Double): BigInteger {
    val a = subsetProducts(primes, 0, split)
    val b = subsetProducts(primes, split, primes.size).filter { it <= s }.toTypedArray()
    Arrays.sort(a)
    Arrays.sort(b)
    // 值都 ≲ 10^50，doubleValue() 不溢出；Math.log 的相对误差 ~1e-16，足够做路标。
    val logA = DoubleArray(a.size) { Math.log(a[it].toDouble()) }
    var best = BigInteger.ONE
    for (y in b) {
        val t = logS - Math.log(y.toDouble())
        // 找到最后一个 logA[i] ≤ t 的下标（近似）
        var lo = 0
        var hi = logA.size - 1
        while (lo < hi) {
            val mid = (lo + hi + 1) ushr 1
            if (logA[mid] <= t) lo = mid else hi = mid - 1
        }
        var i = lo
        // 精确修正：先把 i 夹到 a[i]·y ≤ S < a[i+1]·y
        while (i + 1 < a.size && a[i + 1].multiply(y) <= s) i++
        while (i > 0 && a[i].multiply(y) > s) i--
        check(a[i].multiply(y) <= s) { "方法 B 内部错误：找不到可行 a" }
        val v = a[i].multiply(y)
        if (v > best) best = v
    }
    return best
}

/** 权威对照（缩小规模）：枚举全部子集乘积，挑不超过 √p 的最大者。 */
private fun bruteSubsets(primes: IntArray): BigInteger {
    val s = isqrt(productOf(primes))
    var best = BigInteger.ONE
    for (v in subsetProducts(primes, 0, primes.size)) if (v <= s && v > best) best = v
    return best
}

/** 题面锚点：按定义从 ⌊√n⌋ 向下试除。 */
private fun psrByScan(n: Long): Long {
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    while (n % r != 0L) r--
    return r
}

/** JIT 预热后 3 轮取最优（每轮都核对结果一致，防漂移）。 */
private fun bestOf3(tag: String, expected: BigInteger, f: () -> BigInteger): Double {
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

private fun logOfBig(v: BigInteger): Double = Math.log(v.toDouble())

fun main() {
    val primes = primesBelow(LIMIT)
    println("小于 $LIMIT 的素数共 ${primes.size} 个：${primes.joinToString(",")}")

    // ---------- 1. 题面锚点 ----------
    check(psrByScan(12) == 3L) { "PSR(12) 应为 3" }
    check(psrByScan(3102) == 47L) { "PSR(3102) 应为 47" }
    println("题面锚点：PSR(12) = 3，PSR(3102) = 47（按定义试除复现）")

    // ---------- 2. 缩小规模三方对照 ----------
    val scales = listOf(5 to "2,3,5,7,11", 6 to "2…13", 10 to "<30", 15 to "<50", 20 to "<71", 22 to "<80")
    for ((k, name) in scales) {
        val ps = primes.copyOfRange(0, k)
        val s = isqrt(productOf(ps))
        val brute = bruteSubsets(ps)
        val mA = solveTwoPointer(ps, k / 2, s)
        val mB = solveLogGuided(ps, k / 2 + if (k % 2 == 0) 1 else 0, s, logOfBig(s))
        check(brute == mA && mA == mB) { "$name：暴力 $brute，方法 A $mA，方法 B $mB 不一致" }
        println("$name：素数 $k 个，√p ≈ ${"%.3e".format(Math.sqrt(productOf(ps).toDouble()))}，三方一致 → $brute")
    }

    // ---------- 3. 完整规模 ----------
    val p = productOf(primes)
    val s = isqrt(p)
    check(s.multiply(s) <= p && s.add(BigInteger.ONE).multiply(s.add(BigInteger.ONE)) > p) { "isqrt 自检失败" }

    val t0 = System.nanoTime()
    val bestA = solveTwoPointer(primes, 21, s)
    val msA1 = (System.nanoTime() - t0) / 1e6
    println("方法 A（21/21 精确双指针）：第一次 ${"%.1f".format(msA1)} ms（含枚举/排序/扫描）")

    val bestB = solveLogGuided(primes, 22, s, logOfBig(s))
    check(bestA == bestB) { "两种方法不一致：A=$bestA B=$bestB" }
    println("方法 B（22/20 折半 + 对数引导）一致：$bestA")

    // 三重自检：d 整除 p、d ≤ S、p/d > S
    check(p.mod(bestA).signum() == 0) { "PSR(p) 不整除 p" }
    check(bestA <= s) { "PSR(p) > ⌊√p⌋" }
    check(p.divide(bestA) > s) { "存在更大的因数 ≤ ⌊√p⌋，结果偏小" }
    val answer = bestA.mod(TEN16).toLong()
    println("PSR(p) = $bestA")
    println("是 p 的因数：${p.mod(bestA).signum() == 0}；≤ ⌊√p⌋：${bestA <= s}；p/PSR(p) > ⌊√p⌋：${p.divide(bestA) > s}")

    // ---------- 4. 计时 ----------
    // 先各跑一遍作为预热，再 3 轮取最优
    solveTwoPointer(primes, 21, s)
    solveLogGuided(primes, 22, s, logOfBig(s))
    val msA = bestOf3("方法 A（全规模 42 素数，21/21）", bestA) { solveTwoPointer(primes, 21, s) }
    val msB = bestOf3("方法 B（全规模 42 素数，22/20）", bestB) { solveLogGuided(primes, 22, s, logOfBig(s)) }
    val ps22 = primes.copyOfRange(0, 22)
    bruteSubsets(ps22)
    val msBrute = bestOf3("暴力全子集（<80 的 22 个素数，2^22）", bruteSubsets(ps22)) { bruteSubsets(ps22) }

    println()
    println("答案 = $answer")
    println("汇总：方法 A ${"%.1f".format(msA)} ms；方法 B ${"%.1f".format(msB)} ms；" +
        "暴力（22 素数，2^22 子集）${"%.1f".format(msBrute)} ms")
    println("check() 全部通过")
}
