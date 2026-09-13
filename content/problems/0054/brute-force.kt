/**
 * Project Euler 054 — Poker Hands（对比实现，教学用）
 *
 * 与 solution.kt 的「打包成 Long 键」写法相对：这里把每手牌解析成 Hand 对象，
 * compareTo 里按题面规则先比牌型、再逐级比点数；每次比较都重新推导分组与牌型，
 * 每行还要构造两个对象。两者逻辑等价、结果相同，用于对比键化写法的分配开销。
 *
 * 需从题目目录运行（读取同目录 poker.txt）：
 *   kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.io.File

fun cardValueOf(card: String): Int = when (card[0]) {
    'T' -> 10
    'J' -> 11
    'Q' -> 12
    'K' -> 13
    'A' -> 14
    else -> card[0] - '0'
}

class Hand(cards: List<String>) : Comparable<Hand> {
    private val values: List<Int> = cards.map { cardValueOf(it) }.sortedDescending()
    private val flush: Boolean = cards.map { it[1] }.distinct().size == 1
    private val counts = IntArray(15)
    private val wheel: Boolean = values == listOf(14, 5, 4, 3, 2)
    private val straight: Boolean = values.distinct().size == 5 && values[0] - values[4] == 4

    init {
        for (v in values) counts[v]++
    }

    private fun category(): Int = when {
        flush && (straight || wheel) -> 8
        counts.any { it == 4 } -> 7
        counts.any { it == 3 } && counts.any { it == 2 } -> 6
        flush -> 5
        straight || wheel -> 4
        counts.any { it == 3 } -> 3
        counts.count { it == 2 } == 2 -> 2
        counts.any { it == 2 } -> 1
        else -> 0
    }

    /** 按「出现次数降序、点数降序」排列的点数：成对/三条的关键点数自然排在前面。 */
    private fun orderedValues(): List<Int> =
        (2..14).filter { counts[it] > 0 }
            .sortedWith(compareByDescending<Int> { counts[it] }.thenByDescending { it })

    private fun tieBreak(): List<Int> = when (category()) {
        8, 4 -> if (wheel) listOf(5) else listOf(values[0])
        7, 6 -> orderedValues().take(2)
        5, 0 -> values
        else -> orderedValues()
    }

    override fun compareTo(other: Hand): Int {
        val byCategory = category().compareTo(other.category())
        if (byCategory != 0) return byCategory
        val a = tieBreak()
        val b = other.tieBreak()
        for (i in 0 until 5) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x.compareTo(y)
        }
        return 0
    }
}

fun solveBruteForce(path: String = "poker.txt"): Long {
    var wins = 0L
    for (line in File(path).readLines()) {
        if (line.isBlank()) continue
        val cards = line.trim().split(" ")
        if (Hand(cards.take(5)) > Hand(cards.drop(5))) wins++
    }
    return wins
}

fun main() {
    println(solveBruteForce())
}
