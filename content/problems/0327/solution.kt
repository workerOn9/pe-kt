#!/usr/bin/env kotlin
/**
 * Project Euler 327 — Rooms of Doom（厄运之室）
 *
 * 题目：C 为最多随身携带的安全卡数，R 为要穿过的房间数。
 *      M(C,R) = 从发卡机取出的最少卡数。已知 M(3,6)=123、M(4,6)=23、
 *      Σ_{C=3}^{4} M(C,6)=146、Σ_{C=3}^{10} M(C,10)=10382。
 *      求 Σ_{C=3}^{40} M(C,30)。
 *
 * 思路推导
 * --------
 * 从最右边的房间往左逐间计算「要逃出本房间及其右侧所有房间，需要带足多少卡」。
 * 最右房间只需 1 张卡开门即可；每往左一间，就要再多带 1 张（进入下一间的开门卡）。
 *
 * 当某间房需要的卡数 need 超过携带上限 C 时，必须从左侧房间搬卡过来：
 * 一次「往返搬运」从上一间房带货过来，可净搬运 Transport = C − 2 张卡
 * （1 张进本间、1 张返程），但消耗 C 张取自上一间房的卡。于是
 *   need 减掉若干次 Transport，consumed 加上每次的 C，
 * 直到 need < C，再把 remaining = need + consumed + 1 作为左侧房间的新需求（+1 为进入本间的开卡）。
 * 对 C=3（Transport=1）迭代次数极多，用整除一次性算出批量次数即可。
 *
 * 验证
 * --------
 * 1. 题面样例：M(3,6)=123、M(4,6)=23、Σ_{C=3}^{4}M(C,6)=146、Σ_{C=3}^{10}M(C,10)=10382
 *    由同一函数直接算出（下方输出）。
 * 2. 朴素逐次循环版与批量整除版在 C≥5 上结果一致（验证批量公式正确）。
 *
 * 复杂度：Σ_{C=3}^{40} 各一次 O(R) 递推（R=30），常数级；朴素逐次版对 C=3 不可行。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

/** 批量整除版：M(C,R)。 */
private fun minCards(cards: Int, rooms: Int): Long {
    var need = 1L
    val transport = cards - 2             // 每次往返净搬运的卡数
    var room = rooms
    while (room > 0) {
        var consumed = 0L
        if (need >= cards) {
            var moves = (need - cards) / transport
            if (need - moves * transport >= cards) moves++
            need -= moves * transport
            consumed += moves * cards
        }
        need = need + consumed + 1        // 进入本间的 1 张开卡 + 左侧需备的卡
        room--
    }
    return need
}

/** 朴素逐次循环版（仅用于小规模互证；C=3 很慢）。 */
private fun minCardsNaive(cards: Int, rooms: Int): Long {
    var need = 1L
    val transport = cards - 2
    var room = rooms
    while (room > 0) {
        var consumed = 0L
        while (need >= cards) { need -= transport; consumed += cards }
        need = need + consumed + 1
        room--
    }
    return need
}

private inline fun timeOf(runs: Int = 3, body: () -> Long): Pair<Long, Double> {
    var r = body()
    var best = Double.MAX_VALUE
    repeat(runs) {
        val st = System.nanoTime()
        r = body()
        val ms = (System.nanoTime() - st) / 1e6
        if (ms < best) best = ms
    }
    return r to best
}

fun main() {
    println("== 题面样例 ==")
    println("M(3,6) = ${minCards(3, 6)}  " + (if (minCards(3, 6) == 123L) "-> 与题面 123 一致" else "-> 不一致！"))
    println("M(4,6) = ${minCards(4, 6)}  " + (if (minCards(4, 6) == 23L) "-> 与题面 23 一致" else "-> 不一致！"))
    val s46 = (3..4).sumOf { minCards(it, 6) }
    println("Σ M(C,6), C=3..4 = $s46  " + (if (s46 == 146L) "-> 与题面 146 一致" else "-> 不一致！"))
    val s10 = (3..10).sumOf { minCards(it, 10) }
    println("Σ M(C,10), C=3..10 = $s10  " + (if (s10 == 10382L) "-> 与题面 10382 一致" else "-> 不一致！"))

    println("== 批量版 vs 朴素版互证 ==")
    var ok = true
    for (c in 5..12) for (r in 1..12) {
        if (minCards(c, r) != minCardsNaive(c, r)) { ok = false; println("  不一致 C=$c R=$r") }
    }
    println("  C=5..12, R=1..12 全部一致：$ok")

    println("== 正式求解 ==")
    val (ans, ms) = timeOf { (3..40).sumOf { minCards(it, 30) } }
    println("Σ M(C,30), C=3..40 = $ans")
    println("OPT_MS: " + String.format("%.4f", ms) + "  （批量整除递推）")
    val (bans, bms) = timeOf(runs = 1) { (5..40).sumOf { minCardsNaive(it, 30) } }
    println("BRUTE_MS: " + String.format("%.4f", bms) + "  （朴素逐次版 C=5..40） 结果=$bans")
}
