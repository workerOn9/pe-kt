/**
 * Project Euler 148 — Exploring Pascal's Triangle（探究帕斯卡三角形）
 *
 * 思路：由库默尔（Kummer）定理，素数 p 在 C(n,k) 中的指数等于「k + (n−k) 在 p 进制下相加时的进位次数」。
 * 于是 C(n,k) 不被 p 整除 ⟺ 该加法完全不进位 ⟺ k 在 p 进制的每一位都不超过 n 的对应位。
 * 固定行 n（p 进制数位记为 d_0, d_1, …），第 i 位的 k 可取 0..d_i 共 d_i + 1 种，各位彼此独立，故
 *
 *     A(n) = ∏_i (d_i + 1)            （p = 7）
 *
 * 题目求的是前十亿行的总和 F(N) = Σ_{n<N} A(n)，N = 10⁹。把 n 的 7 进制数位看成位置做数位 DP：
 * 从最高位扫到最低位，扫到第 i 位时更高位已与 N 一致（贡献乘子 cur = ∏_{j<i}(d_j + 1)），
 * 若这一位取 x < d_i 则低位完全自由，而长度 L 的自由低位对 ∏(digit+1) 的求和为
 *
 *     Σ_{digits} ∏(d_j + 1) = (Σ_{x=0}^{6}(x+1))^L = 28^L
 *
 * 于是
 *
 *     F(N) = Σ_i cur_i · (d_i(d_i+1)/2) · 28^{m−1−i},   cur_i = ∏_{j<i}(d_j + 1)
 *
 * 其中 d_i(d_i+1)/2 = Σ_{x=0}^{d_i−1}(x+1) 是当前位取小于 d_i 时所有乘积之和。
 *
 * 复杂度：时间 O(log_7 N)（N = 10⁹ 只有 11 位），空间 O(log_7 N)。
 * 结果上界 28^{11} ≈ 8.29×10^15，Long（≈9.22×10^18）绰绰有余，中间量无溢出。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 行 n 中不被 7 整除的项数 A(n) = ∏(7 进制数位 + 1)。 */
fun nonDivisibleInRow(n: Long): Long {
    var x = n
    var product = 1L
    while (x > 0) {
        product *= x % 7 + 1                      // 当前最低位 + 1
        x /= 7
    }
    return product
}

/** 前 rows 行（第 0 行到第 rows−1 行）中不被 7 整除的项数。 */
fun countNonDivisible(rows: Long): Long {
    if (rows <= 0L) return 0L
    val digits = IntArray(32)                     // 7 进制数位，低位在前（Long 在 7 进制下最多 23 位）
    var len = 0
    var x = rows
    while (x > 0) {
        digits[len++] = (x % 7).toInt()
        x /= 7
    }

    var lowerWays = 1L                            // 28^(当前位之下还剩几位)
    for (i in 1 until len) lowerWays *= 28

    var total = 0L
    var prefixProduct = 1L                        // cur = ∏(已定高位 + 1)
    for (i in len - 1 downTo 0) {                 // 自最高位向最低位扫
        val d = digits[i]
        total += prefixProduct * (d.toLong() * (d + 1) / 2) * lowerWays
        prefixProduct *= d + 1
        lowerWays /= 28
    }
    return total
}

fun solve(): Long = countNonDivisible(1_000_000_000L)

/** 用最朴素的递推把帕斯卡三角形前 rows 行逐行建出来（模 7），数出非零项个数。仅用于自检。 */
fun literalCount(rows: Int): Long {
    var row = LongArray(1) { 1L }
    var count = 0L
    for (n in 0 until rows) {
        for (v in row) if (v % 7L != 0L) count++
        val next = LongArray(row.size + 1)
        for (k in next.indices) {
            var v = 0L
            if (k > 0) v += row[k - 1]
            if (k < row.size) v += row[k]
            next[k] = v % 7
        }
        row = next
    }
    return count
}

fun verifySample() {
    // 题面：前七行无一项被 7 整除，共 1+2+…+7 = 28 项
    check(countNonDivisible(7) == 28L) { "前七行应为 28 项，实得 ${countNonDivisible(7)}" }
    // 题面：前一百行共 100·101/2 = 5050 项，其中 2361 项不被 7 整除
    check(100L * 101 / 2 == 5050L)
    check(countNonDivisible(100) == 2361L) { "前一百行应为 2361 项，实得 ${countNonDivisible(100)}" }
    // 边界：零行没有项；只有第一行时 1 项
    check(countNonDivisible(0) == 0L)
    check(countNonDivisible(1) == 1L)
    // 由 ∏(数位+1)：第 6 行是 7 项，第 7 行（7 进制 10）只剩 2 项，第 8 行（11）是 4 项
    check(nonDivisibleInRow(6) == 7L)
    check(nonDivisibleInRow(7) == 2L)
    check(nonDivisibleInRow(8) == 4L)
    // 与「逐行建三角形」的朴素定义对拍前 60 行
    for (rows in 0..60) {
        check(literalCount(rows) == countNonDivisible(rows.toLong())) {
            "第 $rows 行规模下公式与逐行递推不符：${countNonDivisible(rows.toLong())} vs ${literalCount(rows)}"
        }
    }
}

fun main() {
    verifySample()
    repeat(5) { solve() }                        // JIT 预热
    // solve() 只有约 1 µs，单次计时会落在计时器精度边缘，改为连跑 1000 次取每次均摊
    val reps = 1000
    var acc = 0L
    val start = System.nanoTime()
    repeat(reps) { acc += solve() }
    val perCallMs = (System.nanoTime() - start) / 1e6 / reps
    check(acc == reps * solve()) { "重复计时的累加值与单次结果不一致" }   // 顺带挡住死代码消除
    System.err.printf("optimized: %.4f ms%n", perCallMs)
    println(solve())
}
