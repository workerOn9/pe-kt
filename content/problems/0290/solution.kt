#!/usr/bin/env kotlin
/**
 * Project Euler 290 — Digital Signature（数字签名）
 *
 * 题目：统计 0 ≤ n < 10^18 中满足 digitSum(n) = digitSum(137n) 的整数 n 的个数。
 *
 * 建模
 * ────
 * 把 n 写成 18 位十进制（允许前导零）：n = Σ_{i=0}^{17} d_i·10^i。逐位做乘法 137·n：
 * 在第 i 位上
 *     v_i = 137·d_i + c_i,   p_i = v_i mod 10,   c_{i+1} = v_i div 10,
 * c_i 是「从低位带进第 i 位的进位」。137n 的第 i 位就是 p_i，而 c_0 = 0。因为
 * 137·(10^18−1) < 10^21，137n 只有 21 位：n 的 18 位处理完后，再补 3 个「排空位」（d_i = 0，
 * i = 18,19,20）把进位倒空（c_18 ≤ 137 ⇒ 三位必然倒空，且排完 c_21 = 0）。
 * 判定条件就是
 *     Σ_{i=0}^{17} p_i + Σ_{i=18}^{20} p_i = Σ_{i=0}^{17} d_i
 * ⟺ δ := Σ_i (p_i − d_i) = 0（i 跑遍 21 位）。
 * 进位在稳态下满足 c ≤ 137（137·9 + 137 = 1370 → 1370 div 10 = 137），所以状态空间极小。
 *
 * 主路径 A：逐位 DP（自低位向高位）
 * ─────────────────────────────
 * 状态 (c, δ) =（进位，数字和之差的部分和），转移就是把上面三个公式照着走一遍：
 *     dp[i+1][v div 10][δ + (v mod 10) − d] += dp[i][c][δ],   v = 137·d + c.
 * 位宽：21 位 × 进位 138 种 × δ ∈ [−189, 189]，共约 1.2×10^7 次状态转移，毫秒级。
 *
 * 主路径 B：折半 + 进位匹配（复核）
 * ────────────────────────────
 * 取 n = n_lo + 10^6·n_hi（n_lo < 10^6）：低半 10^6 个值**显式枚举**成直方图
 *     low[c][δ_lo]：c = 137·n_lo div 10^6 是进入第 6 位的进位；
 * 高半 12 位用**反向** DP（从第 17 位向第 6 位倒着推）：
 * 乘法在反向也是确定的——给定「第 i+1 位的进位 c_out、137n 的第 i 位 p、n 的第 i 位 d」，
 *     c_in = 10·c_out + p − 137·d     （即 v = 137d + c_in = 10·c_out + p 的唯一解）
 * 必须落在 [0, 137] 内。反向 DP 的初值就是「排空段」的贡献：进位 c 排空后贡献 digitSum(c)
 * 位数字，故初值 (c, δ) = (c, digitSum(c))，每个 c 一个。于是在第 6 位处拿到
 *     high[c][δ_hi]（c = 同一个进位，δ_hi 含排空段）。
 * 合并：count = Σ_c Σ_δ low[c][δ]·high[c][−δ]。
 * 这条路径与 A 的切分方式（进位匹配）、推进方向（反向）、低半实现（显式枚举）都不同。
 *
 * 验证
 * ────
 * 1. 小规模穷举对拍：n < 10^k（k = 1..6）逐值朴素枚举（另写 digitSum 与乘法）与主路径 A 的
 *    k 位版本逐 k 相等；穷举本身还给出 k = 1 时只有 {0, 9} 两个（digitSum(137·9)=9）；
 * 2. 两条路径在全尺寸 10^18 上给出同一个数；
 * 3. brute-force.kt 用第三套实现（朴素枚举做到 10^8，另一套「双累加器」DP）复核；
 * 4. 公开答案表（Nayuki 的 Project Euler 答案清单）第 290 条为 20444710234716473，
 *    与本机实跑一致（仅作旁证）。
 *
 * 复杂度
 * ──────
 * A：O(位数 × 进位 × δ 宽度 × 10) ≈ 10^7 次整数加法；B：低半 10^6 次枚举 + 反向 DP ≈ 6×10^7 次；
 * 都是毫秒~百毫秒级。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0290/solution.kt -d /tmp/kc-0290
 * java -cp /tmp/kc-0290:<kotlin-stdlib> SolutionKt
 */

private const val MULT = 137L
private const val DIGITS = 18
private const val CARRY_MAX = 137              // 137·9 + 137 = 1370 → 1370 div 10 = 137（稳态上界）
private const val OFF = 200                    // δ 的偏移：21 位最多偏 ±9·21 = ±189

/** 十进制数字和（纯算术，不用字符串）。 */
private fun digitSum(x0: Long): Int {
    var x = x0
    var s = 0
    while (x > 0L) {
        s += (x % 10L).toInt()
        x /= 10L
    }
    return s
}

private fun pow10(k: Int): Long {
    var r = 1L
    repeat(k) { r *= 10L }
    return r
}

/**
 * 主路径 A：逐位 DP。digits = n 的位数（本题 18；对拍时取小值），n 位之后补 3 个排空位。
 * 返回满足 digitSum(n) = digitSum(137n) 的 n < 10^digits 的个数。
 */
