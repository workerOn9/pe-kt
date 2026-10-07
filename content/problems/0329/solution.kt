#!/usr/bin/env kotlin
/**
 * Project Euler 329 — Prime Frog（素数青蛙）
 *
 * 题目：青蛙在 1..500 号方格上每步等概率向左/右跳一格（两端自动反弹）。
 *      落在素数格时以 2/3 概率叫 P、1/3 叫 N；落在非素数格时以 1/3 叫 P、2/3 叫 N。
 *      起点在 500 格上等概率。求听到序列 PPPPNNPPPNPPNPN（15 声）的概率，化为最简分数 p/q。
 *
 * 思路推导
 * --------
 * 记 p_t[pos] 为「听到前 t 声且此刻位于 pos」的联合概率，起点 p_0[pos] = 1/500。
 * 第 t+1 声为 c：先在 pos 发声（乘 prob_c(pos)），再按 1/2 移到相邻格
 * （pos=1 只能到 2、pos=500 只能到 499）。最后对全部 pos 求和即所求概率。
 *
 * 精确计算：所有分母都是 500·3^15·2^14（15 声各带一个 3 因子、14 次移动各带一个 2 因子），
 * 取公共分母 D = 500·3^15·2^14，用 Long 分子递推（每次除以 3 或 2 都整除），最后约分。
 *
 * 验证
 * --------
 * 1. 暴力枚举：枚举全部 2^14 条名义移动序列 × 500 个起点（约 8.2×10^6 条路径），
 *    按同一公共分母 D 累加得精确分数，与 DP 结果逐位一致。
 * 2. 旁证：约分后与已知最简分数 199740353/29386561536000 一致。
 *
 * 复杂度：DP 为 O(500·15)；暴力枚举为 O(500·2^14·15)。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

private const val N_SQUARES = 500
private const val SEQ = "PPPPNNPPPNPPNPN"

/** 500 以内素性表。 */
private fun sievePrime(n: Int): BooleanArray {
    val isPrime = BooleanArray(n + 1) { it >= 2 }
    var i = 2
    while (i.toLong() * i <= n) {
        if (isPrime[i]) {
            var j = i.toLong() * i
            while (j <= n) { isPrime[j.toInt()] = false; j += i }
        }
        i++
    }
    return isPrime
}

private fun combine(num: Long, den: Long): Pair<Long, Long> {
    var a = num; var b = den
    while (b != 0L) { val t = a % b; a = b; b = t }
    val g = a
    return (num / g) to (den / g)
}

/** 精确 DP：返回 (分子, 分母)。 */
private fun dpSolution(): Pair<Long, Long> {
    val isPrime = sievePrime(N_SQUARES)
    var den = 500L
    repeat(SEQ.length) { den *= 3 }          // 15 声 × 3
    repeat(SEQ.length - 1) { den *= 2 }      // 14 次移动 × 2
    val start = den / 500
    var p = LongArray(N_SQUARES + 1) { start }
    for (idx in SEQ.indices) {
        val c = SEQ[idx]
        val np = LongArray(N_SQUARES + 1)
        for (pos in 1..N_SQUARES) {
            if (p[pos] == 0L) continue
            val prime = isPrime[pos]
            val factor = if (c == 'P') { if (prime) 2L else 1L } else { if (prime) 1L else 2L }
            np[pos] = p[pos] * factor / 3
        }
        if (idx < SEQ.length - 1) {
            val q = LongArray(N_SQUARES + 1)
            for (pos in 1..N_SQUARES) {
                if (np[pos] == 0L) continue
                val left = if (pos > 1) pos - 1 else 2
                val right = if (pos < N_SQUARES) pos + 1 else N_SQUARES - 1
                q[left] += np[pos] / 2
                q[right] += np[pos] / 2
            }
            p = q
        } else {
            p = np
        }
    }
    var num = 0L
    for (pos in 1..N_SQUARES) num += p[pos]
    return combine(num, den)
}

/** 暴力枚举：枚举 2^14 条名义移动序列 × 500 起点，累加同一分母下的分子。 */
private fun bruteSolution(): Pair<Long, Long> {
    val isPrime = sievePrime(N_SQUARES)
    var den = 500L
    repeat(SEQ.length) { den *= 3 }
    repeat(SEQ.length - 1) { den *= 2 }
    val moves = SEQ.length - 1
    var num = 0L
    for (mask in 0 until (1 shl moves)) {
        for (s in 1..N_SQUARES) {
            var pos = s
            var prod = 1L
            for (idx in SEQ.indices) {
                val prime = isPrime[pos]
                val factor = if (SEQ[idx] == 'P') { if (prime) 2L else 1L } else { if (prime) 1L else 2L }
                prod *= factor
                if (idx < moves) {
                    val goRight = (mask shr idx) and 1 == 1
                    pos = when {
                        pos == 1 -> 2
                        pos == N_SQUARES -> N_SQUARES - 1
                        goRight -> pos + 1
                        else -> pos - 1
                    }
                }
            }
            num += prod
        }
    }
    return combine(num, den)
}

private inline fun timeOf(runs: Int = 3, body: () -> Unit): Double {
    body()
    var best = Double.MAX_VALUE
    repeat(runs) {
        val st = System.nanoTime()
        body()
        val ms = (System.nanoTime() - st) / 1e6
        if (ms < best) best = ms
    }
    return best
}

fun main() {
    println("== 精确 DP ==")
    val (num, den) = dpSolution()
    println("p/q = $num/$den")
    println("参考最简分数 199740353/29386561536000： " +
        (if (num == 199740353L && den == 29386561536000L) "-> 一致" else "-> 不一致！"))

    println("== 暴力枚举互证 ==")
    val (bn, bd) = bruteSolution()
    println("暴力 p/q = $bn/$bd  " + (if (bn == num && bd == den) "-> 与 DP 一致" else "-> 不一致！"))

    val optMs = timeOf { dpSolution() }
    val bruteMs = timeOf(runs = 1) { bruteSolution() }
    println("RESULT: $num/$den")
    println("OPT_MS: " + String.format("%.4f", optMs) + "  （DP O(500·15)）")
    println("BRUTE_MS: " + String.format("%.1f", bruteMs) + "  （枚举 500·2^14 条路径）")
}
