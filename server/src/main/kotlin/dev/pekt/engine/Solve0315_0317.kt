package dev.pekt.engine

/**
 * PE 315 — Digital Root Clocks（数根时钟）
 *
 * 两钟切换总次数之差 = 2 * sum popcount(U_i and U_{i+1})。
 * 最终答案：13625242。
 */
internal fun solve0315Impl(): Long {
    val masks = intArrayOf(
        0b0111111, // 0: abcdef
        0b0000110, // 1: bc
        0b1011011, // 2: abdeg
        0b1001111, // 3: abcdg
        0b1100110, // 4: bcfg
        0b1101101, // 5: acdfg
        0b1111101, // 6: acdefg
        0b0100111, // 7: abcf
        0b1111111, // 8: abcdefg
        0b1101111  // 9: abcdfg
    )

    fun drChain(n: Int): List<Int> {
        val res = ArrayList<Int>()
        var cur = n
        res.add(cur)
        while (cur >= 10) {
            var s = 0
            var t = cur
            while (t > 0) { s += t % 10; t /= 10 }
            res.add(s)
            cur = s
        }
        return res
    }

    fun numMasks(n: Int): IntArray {
        var len = 0
        var t = n
        while (t > 0) { len++; t /= 10 }
        val arr = IntArray(len)
        t = n
        for (i in 0 until len) {
            arr[i] = masks[t % 10]
            t /= 10
        }
        return arr
    }

    val limit = 20_000_000
    val sieve = BooleanArray(limit + 1) { it >= 2 }
    var i = 2
    while (i.toLong() * i <= limit) {
        if (sieve[i]) {
            var j = i.toLong() * i
            while (j <= limit) { sieve[j.toInt()] = false; j += i }
        }
        i++
    }

    var total = 0L
    for (p in 10_000_001..limit step 2) {
        if (!sieve[p]) continue
        val chain = drChain(p)
        for (c in 0 until chain.size - 1) {
            val u = numMasks(chain[c])
            val v = numMasks(chain[c + 1])
            val digits = minOf(u.size, v.size)
            var common = 0
            for (d in 0 until digits) {
                common += Integer.bitCount(u[d] and v[d])
            }
            total += 2 * common
        }
    }
    return total
}

/**
 * PE 316 — Numbers in Decimal Expansions（小数展开中的数）
 *
 * g(n) = sum_{k in borders} 10^k - d + 1。
 * 最终答案：542934735751917735。
 */
internal fun solve0316Impl(): Long {
    val pow10 = LongArray(18) { 1L }
    for (idx in 1..17) pow10[idx] = pow10[idx - 1] * 10L

    var total = 0L
    for (n in 2..999_999) {
        val v = 10_000_000_000_000_000L / n
        val s = v.toString()
        val d = s.length
        val pi = IntArray(d)
        var j = 0
        for (idx in 1 until d) {
            while (j > 0 && s[idx] != s[j]) j = pi[j - 1]
            if (s[idx] == s[j]) j++
            pi[idx] = j
        }
        var sumBorders = pow10[d]
        var k = pi[d - 1]
        while (k > 0) {
            sumBorders += pow10[k]
            k = pi[k - 1]
        }
        total += sumBorders - d + 1
    }
    return total
}

/**
 * PE 317 — Firecracker（爆竹）
 *
 * 旋转安全抛物面体积 V = pi * (v0^2 / g) * (h0 + v0^2 / (2g))^2。
 * 四舍五入到 4 位小数 = 1856532.8455，* 10^4 = 18565328455。
 */
internal fun solve0317Impl(): String {
    val h0 = 100.0
    val v0 = 20.0
    val g = 9.81
    val H = h0 + (v0 * v0) / (2.0 * g)
    val V = Math.PI * (v0 * v0 / g) * H * H
    return String.format(java.util.Locale.US, "%.4f", V)
}