private fun countByDigitDP(digits: Int): Long {
    val positions = digits + 3
    val size = 2 * OFF + 1
    var dp = Array(CARRY_MAX + 1) { LongArray(size) }
    dp[0][OFF] = 1L
    for (pos in 0 until positions) {
        val next = Array(CARRY_MAX + 1) { LongArray(size) }
        val maxD = if (pos < digits) 9 else 0
        for (c in 0..CARRY_MAX) {
            val row = dp[c]
            var touched = false
            for (idx in 0 until size) if (row[idx] != 0L) { touched = true; break }
            if (!touched) continue
            for (d in 0..maxD) {
                val v = MULT * d + c
                val p = (v % 10L).toInt()
                val c2 = (v / 10L).toInt()
                val delta = p - d
                val dst = next[c2]
                for (idx in 0 until size) {
                    val cnt = row[idx]
                    if (cnt != 0L) dst[idx + delta] += cnt
                }
            }
        }
        dp = next
    }
    // 排空段结束后进位必为 0（137 → 13 → 1 → 0），断言一下
    for (c in 1..CARRY_MAX) {
        for (idx in 0 until size) check(dp[c][idx] == 0L) { "排空后仍有非零进位 c = $c" }
    }
    return dp[0][OFF]
}

/**
 * 主路径 B：折半 + 进位匹配（仅全尺寸 18 位；低 6 位显式枚举，高 12 位反向 DP）。
 */
private fun countByFoldHalf(): Long {
    val split = 6
    val size = 2 * OFF + 1
    val lowMod = pow10(split)

    // 低半：显式枚举 n_lo < 10^6
    val low = Array(CARRY_MAX + 1) { LongArray(size) }
    for (nLo in 0 until lowMod) {
        val prod = MULT * nLo
        val c = (prod / lowMod).toInt()
        val delta = digitSum(prod % lowMod) - digitSum(nLo)
        check(c <= CARRY_MAX) { "低半进位越界：$c" }
        low[c][OFF + delta]++
    }

    // 高半：反向 DP。初值 = 排空段贡献（进位 c 的十进制各位数字）
    var dp = Array(CARRY_MAX + 1) { LongArray(size) }
    for (c in 0..CARRY_MAX) dp[c][OFF + digitSum(c.toLong())] = 1L
    // 反向转移表：给定 (c_out, d)，列出所有满足 c_in = 10·c_out + p − 137·d ∈ [0,137] 的 (c_in, δ)
    for (pos in DIGITS - 1 downTo split) {
        val next = Array(CARRY_MAX + 1) { LongArray(size) }
        for (cOut in 0..CARRY_MAX) {
            val row = dp[cOut]
            var touched = false
            for (idx in 0 until size) if (row[idx] != 0L) { touched = true; break }
            if (!touched) continue
            for (d in 0..9) {
                for (p in 0..9) {
                    val cIn = 10 * cOut + p - 137 * d
                    if (cIn < 0 || cIn > CARRY_MAX) continue
                    val delta = p - d
                    val dst = next[cIn]
                    for (idx in 0 until size) {
                        val cnt = row[idx]
                        if (cnt != 0L) dst[idx + delta] += cnt
                    }
                }
            }
        }
        dp = next
    }

    // 合并：δ_lo + δ_hi = 0
    var total = 0L
    for (c in 0..CARRY_MAX) {
        val l = low[c]
        val h = dp[c]
        for (i in 0 until size) {
            val a = l[i]
            if (a == 0L) continue
            val b = h[size - 1 - i]
            if (b != 0L) total += a * b
        }
    }
    return total
}

/** 小规模朴素穷举：n < 10^k 逐个判定（独立实现，用于对拍）。 */
private fun bruteCount(k: Int): Long {
    val limit = pow10(k)
    var count = 0L
    var n = 0L
    while (n < limit) {
        if (digitSum(n) == digitSum(MULT * n)) count++
        n++
    }
    return count
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    println("== 1. 小规模穷举对拍（n < 10^k，逐值朴素枚举 vs 主路径 A）==")
    for (k in 1..6) {
        val brute = bruteCount(k)
        val dp = countByDigitDP(k)
        check(brute == dp) { "k = $k：穷举 $brute ≠ DP $dp" }
        println("k = $k：穷举 = DP = $brute")
    }
    println("（k = 1 的答案是 {0, 9}：digitSum(137·9) = digitSum(1233) = 9 ✓）")

    println()
    println("== 2. 全尺寸：两条独立路径 ==")
    val ansA = countByDigitDP(DIGITS)
    val ansB = countByFoldHalf()
    check(ansA == ansB) { "两条路径不一致：A = $ansA，B = $ansB" }
    println("路径 A（逐位 DP，21 位 × 138 进位 × 401 差值）：$ansA")
    println("路径 B（低 6 位显式枚举 + 高 12 位反向 DP + 进位匹配）：$ansB")
    println("一致性 ✓（约每 ${
        String.format("%.2f", 1e18 / ansA)
    } 个整数里有 1 个满足条件）")

    println()
    println("== 3. 计时 ==")
    val msA = bestOf3("主路径 A（逐位 DP）", ansA) { countByDigitDP(DIGITS) }
    val msB = bestOf3("路径 B（折半 + 进位匹配）", ansB) { countByFoldHalf() }
    val msBrute = bestOf3("朴素穷举 10^6（对拍用）", bruteCount(6)) { bruteCount(6) }

    println()
    println("== 4. 结果 ==")
    println("0 ≤ n < 10^18 中 digitSum(n) = digitSum(137n) 的个数 = $ansA")
    println("check() 全部通过；A ${"%.3f".format(msA)} ms，B ${"%.3f".format(msB)} ms，暴力 10^6 ${"%.3f".format(msBrute)} ms")
}
