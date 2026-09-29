package dev.pekt.engine

import dev.pekt.math.primesUpTo
import java.math.BigInteger

/**
 * PE 266 — Pseudo Square Root（伪平方根）：p = 所有小于 190 的素数之积（42 个素数，p ≈ 10^72.6），
 * 求 PSR(p) = 不超过 √p 的最大因数，结果对 10^16 取模。
 *
 * 推导（详见 content/problems/0266/solution.kt 头部与 0266/analysis.md）：
 *   p 无平方因子 ⇒ p 的因数就是若干素数的乘积，PSR(p) = 「不超过 ⌊√p⌋ 的最大子集乘积」。
 *   折半枚举：素数分两半 A（前 21 个：2…73）、B（后 21 个：79…181），各枚举 2^21 个子集乘积
 *   （products[i] = products[i and (i−1)] · primes[最低位]）。令 S = ⌊√p⌋（BigInteger 牛顿迭代），
 *   则 a·b ≤ √p ⟺ a·b ≤ S（左边是整数）。把 A、B 按精确值升序排序后做单调双指针：a 升序时
 *   S/a 单调递减，「满足 a·b ≤ S 的最大 b 下标」只减不增——B[j] 即该 a 下的最优 b（b = 1 恒可行）。
 *   对所有 a 的候选取最大即答案，最后 mod 10^16。算法与 content/problems/0266/solution.kt 的
 *   主路径（方法 A）逐字对应。
 *
 * 实现说明（与 solution.kt 的唯一差别是数值表示）：solution.kt 用 BigInteger 数组；这里为了
 *   控制内存（部署容器堆可能较小），把每个子集乘积存成 128-bit 的 (hi, lo) 两个 Long——
 *   所有候选值都 ≤ S < 2^121，A 半乘积 < 2^105，两 limb 足够；枚举时用无符号 64×64 高位乘法
 *   (Math.multiplyHigh) 检测溢出，超过 S 的分支标成哨兵值（排序后落在尾部，不参与扫描）。
 *   排序是自底向上归并（(hi, lo) 字典序，hi 恒为非负、lo 用无符号比较）。双指针对比较时再把
 *   limb 还原成 BigInteger 做一次精确乘法——比较次数只有 O(|A|+|B|)，代价可忽略。
 *
 * 复杂度：时间 O(2^21 log 2^21)（排序）+ O(2^21)（扫描），实测约 0.7 s（远低于 10 s 熔断线）；
 *   内存约 110 MB（两份 2^21×2 个 Long 加归并缓冲），-Xmx128m 下可运行（BigInteger 版需 384 MB）。
 *   校验：题面锚点 PSR(3102) = 47；小素数集合上 limb 版与方法 A/B、全子集暴力三方一致（见
 *   solution.kt 与 brute-force.kt）；全规模输出 1096883702440585，与方法 A（BigInteger 版）一致。
 */
internal fun solve0266Impl(): Long {
    val primes = primesUpTo(189).map { it.toInt() }.toIntArray()  // 42 个素数
    val p = productOf(primes)
    val s = isqrt(p)
    val best = mitmBest(primes, 21, s)
    check(p.mod(best).signum() == 0 && best <= s) { "PE 266 自检失败：结果不是不超过 √p 的因数" }
    return best.mod(BigInteger.valueOf(10_000_000_000_000_000L)).toLong()
}

private fun productOf(primes: IntArray): BigInteger {
    var p = BigInteger.ONE
    for (q in primes) p = p.multiply(BigInteger.valueOf(q.toLong()))
    return p
}

/** ⌊√n⌋（牛顿迭代，整数运算）。 */
private fun isqrt(n: BigInteger): BigInteger {
    if (n.signum() == 0) return BigInteger.ZERO
    var x = BigInteger.ONE.shiftLeft((n.bitLength() + 1) / 2)
    while (true) {
        val y = x.add(n.divide(x)).shiftRight(1)
        if (y >= x) return x
        x = y
    }
}

/** 折半 + 精确升序排序 + 单调双指针：不超过 [s] 的最大子集乘积（[split] 为两半的分界下标）。 */
private fun mitmBest(primes: IntArray, split: Int, s: BigInteger): BigInteger {
    val sHi = s.shiftRight(64).toLong()
    val sLo = s.toLong()
    val a = enumerateProducts(primes, 0, split, sHi, sLo).also { sortProducts(it) }
    val b = enumerateProducts(primes, split, primes.size, sHi, sLo).also { sortProducts(it) }
    var j = b.n - 1
    var bestHi = 0L
    var bestLo = 1L
    for (i in 0 until a.n) {
        val aBig = toBigInteger(a.hi[i], a.lo[i])
        if (aBig > s) continue                       // ∏A 超过 S 时这个 a 无可用 b
        while (j > 0 && aBig.multiply(toBigInteger(b.hi[j], b.lo[j])) > s) j--   // b[0] = 1 可行
        val v = aBig.multiply(toBigInteger(b.hi[j], b.lo[j]))
        val vh = v.shiftRight(64).toLong()
        val vl = v.toLong()
        if (vh > bestHi || (vh == bestHi && java.lang.Long.compareUnsigned(vl, bestLo) > 0)) {
            bestHi = vh
            bestLo = vl
        }
    }
    return toBigInteger(bestHi, bestLo)
}

