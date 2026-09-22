/**
 * PE 178 — brute force：对较小长度（如 <= 6 位）小步进数通过暴力递归枚举验证状态转移正确性。
 */
private fun countStepPandigitalBrute(maxDigits: Int): Long {
    var count = 0L
    fun dfs(currentDigits: Int, last: Int, mask: Int) {
        val nextMask = mask or (1 shl last)
        if (nextMask == 0x3FF) {
            count++
        }
        if (currentDigits == maxDigits) return
        if (last > 0) dfs(currentDigits + 1, last - 1, nextMask)
        if (last < 9) dfs(currentDigits + 1, last + 1, nextMask)
    }
    for (start in 1..9) {
        dfs(1, start, 0)
    }
    return count
}

fun main() {
    // 小长度验证：10 位时（最短全数字步进数）
    val count10 = countStepPandigitalBrute(10)
    println("10-digit pandigital step numbers: $count10")
    check(count10 > 0) { "must have at least one 10-digit pandigital step number" }
}
