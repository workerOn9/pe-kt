#!/usr/bin/env kotlin
/**
 * Project Euler 321 — Swapping Counters（交换棋子）
 *
 * 题目：2n+1 个方格，n 红 n 蓝分列两端，中间一个空格。棋子可滑到相邻空格或跳过相邻棋子。
 *      M(n) = 完全对调红蓝位置的最少步数。已知 M(3)=15 是三角形数。
 *      求使 M(n) 为三角形数的 n 的前 40 项之和。
 *
 * 思路推导
 * --------
 * 1. M(n) = n² + 2n = (n+1)² − 1。BFS 对 n=1,2,3,4 验证此公式。
 *    （每枚棋子需越过 n 枚异色棋子 + 空格；n² 次跳跃 + 2n 次滑动 = n² + 2n。）
 *
 * 2. M(n) 为三角形数 ⟺ n² + 2n = k(k+1)/2 对某个正整数 k。
 *    变形：8(n+1)² = (2k+1)² + 7，即 (2k+1)² − 8(n+1)² = −7。
 *    令 x = 2k+1, y = n+1，得佩尔方程 x² − 8y² = −7。
 *
 * 3. x² − 8y² = −7 的基本解为 (1,1) 与 (5,2)。
 *    x² − 8y² = 1 的基本解为 (3,1)。
 *    若 (x,y) 是 x²−8y²=−7 的解，则 (3x+8y, x+3y) 也是解（乘以基本单位 3+√8）。
 *    从 (1,1) 出发的链：n = 0, 3, 22, 133, ...（n=0 舍去）
 *    从 (5,2) 出发的链：n = 1, 10, 63, 372, ...
 *    两链合并排序即得 1, 3, 10, 22, 63, ...，与题面前五项一致。
 *
 * 验证
 * --------
 * 1. BFS 模拟验证 M(n) = n²+2n（n=1,2,3,4）；
 * 2. 暴力枚举 n ≤ 10⁷ 中使 n²+2n 为三角形数的 n，前 5 项与佩尔方程生成一致；
 * 3. 佩尔方程生成的前 5 项 1,3,10,22,63 之和 = 99，与题面一致。
 *
 * 复杂度
 * --------
 * 暴力枚举 O(N)（N=10⁷，约 10⁷ 次三角形数判定）；
 * 佩尔方程直接生成 O(40) 次递推，常数时间。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

/** BFS 求 M(n)：返回最少步数。 */
private fun bfsM(n: Int): Int {
    val size = 2 * n + 1
    val start = "R".repeat(n) + " " + "B".repeat(n)
    val target = "B".repeat(n) + " " + "R".repeat(n)
    val dist = HashMap<String, Int>()
    val queue = ArrayDeque<String>()
    dist[start] = 0
    queue.add(start)
    while (queue.isNotEmpty()) {
        val s = queue.removeFirst()
        val d = dist[s]!!
        if (s == target) return d
        val gap = s.indexOf(' ')
        // slide: 相邻棋子滑入空格
        for (delta in intArrayOf(-1, 1)) {
            val from = gap + delta
            if (from in 0 until size && s[from] != ' ') {
                val ns = s.toCharArray()
                ns[gap] = ns[from]
                ns[from] = ' '
                val key = String(ns)
                if (key !in dist) {
                    dist[key] = d + 1
                    queue.add(key)
                }
            }
        }
        // hop: 棋子跳过相邻棋子落入空格
        for (delta in intArrayOf(-2, 2)) {
            val from = gap + delta
            val mid = gap + delta / 2
            if (from in 0 until size && s[from] != ' ' && s[mid] != ' ') {
                val ns = s.toCharArray()
                ns[gap] = ns[from]
                ns[from] = ' '
                val key = String(ns)
                if (key !in dist) {
                    dist[key] = d + 1
                    queue.add(key)
                }
            }
        }
    }
    return -1
}