/** 子集乘积表：hi[i]·2^64 + lo[i]（无符号），[0, n) 为有效项，哨兵项排在末尾。 */
private class Products(val hi: LongArray, val lo: LongArray, val n: Int)

private const val SENTINEL = Long.MAX_VALUE   // 越界（> S 或溢出 128 位）的标记，排序后落到尾部

/**
 * 按「最低位」递推枚举 primes[from, to) 的全部子集乘积（含 1），并用 S = (sHi, sLo) 剪枝：
 * 超过 S 的乘积不会再变小，其延伸分支一并丢弃。所有有效值 < 2^128（hi 非负且 < 2^64）。
 */
private fun enumerateProducts(primes: IntArray, from: Int, to: Int, sHi: Long, sLo: Long): Products {
    val n = 1 shl (to - from)
    val hi = LongArray(n)
    val lo = LongArray(n)
    lo[0] = 1L
    var valid = 1
    for (i in 1 until n) {
        val low = i and (i - 1)
        val h = hi[low]
        val l = lo[low]
        if (h == SENTINEL) {                          // 父乘积已越界 ⇒ 本乘积也越界
            hi[i] = SENTINEL; lo[i] = SENTINEL; continue
        }
        val q = primes[from + Integer.numberOfTrailingZeros(i)].toLong()
        val nl = l * q
        val carry = mulHighUnsigned(l, q)             // l·q 的高 64 位
        if (Math.multiplyHigh(h, q) != 0L) {          // h·q ≥ 2^64 ⇒ 乘积 ≥ 2^128 > S
            hi[i] = SENTINEL; lo[i] = SENTINEL; continue
        }
        val nh = h * q + carry
        if (java.lang.Long.compareUnsigned(nh, carry) < 0 ||   // 加法回绕
            greaterS(nh, nl, sHi, sLo)
        ) {
            hi[i] = SENTINEL; lo[i] = SENTINEL; continue
        }
        hi[i] = nh; lo[i] = nl; valid++
    }
    return Products(hi, lo, valid)
}

/** (h,l) > (sHi,sLo)？按无符号 128 位比较。 */
private fun greaterS(h: Long, l: Long, sHi: Long, sLo: Long): Boolean =
    if (h != sHi) java.lang.Long.compareUnsigned(h, sHi) > 0
    else java.lang.Long.compareUnsigned(l, sLo) > 0

/** 无符号 64×64 乘法的高 64 位。 */
private fun mulHighUnsigned(a: Long, b: Long): Long =
    Math.multiplyHigh(a, b) + (if (a < 0) b else 0L) + (if (b < 0) a else 0L)

/** 把 (hi, lo) 按无符号解释还原成 BigInteger。 */
private fun toBigInteger(h: Long, l: Long): BigInteger {
    val bytes = ByteArray(16)
    var x = h
    for (i in 7 downTo 0) { bytes[i] = x.toByte(); x = x shr 8 }
    x = l
    for (i in 15 downTo 8) { bytes[i] = x.toByte(); x = x shr 8 }
    return BigInteger(1, bytes)
}

/** 自底向上归并排序（稳定）。(hi, lo) 字典序：hi 恒为非负，lo 按无符号比较。 */
private fun sortProducts(p: Products) {
    val n = p.hi.size
    var srcH = p.hi
    var srcL = p.lo
    var dstH = LongArray(n)
    var dstL = LongArray(n)
    var width = 1
    while (width < n) {
        var i = 0
        while (i < n) {
            val mid = minOf(i + width, n)
            val end = minOf(i + 2 * width, n)
            var x = i
            var y = mid
            var w = i
            while (x < mid && y < end) {
                val takeLeft = if (srcH[x] != srcH[y]) srcH[x] < srcH[y]
                else java.lang.Long.compareUnsigned(srcL[x], srcL[y]) <= 0
                if (takeLeft) {
                    dstH[w] = srcH[x]; dstL[w] = srcL[x]; x++
                } else {
                    dstH[w] = srcH[y]; dstL[w] = srcL[y]; y++
                }
                w++
            }
            while (x < mid) { dstH[w] = srcH[x]; dstL[w] = srcL[x]; x++; w++ }
            while (y < end) { dstH[w] = srcH[y]; dstL[w] = srcL[y]; y++; w++ }
            i = end
        }
        var t = srcH; srcH = dstH; dstH = t
        t = srcL; srcL = dstL; dstL = t
        width = width shl 1
    }
    if (srcH !== p.hi) {
        srcH.copyInto(p.hi)
        srcL.copyInto(p.lo)
    }
}
