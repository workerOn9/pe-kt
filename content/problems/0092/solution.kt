/**
 * Project Euler 092 — Square Digit Chains
 *
 * 优化解：链的下一项只依赖「各位数字平方和」，而七位数的平方和最多为 7·9² = 567，
 * 因此先用记忆化把 1..567 的归宿（1 或 89）全部判定好，再用数位 DP 统计
 * 每个平方和对应 [1, 10^7) 内多少个数（允许前导零的 7 位数枚举），按归宿加权求和，
 * 完全不必逐个枚举 10^7 个数或逐个走链。
 *
 * 复杂度：时间 O(7·567·10) 建表 + O(567) 汇总，空间 O(567)
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun digitSquareSum(n: Int): Int {
    var x = n
    var s = 0
    while (x > 0) {
        val d = x % 10
        s += d * d
        x /= 10
    }
    return s
}

fun solve(): Long {
    val digits = 7
    val maxSum = digits * 81

    // dest[s] = 归宿：1 表示落到 1，2 表示落到 89
    val dest = IntArray(maxSum + 1)
    for (s in 1..maxSum) {
        var x = s
        while (x != 1 && x != 89) x = digitSquareSum(x)
        dest[s] = x
    }

    // dp[s] = 用 digits 个数字（允许前导零）拼成、各位平方和恰为 s 的串有多少个
    var dp = LongArray(maxSum + 1)
    dp[0] = 1L
    repeat(digits) {
        val next = LongArray(maxSum + 1)
        for (s in 0..maxSum) {
            val c = dp[s]
            if (c == 0L) continue
            for (d in 0..9) next[s + d * d] += c
        }
        dp = next
    }

    var count = 0L
    for (s in 1..maxSum) if (dest[s] == 89) count += dp[s]
    return count
}

fun main() {
    println(solve())
}
