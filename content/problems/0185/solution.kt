/**
 * PE 185 — Number Mind 16 位密钥序列（唯一解）。
 *
 * 原理：约束传播回溯。
 * - 状态：每位的候选数字集合（10-bit 掩码 domains）+ 每条 guess 已入账匹配数 mc + assigned 标记。
 * - MRV 选候选最少的未定位赋值，传播到不动点：
 *   1) 位被赋值（分支或钉死）时对命中的 guess 各 mc++（assigned 去重保证恰好一次）；
 *   2) mc == target：其余未入账位置必须避开该 guess 对应数字（否则多匹配）；
 *   3) mc + fm == target（fm = 未入账且仍可匹配的位置数）：这些位置全部钉死为匹配；
 *      mc + fm < target 直接剪枝。
 * - 叶子处逐条 guess 终验。全树搜索证明解唯一（count = 1）。
 *
 * 5 位样例（题面）独立用全枚举 0..99999 验证唯一解 39542（brute-force.kt / Python 对拍）。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
private val GUESSES = listOf(
    "5616185650518293" to 2, "3847439647293047" to 1, "5855462940810587" to 3,
    "9742855507068353" to 3, "4296849643607543" to 3, "3174248439465858" to 1,
    "4513559094146117" to 2, "7890971548908067" to 3, "8157356344118483" to 1,
    "2615250744386899" to 2, "8690095851526254" to 3, "6375711915077050" to 1,
    "6913859173121360" to 1, "6442889055042768" to 2, "2321386104303845" to 0,
    "2326509471271448" to 2, "5251583379644322" to 2, "1748270476758276" to 3,
    "4895722652190306" to 1, "3041631117224635" to 3, "1841236454324589" to 3,
    "2659862637316867" to 2
)
private const val L = 16
private val NG = GUESSES.size
/** matchMasks[p][d] = 在位置 p 放数字 d 时命中的 guess 位掩码 */
private val matchMasks: Array<IntArray> = Array(L) { p ->
    IntArray(10) { d ->
        var m = 0
        for (gi in 0 until NG) if (GUESSES[gi].first[p] == '0' + d) m = m or (1 shl gi)
        m
    }
}
/** gDigit[gi][q] = guess gi 在位置 q 的数字 */
private val gDigit: Array<IntArray> = Array(NG) { gi ->
    IntArray(L) { q -> GUESSES[gi].first[q] - '0' }
}

private fun popcount(x: Int): Int = Integer.bitCount(x)

fun solveNumberMind(): Pair<Long, String> {
    var solutionCount = 0L
    var firstSolution = ""

    fun propagate(
        domains: IntArray, mc: IntArray, assigned: BooleanArray,
        queue: ArrayList<Int>
    ): Boolean {
        while (queue.isNotEmpty()) {
            val p = queue.removeAt(queue.size - 1)
            if (assigned[p]) continue
            assigned[p] = true
            val d = Integer.numberOfTrailingZeros(domains[p])
            val hits = matchMasks[p][d]
            if (hits != 0) {
                var m = hits
                while (m != 0) {
                    val gi = java.lang.Integer.numberOfTrailingZeros(m)
                    mc[gi]++
                    if (mc[gi] > GUESSES[gi].second) return false
                    m = m and (m - 1)
                }
            }
            // 传播到不动点
            var changed = true
            while (changed) {
                changed = false
                for (gi in 0 until NG) {
                    val t = GUESSES[gi].second
                    val gd = gDigit[gi]
                    if (mc[gi] == t) {
                        // 其余未入账、未定位置必须避开 guess 数字
                        for (q in 0 until L) {
                            if (!assigned[q] && popcount(domains[q]) > 1) {
                                val nd = domains[q] and (1 shl gd[q]).inv()
                                if (nd == 0) return false
                                if (nd != domains[q]) {
                                    domains[q] = nd
                                    changed = true
                                    if (popcount(nd) == 1) queue.add(q)
                                }
                            }
                        }
                    } else {
                        // 下界：未入账且仍可能匹配的位置数 fm
                        var fm = 0
                        for (q in 0 until L) {
                            if (!assigned[q] && (domains[q] and (1 shl gd[q])) != 0) fm++
                        }
                        if (mc[gi] + fm < t) return false
                        if (mc[gi] + fm == t) {
                            for (q in 0 until L) {
                                if (!assigned[q] && popcount(domains[q]) > 1 &&
                                    (domains[q] and (1 shl gd[q])) != 0
                                ) {
                                    domains[q] = 1 shl gd[q]
                                    changed = true
                                    queue.add(q)
                                }
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    fun rec(domains: IntArray, mc: IntArray, assigned: BooleanArray) {
        if (solutionCount > 1) return
        var p = -1
        var best = 11
        for (q in 0 until L) {
            val pc = popcount(domains[q])
            if (pc == 0) return
            if (!assigned[q] && pc > 1 && pc < best) { best = pc; p = q }
        }
        if (p == -1) {
            // 全部入账：mc 应恰好等于 target（传播已保证 <=），终验
            for (gi in 0 until NG) {
                if (mc[gi] != GUESSES[gi].second) return
            }
            solutionCount++
            if (firstSolution.isEmpty()) {
                firstSolution = domains.joinToString("") {
                    Integer.numberOfTrailingZeros(it).toString()
                }
            }
            return
        }
        for (d in 0..9) {
            if (domains[p] and (1 shl d) == 0) continue
            val nd = domains.copyOf()
            val nmc = mc.copyOf()
            val nassigned = assigned.copyOf()
            nd[p] = 1 shl d
            val queue = ArrayList<Int>()
            queue.add(p)
            if (propagate(nd, nmc, nassigned, queue)) rec(nd, nmc, nassigned)
            if (solutionCount > 1) return
        }
    }

    rec(IntArray(L) { 0x3FF }, IntArray(NG), BooleanArray(L))
    return solutionCount to firstSolution
}

fun main() {
    val (count, secret) = solveNumberMind()
    check(count == 1L) { "must have exactly one solution, got $count" }
    // 终检：逐条核验 22 条 guess
    for ((g, t) in GUESSES) {
        val m = g.zip(secret).count { it.first == it.second }
        check(m == t) { "guess $g expects $t, got $m" }
    }
    println("$count solution(s): $secret")
    println(secret.toLong())
}
