/**
 * Project Euler 151 — Paper sheets of standard sizes（偏好 A5）· 暴力对照解
 *
 * 思路：与 solution.kt 相同的 DP 方法，但用更清晰的状态转移写法。
 */

fun solveBruteForce(): Double {
    val maxPapers = 16
    val dp = Array(maxPapers + 1) { Array(maxPapers + 1) { Array(maxPapers + 1) { Array(maxPapers + 1) { 0.0 } } } }
    
    dp[1][0][0][0] = 1.0
    
    var expectedSinglePaper = 0.0
    
    for (batch in 1 until 16) {
        val newDp = Array(maxPapers + 1) { Array(maxPapers + 1) { Array(maxPapers + 1) { Array(maxPapers + 1) { 0.0 } } } }
        
        for (a2 in 0..maxPapers) {
            for (a3 in 0..maxPapers) {
                for (a4 in 0..maxPapers) {
                    for (a5 in 0..maxPapers) {
                        val prob = dp[a2][a3][a4][a5]
                        if (prob <= 0) continue
                        
                        val total = a2 + a3 + a4 + a5
                        if (total == 0) continue
                        
                        if (total == 1 && batch > 1 && batch < 15) {
                            expectedSinglePaper += prob
                        }
                        
                        if (a5 > 0) newDp[a2][a3][a4][a5 - 1] += prob * a5.toDouble() / total
                        if (a4 > 0) newDp[a2][a3][a4 - 1][a5 + 1] += prob * a4.toDouble() / total
                        if (a3 > 0) newDp[a2][a3 - 1][a4 + 1][a5] += prob * a3.toDouble() / total
                        if (a2 > 0) newDp[a2 - 1][a3 + 1][a4][a5] += prob * a2.toDouble() / total
                    }
                }
            }
        }
        
        for (a2 in 0..maxPapers) {
            for (a3 in 0..maxPapers) {
                for (a4 in 0..maxPapers) {
                    for (a5 in 0..maxPapers) {
                        dp[a2][a3][a4][a5] = newDp[a2][a3][a4][a5]
                    }
                }
            }
        }
    }
    
    return expectedSinglePaper
}

fun main() {
    val start = System.nanoTime()
    val result = solveBruteForce()
    val elapsed = (System.nanoTime() - start).toDouble() / 1e6
    System.err.printf("brute: %.4f ms%n", elapsed)
    println("%.6f".format(result))
}
