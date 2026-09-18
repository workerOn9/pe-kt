/**
 * Project Euler 121 — Disc Game Prize Fund（圆碟游戏奖金）
 *
 * 思路：第 k 轮开始时袋中有 k 张红碟与 1 张蓝碟（初始 1 红 1 蓝，每轮结束加一张红碟），
 * 所以第 k 轮抽到蓝碟的概率是 1/(k+1)，抽到红碟是 k/(k+1)。蓝碟全程只有一张，
 * 故「蓝碟数多于红碟数」等价于「n 轮里至少抽到 ⌊n/2⌋+1 次蓝碟」。
 * 用面向计数的 DP 求精确概率：把 n 轮后「恰好 b 次蓝碟」的概率统一放大 D = (n+1)!
 * 倍变成整数，转移为
 *     count[b] ← (count[b]·k + count[b−1]) / (k+1)，
 * 这两项正是第 k 轮抽红（概率 k/(k+1)）与抽蓝（1/(k+1)）的加权，整除性由 D 含因子
 * (k+1) 保证。获胜概率 p = W/D；奖金是整数英镑，庄家不亏本的临界值为 1/p，
 * 故答案 = ⌊D/W⌋（含玩家支付的 £1 本金）。全程整数运算，无浮点比较。
 * 题面锚点：4 轮时 W/D = 11/120 且奖金 £10，在 main 里自检。
 *
 * 复杂度：O(n²) 时间、O(n) 空间（n = 15 时约 120 次乘加），微秒级。
 * 数值上界：D = 16! = 20922789888000，count 与中间量均远小于 Long 上限。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 获胜概率的精确分数 W/D，两者都是整数（分母为 (turns+1)!）。 */
fun winningFraction(turns: Int): Pair<Long, Long> {
    var denominator = 1L
    for (k in 1..turns) denominator *= (k + 1)                  // D = (turns+1)!
    var counts = LongArray(turns + 1)
    counts[0] = denominator                                      // 0 轮后 0 次蓝碟，概率 1
    for (turn in 1..turns) {
        val next = LongArray(turns + 1)
        for (blue in 0..turn) {
            var numerator = 0L
            if (blue <= turn - 1) numerator += counts[blue] * turn          // 本轮抽到红碟
            if (blue >= 1) numerator += counts[blue - 1]                     // 本轮抽到蓝碟
            next[blue] = numerator / (turn + 1)
        }
        counts = next
    }
    var winning = 0L
    for (blue in 0..turns) if (blue > turns - blue) winning += counts[blue]
    return winning to denominator
}

/** 游戏进行 turns 轮时，庄家应为单场游戏预留的最大奖金基金（整数英镑，含本金）。 */
fun prizeFund(turns: Int): Long {
    val (winning, denominator) = winningFraction(turns)
    return denominator / winning
}

fun solve(): Long = prizeFund(15)

fun main() {
    val (numerator4, denominator4) = winningFraction(4)
    check(120L * numerator4 == 11L * denominator4) {
        "题面锚点：4 轮获胜概率应为 11/120，实际 $numerator4/$denominator4"
    }
    check(prizeFund(4) == 10L) { "题面锚点：4 轮奖金应为 £10，实际 ${prizeFund(4)}" }
    println(solve())
}
