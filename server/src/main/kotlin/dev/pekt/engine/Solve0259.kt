package dev.pekt.engine

import dev.pekt.math.gcd

/**
 * PE 259 — Reachable Numbers（可达数）。
 *
 * 把 "123456789" 切成连续块，再在块上做四则运算组成表达式树。任一表达式的「最后一次运算」
 * 把数字串切成左右两段，于是可以按区间 DP：V(i,j) = 数字段 [i,j) 能算出的全部精确有理数，
 *
 *     V(i,j) = { concat(i,j) } ∪ ∪_{i<k<j} { a op b : a ∈ V(i,k), b ∈ V(k,j), b ≠ 0 }，
 *
 * 答案 = V(0,9) 中所有正整数（分母 1、分子 > 0）之和 = 20101196798。
 *
 * 既约分数 (p, q)（q > 0）位打包成 (p << 32) | q 存入开放寻址 Long 集；实测 max |p| =
 * 123456789、max q = 23456789（< 2^31），且任意中间乘积 ≤ 2B² ≈ 3.05×10^16 < Long.MAX_VALUE。
 * 逻辑与 content/problems/0259/solution.kt 的方法 A 一致（gcd 复用 dev.pekt.math）。
 * 本机耗时约 0.7 秒。
 */
internal fun solve0259Impl(): Long {
    val digits = "123456789"
    val n = digits.length
    @Suppress("UNCHECKED_CAST")
    val dp = Array(n) { arrayOfNulls<Pe259LongSet>(n + 1) }

    for (i in 0 until n) {
        val set = Pe259LongSet(4)
        pe259AddReduced(set, (digits[i] - '0').toLong(), 1L)
        dp[i][i + 1] = set
    }
    for (len in 2..n) {
        for (i in 0..n - len) {
            val j = i + len
            val set = Pe259LongSet(64)
            var chunk = 0L
            for (t in i until j) chunk = chunk * 10 + (digits[t] - '0')
            pe259AddReduced(set, chunk, 1L)                        // 整段拼接
            for (m in i + 1 until j) {
                val left = dp[i][m]!!
                val right = dp[m][j]!!
                for (lk in left.keys) {
                    if (lk == 0L) continue
                    val ln = lk shr 32
                    val ld = lk and 0xFFFFFFFFL
                    for (rk in right.keys) {
                        if (rk == 0L) continue
                        val rn = rk shr 32
                        val rd = rk and 0xFFFFFFFFL
                        pe259AddReduced(set, ln * rd + rn * ld, ld * rd)
                        pe259AddReduced(set, ln * rd - rn * ld, ld * rd)
                        pe259AddReduced(set, ln * rn, ld * rd)
                        if (rn != 0L) pe259AddReduced(set, ln * rd, ld * rn)
                    }
                }
            }
            dp[i][j] = set
        }
    }

    var sum = 0L
    for (k in dp[0][n]!!.keys) {
        if (k == 0L) continue
        if ((k and 0xFFFFFFFFL) == 1L) {
            val p = k shr 32
            if (p > 0L) sum += p
        }
    }
    return sum
}

/** 既约、分母为正后位打包插入；0 是空槽哨兵（合法分母恒 ≥ 1）。 */
private fun pe259AddReduced(set: Pe259LongSet, num0: Long, den0: Long) {
    var p = num0
    var q = den0
    if (q < 0L) { p = -p; q = -q }
    if (p == 0L) {
        q = 1L
    } else {
        val g = gcd(p, q)
        p /= g
        q /= g
    }
    set.add((p shl 32) xor q)
}

/** 开放寻址哈希集（线性探测，装填因子 1/2），键为非零 Long。 */
private class Pe259LongSet(capHint: Int) {
    var keys = run {
        var c = 16
        while (c < capHint * 2) c = c shl 1
        LongArray(c)
    }
        private set
    private var mask = keys.size - 1
    private var size = 0

    fun add(key: Long): Boolean {
        var i = pe259Mix(key) and mask
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
            var i = pe259Mix(k) and nm
            while (nk[i] != 0L) i = (i + 1) and nm
            nk[i] = k
        }
        keys = nk
        mask = nm
    }
}

private fun pe259Mix(k: Long): Int {
    var h = k
    h = (h xor (h ushr 33)) * 0xFF51AFD7ED558CCDUL.toLong()
    h = (h xor (h ushr 33)) * 0xC4CEB9FE1A85EC53UL.toLong()
    h = h xor (h ushr 33)
    return h.toInt()
}
