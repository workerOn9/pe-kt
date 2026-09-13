/**
 * Project Euler 054 — Poker Hands
 *
 * 优化解：给每手牌算一个 Long 排序键——高位放牌型等级（0 高牌 … 8 同花顺），低位放
 * 5 个逐级比较用的点数，键大者胜，比较两手牌只需一次整数比较。
 * 点数序列按「出现次数降序、同次数点数降序」展开：成对、三条的关键点数自动排在
 * 单张前面，两对也自然是「大对、小对、散张」，无需逐牌型分支拼装。
 * 全程只用定长 IntArray 与循环，不建 HashMap、不用流式排序。
 * 牌型判定：同花比较 5 张花色；顺子即点数全不同且极差为 4；A-2-3-4-5 单独判为 5 高顺子。
 * 键里固定留 5 个点数槽位并补 0，避免不同牌型的键因槽位数不同而错序。
 *
 * 复杂度：每行 O(5 log 5)，1000 行共 O(1000 · 5 log 5)。
 * 需从题目目录运行（读取同目录 poker.txt）：
 *   kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.io.File

fun cardValue(card: String): Int = when (card[0]) {
    'T' -> 10
    'J' -> 11
    'Q' -> 12
    'K' -> 13
    'A' -> 14
    else -> card[0] - '0'
}

fun handKey(hand: List<String>): Long {
    val values = IntArray(5) { cardValue(hand[it]) }
    values.sort()

    var flush = true
    for (i in 1..4) if (hand[i][1] != hand[0][1]) { flush = false; break }

    val counts = IntArray(15)
    for (v in values) counts[v]++
    var c4 = 0
    var c3 = 0
    var c2 = 0
    for (v in 2..14) when (counts[v]) {
        4 -> c4++
        3 -> c3++
        2 -> c2++
    }

    // 按「出现次数降序、点数降序」展开成至多 5 个点数
    val ordered = IntArray(5)
    var n = 0
    for (c in 4 downTo 1) for (v in 14 downTo 2) if (counts[v] == c) ordered[n++] = v

    val wheel = counts[14] == 1 && counts[5] == 1 && counts[4] == 1 && counts[3] == 1 && counts[2] == 1
    val straight = n == 5 && values[4] - values[0] == 4

    val category = when {
        flush && (straight || wheel) -> 8
        c4 == 1 -> 7
        c3 == 1 && c2 == 1 -> 6
        flush -> 5
        straight || wheel -> 4
        c3 == 1 -> 3
        c2 == 2 -> 2
        c2 == 1 -> 1
        else -> 0
    }
    val tie = when (category) {
        8, 4 -> intArrayOf(if (wheel) 5 else values[4])
        5 -> IntArray(5) { values[4 - it] }
        else -> ordered
    }

    var key = category.toLong()
    for (i in 0 until 5) key = key * 15 + (if (i < tie.size) tie[i] else 0)
    return key
}

fun solve(path: String = "poker.txt"): Long {
    var wins = 0L
    for (line in File(path).readLines()) {
        if (line.isBlank()) continue
        val cards = line.trim().split(" ")
        if (handKey(cards.subList(0, 5)) > handKey(cards.subList(5, 10))) wins++
    }
    return wins
}

fun main() {
    println(solve())
}
