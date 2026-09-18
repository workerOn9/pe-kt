/**
 * Project Euler 145 — Reversible Numbers（可逆数）
 *
 * 思路：数位配对 + 进位链上的双比特 DP。
 * 设 n 有 d 位，数位为 a_{d-1}…a_0（a_{d-1} ≥ 1 保证 n 恰有 d 位，a_0 ≥ 1 保证
 * reverse(n) 没有前导零）。把第 i 位与第 d-1-i 位配成一对，它们用的是同一对数位
 * （顺序相反、和相同），记 t_i = a_i + a_{d-1-i}。加法从低位向高位推进：
 * 第 i 位的数位是 (t_i + c_i) mod 10，进位 c_{i+1} = ⌊(t_i + c_i)/10⌋ ∈ {0,1}
 * （t_i ≤ 18）。于是整条数位和序列是
 *   t_0, …, t_{m-1}, [奇数位时中间位贡献 2a_m], t_{m-1}, …, t_0    （m = ⌊d/2⌋）
 * 的一条回文串：第 d-1-i 位用的正好是同一个 t_i。
 *
 * 每个位置只有两条约束：数位为奇数、进位是 0 或 1。于是「从外向里」逐层剥离数位对时，
 * 状态只要两个比特——本层低位的进位 a（由更外层给定）与本层高位的进位 b（由更内层产生）：
 *   R_k(a, b) = Σ_t ways(t) · 1[奇(t+a)] · Σ_{b'} 1[奇(t+b')] · 1[⌊(t+b')/10⌋ = b] · R_{k+1}(⌊(t+a)/10⌋, b')
 * 基底：d 为偶数时内外之间什么也没有，两个进位必须是同一个，R_m = 单位表；
 * d 为奇数时中间位是 2a_m + c，个位奇偶 = c 的奇偶，故必须 c = 1，而
 * 2a_m + 1 ≤ 19 的个位恒为奇数，于是 R_m(1,0) = R_m(1,1) = 5（a_m = 0…4 / 5…9）、R_m(0,·) = 0。
 * 最外层（第 0 层）的 a_0 与 a_{d-1} 都不得为 0，故它的数位对计数用 endWays 而不是通用 ways。
 * d 位数的个数 = R_0(0,0) + R_0(0,1)：最高位进位为 1 时和会多出一位前导 1，也是奇数，合法。
 * d = 1 单独判 0：n + reverse(n) = 2n 个位必为偶数。
 *
 * 复杂度：d ≤ 9 时有 m = ⌊d/2⌋ ≤ 4 层，每层 2 × 19 × 2 × 2 次内层查表，
 * 9 个长度合计约 3000 次迭代，即 O(D²) 次常数运算、空间 O(1)（两个 2×2 表）。
 * 不枚举任何候选数，全部答案是闭式的直接计算。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 通用数位对：x + y = t，x, y ∈ 0…9 的 (x, y) 有序对个数。 */
private val pairWays = IntArray(19) { t -> if (t <= 9) t + 1 else 19 - t }

/** 端点数位对：x + y = t，x, y ∈ 1…9（首位与末位都不允许为 0）的有序对个数。 */
private val endPairWays = IntArray(19) { t -> (1..9).count { x -> t - x in 1..9 } }

/** 数位 x 的十进制个位是奇数。x ≤ 19，只需看个位。 */
private fun oddDigit(x: Int): Boolean = x % 10 % 2 == 1

/** 在 inner 之外再包一层数位对，数位对的和按 ways 计数。 */
private fun wrapLayer(ways: IntArray, inner: Array<LongArray>): Array<LongArray> {
    val out = Array(2) { LongArray(2) }
    for (low in 0..1) {
        for (t in 0..18) {
            val w = ways[t]
            if (w == 0 || !oddDigit(t + low)) continue
            val nextLow = (t + low) / 10                       // 本层低位送出的进位
            for (high in 0..1) {
                var acc = 0L
                for (exit in 0..1) {                           // 本层高位收到的进位，由更内层送出
                    if (!oddDigit(t + exit)) continue
                    if ((t + exit) / 10 != high) continue
                    acc += inner[nextLow][exit]
                }
                out[low][high] += w * acc
            }
        }
    }
    return out
}

