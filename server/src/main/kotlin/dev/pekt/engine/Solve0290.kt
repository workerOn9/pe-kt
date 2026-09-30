package dev.pekt.engine

/**
 * PE 290 — Digital Signature（数字签名）：统计 0 ≤ n < 10^18 中 digitSum(n) = digitSum(137n) 的个数。
 *
 * 推导（详见 content/problems/0290/solution.kt 头部与 0290/analysis.md）：
 *   把 n 写成 18 位十进制（允许前导零），乘法 137n 逐位进行：
 *     v_i = 137·d_i + c_i，p_i = v_i mod 10（= 137n 的第 i 位），c_{i+1} = v_i div 10，c_0 = 0。
 *   137·(10^18−1) < 10^21，故 137n 至多 21 位，n 的 18 位后补 3 个排空位（d = 0）把进位倒空。
 *   判定条件等价于 δ = Σ_{i=0}^{20}(p_i − d_i) = 0。进位稳态有界 c ≤ 137
 *   （137·9 + 137 = 1370 → 1370 div 10 = 137），所以状态空间只有 138 × 401 个格子。
 *
 *   逐位 DP：dp[i+1][v div 10][δ + (v mod 10) − d] += dp[i][c][δ]，21 位 × 10 个数字，
 *   约 1.2×10^7 次加法，末态 (0, 0) 的计数即答案；内存两层 LongArray（约 0.9 MB）。
 *
 * 复杂度：O(位数 × 进位 × δ 宽度 × 10) ≈ 1.2×10^7 次整数加法，实测约 7 ms，远低于 10 s 熔断线。
 *   校验：n < 10^k（k = 1..8）的计数在逐值穷举（k ≤ 6 在 solution.kt 内、k = 7,8 在 brute-force.kt
 *   的 10^8 穷举）与两套独立 DP 下全等，取值 2/4/33/306/2902/27955/271523/2636394；
 *   brute-force.kt 的第三条路径（10^8 穷举直方图 × 高 10 位反向 DP + 进位匹配）与独立 Python
 *   实现都给出同一答案 20444710234716473；公开答案表（Nayuki）亦为该值（仅作旁证）。
 */
internal fun solve0290Impl(): Long {
    val digits = 18
    val positions = digits + 3                  // 137n 比 n 多至多 3 位
    val carryMax = 137                          // 稳态进位上界
    val off = 200                               // δ 偏移：|δ| ≤ 9·21 = 189 < 200
    val size = 2 * off + 1

    var dp = Array(carryMax + 1) { LongArray(size) }
    dp[0][off] = 1L
    for (pos in 0 until positions) {
        val next = Array(carryMax + 1) { LongArray(size) }
        val maxD = if (pos < digits) 9 else 0    // n 只有 18 位，其余是进位排空位
        for (c in 0..carryMax) {
            val row = dp[c]
            var touched = false
            for (idx in 0 until size) if (row[idx] != 0L) { touched = true; break }
            if (!touched) continue
            for (d in 0..maxD) {
                val v = 137L * d + c
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
    return dp[0][off]
}
