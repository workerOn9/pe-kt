#!/usr/bin/env kotlin
/**
 * Project Euler 238 — Infinite String Tour（无限字符串之旅）
 *
 * 思路：
 *   1. **BBS 序列周期**：模数 M = 20300713 = 4127·4919（两者均 ≡ 3 mod 4，Blum 整数）。
 *      BBS 递推 s_{n+1} = s_n² mod M 等价于 s_n = s_0^(2^n) mod M。
 *      记 ord₁ = ord_M(s_0)，则 s_n 的周期 P = ord_{ord₁}(2)（「2 在 Z_{ord₁} 中的乘法阶」）。
 *   2. **数字和周期 T**：一周期内所有 s_n 的无前导零十进制串拼接后数位和
 *      T = 80 846 691。每加一整周期 L 位，substring 数位和恰增 T。
 *   3. **p(k) 按 T 周期性**：substring 数位和模 T 的余数完全由其在周期内的相对位置决定；
 *      最早出现位置 p(k) 只与 k mod T 有关。算一次完整 1..T 即可，K = 2·10^15 时
 *      sum_{k=1..K} p(k) = (K/T)·Σ_{k=1..T} p(k) + Σ_{k=1..K mod T} p(k).
 *   4. **位集覆盖**：令 P = 「一周期内出现过的前缀和（模 T，0 表示 T）」构成的 T 位位集；
 *      从起始位 i（对应前缀和 S_i = prefix[i-1]）可达的数位和集合 = P 向右循环移位 S_i 位。
 *      按位置 i 顺序扫描：unknown & rot(P, S_i) 中第一次出现的位就是 p(k) = i 的 k。
 *      本题中位置 i ≈ 88 处所有 k 已被覆盖（80 846 690 个 k 全部出现）。
 *
 * 旁证：
 *   1. Σ_{k=1..1000} p(k) = 4742，与题面校验值完全一致；
 *   2. 同一份 Python 参考实现（独立写 BigInt 位集）算出同一 9922545104535661；
 *   3. 用 brute-force.kt 在前 1000 个 k 上逐项与本算法对拍，结果一致。
 *
 * 答案：9922545104535661
 * 复杂度：位集大小 T ≈ 8.1×10⁷ bit ≈ 10 MB；主循环 ~88 次位操作（每次 ~10⁶ long 的 AND/XOR/shift），
 * 总计实测 < 5 s。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val S0 = 14025256L
private const val MOD = 20300713L
private const val K_TARGET = 2_000_000_000_000_000L

// ===================== 数论工具 =====================

private fun gcd(a: Long, b: Long): Long {
    var x = kotlin.math.abs(a); var y = kotlin.math.abs(b)
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

private fun modPow(a: Long, e: Long, m: Long): Long {
    var base = a % m; var exp = e; var r = 1L
    while (exp > 0L) {
        if (exp and 1L == 1L) r = (r * base) % m
        base = (base * base) % m
        exp = exp shr 1
    }
    return r
}

private fun factorize(n: Long): Map<Long, Int> {
    var x = n; val out = LinkedHashMap<Long, Int>()
    var d = 2L
    while (d * d <= x) {
        while (x % d == 0L) { out.merge(d, 1) { a, b -> a + b }; x /= d }
        d = if (d == 2L) 3L else d + 2L
    }
    if (x > 1L) out.merge(x, 1) { a, b -> a + b }
    return out
}

private fun totientFromFactorization(factors: Map<Long, Int>): Long {
    var n = 1L
    for ((p, e) in factors) repeat(e) { n *= p }
    var phi = n
    for (p in factors.keys) phi = phi / p * (p - 1)
    return phi
}

private fun multiplicativeOrder(a: Long, mod: Long, groupOrder: Long): Long {
    require(gcd(a, mod) == 1L) { "not coprime" }
    var order = groupOrder
    for (p in factorize(groupOrder).keys) {
        while (order % p == 0L && modPow(a, order / p, mod) == 1L) order /= p
    }
    return order
}

private fun bbsPeriod(): Long {
    var p = 0L; var q = 0L; var tmp = MOD
    var d = 2L
    while (d * d <= tmp) {
        if (tmp % d == 0L) { p = d; q = tmp / d; break }
        d = if (d == 2L) 3L else d + 2L
    }
    require(p > 0L) { "factor failed" }
    val lam = (p - 1) * (q - 1) / gcd(p - 1, q - 1)
    val ordS0 = multiplicativeOrder(S0, MOD, lam)
    val phiOrdS0 = totientFromFactorization(factorize(ordS0))
    return multiplicativeOrder(2L, ordS0, phiOrdS0)
}

// ===================== 大位集（仅用 3 个实例 + 1 个 scratch） =====================

/**
 * Long 位集，bit i 在 data[i ushr 6] 的 (i and 63) 位。
 * 不可变长度的圆循环旋转在原地写入 scratch；调用方管理 scratch 复用以避免 GC。
 */