/** 判断 m 是否为三角形数（BigInteger 版本，避免大数溢出）。 */
private fun isTriangularBig(m: java.math.BigInteger): Boolean {
    if (m.signum() <= 0) return false
    val disc = m.multiply(java.math.BigInteger.valueOf(8)).add(java.math.BigInteger.ONE)
    val sqrtDisc = sqrtBig(disc)
    return sqrtDisc.multiply(sqrtDisc) == disc &&
        (sqrtDisc.subtract(java.math.BigInteger.ONE)).mod(java.math.BigInteger.valueOf(2L)) == java.math.BigInteger.ZERO
}

private fun sqrtBig(n: java.math.BigInteger): java.math.BigInteger {
    if (n.signum() <= 0) return java.math.BigInteger.ZERO
    var x = java.math.BigInteger.ONE.shiftLeft(n.bitLength() / 2 + 1)
    while (true) {
        val y = x.add(n.divide(x)).shiftRight(1)
        if (y >= x) return x
        x = y
    }
}

/** 佩尔方程生成前 count 个 n。 */
private fun pellTerms(count: Int): List<Long> {
    val result = mutableListOf<Long>()
    var xa = 5L; var ya = 2L   // chain A: n=1, 10, 63, ...
    var xb = 11L; var yb = 4L  // chain B: n=3, 22, 129, ...
    while (result.size < count) {
        val na = ya - 1
        val nb = yb - 1
        if (na <= nb) {
            result.add(na)
            val nx = 3 * xa + 8 * ya
            val ny = xa + 3 * ya
            xa = nx; ya = ny
        } else {
            result.add(nb)
            val nx = 3 * xb + 8 * yb
            val ny = xb + 3 * yb
            xb = nx; yb = ny
        }
    }
    return result
}

private inline fun <T> timeOf(runs: Int = 3, body: () -> T): Pair<T, Double> {
    body()
    var best = Double.MAX_VALUE
    var r: T? = null
    for (k in 0 until runs) {
        val st = System.nanoTime()
        r = body()
        val ms = (System.nanoTime() - st) / 1e6
        if (ms < best) best = ms
    }
    return r!! to best
}

fun main() {
    println("== BFS 验证 M(n) = n² + 2n ==")
    for (n in 1..4) {
        val m = bfsM(n)
        val formula = n.toLong() * n + 2 * n
        println("  M($n) = $m, 公式 = $formula  " + (if (m == formula.toInt()) "-> 一致" else "-> 不一致！"))
    }

    println("== 暴力枚举 n ≤ 10⁷ ==")
    val bruteN = 10_000_000L
    val bruteTerms = mutableListOf<Long>()
    val bruteStart = System.nanoTime()
    for (n in 1..bruteN) {
        val m = n * n + 2 * n
        if (isTriangularBig(java.math.BigInteger.valueOf(m))) bruteTerms.add(n)
    }
    val bruteMs = (System.nanoTime() - bruteStart) / 1e6
    println("  找到 ${bruteTerms.size} 项，前 5 项 = ${bruteTerms.take(5)}")
    println("  前 5 项之和 = ${bruteTerms.take(5).sum()}  " + (if (bruteTerms.take(5).sum() == 99L) "-> 与题面 99 一致" else "-> 不一致！"))

    println("== 佩尔方程生成 ==")
    val terms = pellTerms(40)
    println("  前 5 项 = ${terms.take(5)}")
    println("  前 5 项之和 = ${terms.take(5).sum()}  " + (if (terms.take(5).sum() == 99L) "-> 与题面 99 一致" else "-> 不一致！"))
    val allTriangular = terms.all {
        val n = java.math.BigInteger.valueOf(it)
        isTriangularBig(n.multiply(n).add(n.multiply(java.math.BigInteger.valueOf(2L))))
    }
    println("  全部 40 项的 M(n) 均为三角形数：$allTriangular")
    val matchCount = minOf(terms.size, bruteTerms.size)
    val allMatch = (0 until matchCount).all { terms[it] == bruteTerms[it] }
    println("  与暴力枚举前 $matchCount 项一致：$allMatch")

    val (ans, ms) = timeOf { terms.sum() }
    println("== 正式求解 ==")
    println("前 40 项之和 = $ans")
    println("OPT_MS: " + String.format("%.3f", ms) + "  （佩尔方程生成）")
    println("BRUTE_MS: " + String.format("%.3f", bruteMs) + "  （暴力枚举 n ≤ 10⁷）")
}
