/**
 * Project Euler 084 — Monopoly Odds（暴力对照解）
 *
 * 思路：不建转移矩阵，直接按规则做蒙特卡洛模拟——反复掷两颗骰子、结算格子效果，统计每个格子被「落点」
 * 的次数，取频率最高的三格拼成六位数。骰子用固定随机种子，结果可复现。
 * 复杂度：O(模拟次数)，本题取 10^8 次掷骰；统计误差约为 0.002%，而第三、四名相差约 0.06%，足以分辨。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 */

private const val BOARD = 40
private const val JAIL = 10
private const val GO_TO_JAIL = 30

private val CC_SQUARES = intArrayOf(2, 17, 33)
private val CH_SQUARES = intArrayOf(7, 22, 36)
private val RAILWAYS = intArrayOf(5, 15, 25, 35)
private val UTILITIES = intArrayOf(12, 28)

private fun nextOf(squares: IntArray, from: Int): Int {
    for (s in squares) if (s > from) return s
    return squares[0]
}

fun solveBruteForce(): Long {
    val rnd = java.util.Random(20260912L)
    val hits = LongArray(BOARD)
    var pos = 0
    var doubles = 0
    var rolls = 0
    while (rolls < 100_000_000) {
        val i = 1 + rnd.nextInt(4)
        val j = 1 + rnd.nextInt(4)
        if (i == j && doubles == 2) {
            pos = JAIL
            doubles = 0
            hits[pos]++
            rolls++
            continue
        }
        pos = (pos + i + j) % BOARD
        doubles = if (i == j) doubles + 1 else 0
        if (pos == GO_TO_JAIL) {
            pos = JAIL
        } else if (CC_SQUARES.contains(pos)) {
            when (rnd.nextInt(16)) {
                0 -> pos = 0
                1 -> pos = JAIL
            }
        } else if (CH_SQUARES.contains(pos)) {
            when (rnd.nextInt(16)) {
                0 -> pos = 0
                1 -> pos = JAIL
                2 -> pos = 11
                3 -> pos = 24
                4 -> pos = 39
                5 -> pos = 5
                6, 7 -> pos = nextOf(RAILWAYS, pos)
                8 -> pos = nextOf(UTILITIES, pos)
                9 -> pos = (pos - 3 + BOARD) % BOARD
            }
        }
        hits[pos]++
        rolls++
    }
    val order = (0 until BOARD).sortedByDescending { hits[it] }
    var result = 0L
    for (k in 0 until 3) result = result * 100 + order[k]
    return result
}

fun main() {
    println(solveBruteForce())
}