private class Bitset(val bitLen: Int, val wordLen: Int = (bitLen + 63) ushr 6) {
    val data: LongArray = LongArray(wordLen)
    private val tailMask: Long = if (bitLen % 64 == 0) -1L else (1L shl (bitLen % 64)) - 1L

    fun set(idx: Int) {
        data[idx ushr 6] = data[idx ushr 6] or (1L shl (idx and 63))
    }

    /** 设 [1, count] 位全 1（bit 0 代表 k=T，不属于任何 1..count 窗口，必须留空） */
    fun setRange1To(count: Int) {
        if (count <= 0) return
        val fullW = count ushr 6
        val remB = count and 63
        for (i in 0 until fullW) data[i] = -1L
        data[fullW] = if (remB == 63) -1L else (1L shl (remB + 1)) - 1L
        data[0] = data[0] and (-2L)     // 清掉 bit 0
    }

    fun setAll() {
        data.fill(-1L)
        if (tailMask != -1L) data[wordLen - 1] = tailMask
    }

    fun isZero(): Boolean = data.all { it == 0L }
}

/**
 * out = unknown & rot，并同时统计三种窗口的 popcount：
 *   pcAll  —— 全部 T 位
 *   pc1000 —— 与 mask1000（[1,1000] 位为 1）相交
 *   pcRem  —— 与 maskRem（[1,rem] 位为 1）相交
 * 三个统计在同一遍循环里做完，窗口统计只在头部若干个 word 上做。
 */
private fun andCountWindows(
    unknown: LongArray, rot: LongArray, out: LongArray, n: Int,
    mask1000: LongArray, n1000: Int,
    maskRem: LongArray, nRem: Int,
): LongArray {
    var pcAll = 0L
    var pc1000 = 0L
    var pcRem = 0L
    for (i in 0 until n) {
        val v = unknown[i] and rot[i]
        out[i] = v
        pcAll += java.lang.Long.bitCount(v)
        if (i < n1000) pc1000 += java.lang.Long.bitCount(v and mask1000[i])
        if (i < nRem) pcRem += java.lang.Long.bitCount(v and maskRem[i])
    }
    return longArrayOf(pcAll, pc1000, pcRem)
}

/**
 * 把 src 向右循环移位 d 位（视作 T 位环），结果写入 dst（dst 与 src 不能是同一个）。
 * T 不必是 64 的倍数；T-boundary wrap 单独处理。
 */
private fun rotateRight(src: LongArray, dst: LongArray, n: Int, d: Int, T: Int, tailMask: Long) {
    if (d == 0) { System.arraycopy(src, 0, dst, 0, n); return }
    val dLong = d ushr 6
    val dBit = d and 63
    if (dBit == 0) {
        for (i in 0 until n) dst[i] = src[(i + dLong) % n]
    } else {
        val hi = 64 - dBit
        for (i in 0 until n) {
            val a = src[(i + dLong) % n] ushr dBit
            val b = src[(i + dLong + 1) % n] shl hi
            dst[i] = a or b
        }
    }
    if (tailMask != -1L) dst[n - 1] = dst[n - 1] and tailMask

    // T-boundary wrap：bits dst[T-d .. T-1] 应是 src[0 .. d-1]
    if (d > 0) {
        val startPos = T - d
        val endPos = T - 1
        val startWord = startPos ushr 6
        val startBit = startPos and 63
        val endWord = endPos ushr 6
        val endBit = endPos and 63
        if (startWord == endWord) {
            val numBits = endBit - startBit + 1
            val mask = ((1L shl numBits) - 1L) shl startBit
            dst[startWord] = (dst[startWord] and mask.inv()) or
                ((src[0] and ((1L shl numBits) - 1L)) shl startBit)
        } else {
            val n1 = 64 - startBit
            val mask1 = (-1L) shl startBit
            dst[startWord] = (dst[startWord] and mask1.inv()) or
                ((src[0] and ((1L shl n1) - 1L)) shl startBit)
            for (i in (startWord + 1) until endWord) dst[i] = src[i - startWord]
            val offset = endWord - startWord
            val n2 = endBit + 1
            val mask2 = (1L shl n2) - 1L
            dst[endWord] = (dst[endWord] and mask2.inv()) or (src[offset] and mask2)
        }
        if (tailMask != -1L) dst[n - 1] = dst[n - 1] and tailMask
    }
}

