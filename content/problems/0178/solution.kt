/**
 * PE 178 — 小于 10^40 的全数字步进数个数。
 *
 * 原理：数位 DP。步进数相邻数位差为 ±1，因此出现过的数位区间天然连续，
 * 只需记录 [low, high]（0 <= low <= high <= 9）与当前末尾数字 last。
 * 状态数：40 步 × 10 (last) × 55 (low, high 对) = 22000 个状态，
 * 状态转移：last 转移到 last-1 (>=0) 与 last+1 (<=9)，更新 low=min(low, d), high=max(high, d)。
 * 初始：长度为 1 时首位为 1..9（无前导零），low=high=d。
 * 累加每个长度（10..40）转移后 low=0 且 high=9 的状态总数。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
fun solve178(): Long {
    // dp[last][low][high]
    var dp = Array(10) { Array(10) { LongArray(10) } }
    for (d in 1..9) {
        dp[d][d][d] = 1L
    }

    var total = 0L
    for (len in 2..40) {
        val next = Array(10) { Array(10) { LongArray(10) } }
        for (last in 0..9) {
            for (low in 0..last) {
                for (high in last..9) {
                    val count = dp[last][low][high]
                    if (count == 0L) continue
                    if (last > 0) {
                        val d = last - 1
                        val nl = if (d < low) d else low
                        next[d][nl][high] += count
                    }
                    if (last < 9) {
                        val d = last + 1
                        val nh = if (d > high) d else high
                        next[d][low][nh] += count
                    }
                }
            }
        }
        dp = next
        for (last in 0..9) {
            total += dp[last][0][9]
        }
    }
    return total
}

fun main() {
    val answer = solve178()
    println(answer)
}
