/**
 * PE 172 — 18 位数、每个数字出现不超过 3 次。
 * 组合计数：枚举出现次数分配 c0..c9（Σc=18, 0≤ci≤3），
 * 排列数 = 18! / Πci!，再减去首位为 0 的 17! / ((c0-1)!Π_{i>0}ci!)。
 * 已由 Python 组合实算 = 227485267000992000（并与小规模暴力枚举对拍一致）。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
private val fact = LongArray(19).also { it[0] = 1L; for (i in 1..18) it[i] = it[i - 1] * i }

private var total = 0L

private fun rec(digit: Int, remaining: Int, counts: IntArray) {
    if (digit == 9) {
        if (remaining > 3) return
        counts[9] = remaining
        var num = fact[18]
        for (c in counts) num /= fact[c]
        if (counts[0] > 0) {
            counts[0]--
            var zeroFirst = fact[17]
            for (c in counts) zeroFirst /= fact[c]
            num -= zeroFirst
            counts[0]++
        }
        total += num
        return
    }
    for (c in 0..minOf(3, remaining)) {
        counts[digit] = c
        rec(digit + 1, remaining - c, counts)
    }
}

fun solve(): Long {
    total = 0L
    rec(0, 18, IntArray(10))
    return total
}

fun main() {
    println(solve())
}