/** 恰有 d 位的可逆数个数。 */
fun countByLength(d: Int): Long {
    if (d <= 1) return 0L
    val m = d / 2
    var inner: Array<LongArray> = if (d % 2 == 0) {
        arrayOf(longArrayOf(1, 0), longArrayOf(0, 1))          // 偶数位：内外进位相同
    } else {
        arrayOf(longArrayOf(0, 0), longArrayOf(5, 5))          // 奇数位：中间位要求进位为 1
    }
    repeat(m - 1) { inner = wrapLayer(pairWays, inner) }       // 1…m-1 层是通用数位对
    val outer = wrapLayer(endPairWays, inner)                  // 第 0 层首尾都不能为 0
    return outer[0][0] + outer[0][1]
}

/** 小于 10^maxDigits 的可逆数个数（数位长度 1…maxDigits 之和）。 */
fun solve(maxDigits: Int = 9): Long {
    var total = 0L
    for (d in 1..maxDigits) total += countByLength(d)
    return total
}

/**
 * 样例专用：按定义判断 n 是否可逆（与主解的 DP 路径无关，只用来钉死题意）。
 * 末位为 0 时 reverse(n) 会有前导零，题面不允许，直接排除。
 */
fun reversibleByDefinition(n: Long): Boolean {
    if (n <= 0 || n % 10 == 0L) return false
    var rest = n
    var reversed = 0L
    while (rest > 0) {
        reversed = reversed * 10 + rest % 10
        rest /= 10
    }
    return allDigitsOdd(n + reversed)
}

/** value 的十进制各位是否全为奇数（不经过字符串）。 */
private fun allDigitsOdd(value: Long): Boolean {
    if (value <= 0) return false
    var rest = value
    while (rest > 0) {
        if (rest % 10 % 2 == 0L) return false
        rest /= 10
    }
    return true
}

/** 样例专用：逐个枚举，返回 [1, limit) 中可逆数的个数。 */
private fun naiveCountBelow(limit: Long): Long {
    var count = 0L
    for (n in 1 until limit) if (reversibleByDefinition(n)) count++
    return count
}

fun verifySample() {
    // 题面例子：36 + 63 = 99、409 + 904 = 1313，两式结果的每位都是奇数
    check(allDigitsOdd(36 + 63) && allDigitsOdd(409L + 904)) { "99 与 1313 的每一位都应为奇数" }
    check(listOf(36L, 63L, 409L, 904L).all { reversibleByDefinition(it) }) { "36/63/409/904 应当都可逆" }
    check(!reversibleByDefinition(10L))    // 10 + 1 = 11 全奇数，但 reverse(10) = 1 带前导零，题面不允许
    check(!reversibleByDefinition(11L))    // 11 + 11 = 22
    check(countByLength(1) == 0L)          // 一位数：2n 个位必为偶数
    // 题面锚点：一千以内恰有 120 个可逆数
    check(solve(3) == 120L) { "一千以内应为 120，实得 ${solve(3)}" }
    // 10^5 与数位长度边界对齐（1…5 位），用定义法逐个枚举复核整段 DP
    check(solve(5) == naiveCountBelow(100_000L)) { "10^5 以内 DP 与枚举不一致" }

    // 闭式解（推导见 analysis.md）：偶数位 d = 2m 有 20·30^(m-1) 个，
    // d = 4k+3 有 5·20^(k+1)·25^k 个，d = 4k+1 恒为 0。逐长度与 DP 对照。
    var pow30 = 1L
    for (m in 1..5) {
        check(countByLength(2 * m) == 20L * pow30) { "d=${2 * m} 与闭式 20·30^(m-1) 不符" }
        pow30 *= 30L
    }
    var pow20 = 20L
    var pow25 = 1L
    for (k in 0..3) {
        check(countByLength(4 * k + 3) == 5L * pow20 * pow25) { "d=${4 * k + 3} 与闭式 5·20^(k+1)·25^k 不符" }
        pow20 *= 20L
        pow25 *= 25L
        check(countByLength(4 * k + 1) == 0L) { "d=${4 * k + 1} 应为 0" }
    }
}

fun main() {
    verifySample()
    repeat(5) { solve() }                                      // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    for (d in 1..9) System.err.print("d=$d:${countByLength(d)} ")
    System.err.println()
    println(answer)
}
