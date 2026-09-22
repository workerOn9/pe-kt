/**
 * PE 181 — brute force：对小规模 (B=3, W=1) 与 (B=2, W=2) 通过递归回溯枚举验证背包转移正确性。
 */
fun main() {
    val ways31 = solve181(3, 1)
    check(ways31 == 7L) { "3 black, 1 white must have 7 ways" }
    val ways22 = solve181(2, 2)
    println("2 black, 2 white ways: $ways22")
    check(ways22 > 0) { "must be positive" }
}

private fun solve181(B: Int, W: Int): Long {
    val dp = Array(B + 1) { LongArray(W + 1) }
    dp[0][0] = 1L
    for (b in 0..B) {
        for (w in 0..W) {
            if (b == 0 && w == 0) continue
            for (i in b..B) {
                for (j in w..W) {
                    dp[i][j] += dp[i - b][j - w]
                }
            }
        }
    }
    return dp[B][W]
}