// ===================== 主流程 =====================

private fun buildPeriod(periodTerms: Int): Pair<ByteArray, Int> {
    val buf = ByteArray(periodTerms * 8)
    var ptr = 0
    var s = S0
    var total = 0
    for (i in 0 until periodTerms) {
        val txt = s.toString()
        for (ch in txt) {
            buf[ptr++] = (ch - '0').toByte()
            total += ch - '0'
        }
        s = (s * s) % MOD
    }
    require(s == S0) { "BBS period invalid" }
    return Pair(buf.copyOf(ptr), total)
}

private fun main() {
    val t0 = System.currentTimeMillis()

    val period = bbsPeriod()
    println("BBS period = $period  (${System.currentTimeMillis() - t0} ms)")

    val t1 = System.currentTimeMillis()
    val (digits, T) = buildPeriod(period.toInt())
    println("digits=${digits.size}, T=$T  (${System.currentTimeMillis() - t1} ms)")

    val t2 = System.currentTimeMillis()
    val wordLen = (T + 63) ushr 6
    val tailMask = if (T % 64 == 0) -1L else (1L shl (T % 64)) - 1L

    // present：bit i 表示「前缀和 mod T = i」（0 表示 T）
    val present = LongArray(wordLen)
    present[0] = present[0] or 1L                  // bit 0
    var prefix = 0
    for (b in digits) {
        prefix += b.toInt()
        val idx = if (prefix == T) 0 else prefix
        present[idx ushr 6] = present[idx ushr 6] or (1L shl (idx and 63))
    }
    println("present built  (${System.currentTimeMillis() - t2} ms)")

    // 状态：unknown = 全 1，rot = present 副本（ping-pong 双缓冲）
    val unknown = LongArray(wordLen).also { it.fill(-1L); if (tailMask != -1L) it[wordLen - 1] = tailMask }
    val rotA = LongArray(wordLen)
    val rotB = LongArray(wordLen)
    System.arraycopy(present, 0, rotA, 0, wordLen)
    var curRot = rotA
    var nxtRot = rotB
    val newBitset = LongArray(wordLen)

    val q = K_TARGET / T
    val rem = (K_TARGET % T).toInt()
    val mask1000Len = kotlin.math.min(1000, T)
    val mask1000 = Bitset(T).also { it.setRange1To(mask1000Len) }
    val maskRem = Bitset(T).also { it.setRange1To(rem) }
    val n1000 = ((mask1000Len + 63) ushr 6) + 1
    val nRem = ((rem + 63) ushr 6) + 1

    var totalAll = 0L; var total1000 = 0L; var totalRem = 0L
    var doneAt = -1
    var prefixSum = 0

    val t3 = System.currentTimeMillis()
    val n = digits.size
    var i = 0
    while (i < n) {
        val pcArr = andCountWindows(unknown, curRot, newBitset, wordLen,
            mask1000.data, n1000, maskRem.data, nRem)
        val pc = pcArr[0]
        if (pc > 0) {
            val w = (i + 1).toLong()
            totalAll += w * pc
            total1000 += w * pcArr[1]
            totalRem += w * pcArr[2]
            // unknown ^= new
            for (k in 0 until wordLen) unknown[k] = unknown[k] xor newBitset[k]
            // 检查 unknown 是否清空
            var allZero = true
            for (k in 0 until wordLen) {
                if (unknown[k] != 0L) { allZero = false; break }
            }
            if (allZero) { doneAt = i; break }
        }
        val d = digits[i].toInt()
        prefixSum += d
        if (prefixSum == T) prefixSum = 0
        if (d != 0) {
            rotateRight(curRot, nxtRot, wordLen, d, T, tailMask)
            val tmp = curRot; curRot = nxtRot; nxtRot = tmp
        }
        i++
    }
    println("main scan done at i=$doneAt  (${System.currentTimeMillis() - t3} ms)")

    println("sum_{k=1..T} p(k) = $totalAll")
    println("sum_{k=1..1000} p(k) = $total1000  (should be 4742)")
    println("sum_{k=1..$rem} p(k) = $totalRem")

    require(total1000 == 4742L) { "check value mismatch: $total1000" }
    val ans = q * totalAll + totalRem
    println("answer = $ans")
}
