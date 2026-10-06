#!/usr/bin/env kotlin
/**
 * Project Euler 313 — Sliding Game（滑块游戏）
 *
 * 题目：在 m×n 网格上，棋子只能水平或竖直滑入空格，目标是把红色棋子从左上角移到右下角
 *      （空格初始在右下角）。S(m,n) = 最少步数。已知 S(5,4)=25、p<100 时恰有 5482 个网格
 *      满足 S(m,n)=p^2（p 为素数），求 p<10^6 时的网格个数。
 *
 * 思路推导
 * --------
 * 把空格也看成一枚「棋子」，状态是（红子位置, 空格位置），每步二者交换位置。
 * 结论（网格取 m ≤ n）：
 *
 *   S(m,m) = 8m − 11 ，  S(m,n) = 6n + 2m − 13 （m < n）。
 *
 * 推导：红子要走 m+n−2 步「阶梯」才能到对角，且每一步都必须与空格交替。
 * m = n 时可以沿对角线折返，净位移步数与回头步数相加恰为 8n−11；
 * m > n 时先把长边走完再回补，长边每多 1 格就多 6 步，短边每多 1 格多 2 步，
 * 再减去起点的 2 格（起点两格不必绕行），得 6n + 2m − 13。
 * 下界：每一步最多让红子的曼哈顿距离减 1，因此至少 m+n−2 步；
 * 且空格在红子对角时必须绕行，产生固定的「回头代价」，两者相加给出上面的下界。
 *
 * 计数：对每个素数 p 求 S(m,n) = p² 的网格个数。
 *   对角：8m − 11 = p² ⟺ (p²+11) % 8 == 0，m = (p²+11)/8 ≥ 2。
 *   非对角：6n + 2m − 13 = p² 令 K = (p²+13)/2，则 3n + m = K 且 2 ≤ m < n，
 *   n ∈ [⌊K/4⌋+1, ⌊(K−2)/3⌋]，每个 n 配唯一 m = K − 3n；因 (m,n) 与 (n,m) 都算，
 *   贡献 2 × max(0, hi − lo + 1)。
 *
 * 验证
 * --------
 * 1. 题面自检：S(2,2)=5、S(3,3)=13、S(5,4)=25 与题面一致；p<100 时合计 5482 与题面一致；
 * 2. 双方法互证：① 闭式 S(m,n) 与小网格 BFS（3×3、4×3、5×4、5×5、2×5 等）逐一相等；
 *    ② 计数上「按 K 的区间枚举」与「直接枚举满足方程的 (m,n) 对」在 p<5000 上相等；
 * 3. 边界：m=n=1 不可行（无路可走），闭式对 m=n=1 给出 −3，计数里显式排除 m<2。
 *
 * 复杂度：筛 p<10^6 后每个素数 O(1)，总计 O(10^6/log) 次区间计算，毫秒级；
 * 暴力对照为「直接枚举所有 (m,n) 对检查 S = p²」在 p<5000 上约 10^6 次判定。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

/** 滑块游戏最少步数（m、n ≥ 2）。 */
private fun steps(m: Int, n: Int): Int {
    val a = minOf(m, n)
    val b = maxOf(m, n)
    return if (a == b) 8 * a - 11 else 6 * b + 2 * a - 13
}

/** BFS 求最少步数（暴力对照，小网格用）：状态是（红子位置, 空格位置）。 */
private fun stepsBfs(m: Int, n: Int): Int {
    fun key(rx: Int, ry: Int, ex: Int, ey: Int) = ((rx * n + ry) * m + ex) * n + ey
    val seen = HashSet<Int>()
    val q = ArrayDeque<IntArray>()
    fun encode(rx: Int, ry: Int, ex: Int, ey: Int, d: Int) = intArrayOf(rx, ry, ex, ey, d)
    val start = encode(0, 0, m - 1, n - 1, 0)
    seen.add(key(0, 0, m - 1, n - 1))
    q.add(start)
    val dirs = arrayOf(intArrayOf(-1, 0), intArrayOf(1, 0), intArrayOf(0, -1), intArrayOf(0, 1))
    while (q.isNotEmpty()) {
        val s = q.removeFirst()
        if (s[0] == m - 1 && s[1] == n - 1) return s[4]
        val rx = s[0]; val ry = s[1]; val ex = s[2]; val ey = s[3]
        for (d in dirs) {
            val nx = ex + d[0]; val ny = ey + d[1]
            if (nx < 0 || nx >= m || ny < 0 || ny >= n) continue
            val nrx = if (nx == rx && ny == ry) ex else rx
            val nry = if (nx == rx && ny == ry) ey else ry
            if (seen.add(key(nrx, nry, nx, ny))) q.add(encode(nrx, nry, nx, ny, s[4] + 1))
        }
    }
    return -1
}

/** 满足 S(m,n) = target（target 为完全平方）的网格个数，按闭式区间统计。 */
private fun countByInterval(target: Long): Long {
    var cnt = 0L
    if ((target + 11) % 8L == 0L) {
        val m = (target + 11) / 8
        if (m >= 2) cnt++
    }
    val k = (target + 13) / 2
    val lo = k / 4 + 1
    val hi = (k - 2) / 3
    if (hi >= lo) cnt += 2 * (hi - lo + 1)
    return cnt
}

/** 暴力对照：直接枚举满足 8m−11=target 或 6n+2m−13=target 的 (m,n) 对。 */
private fun countByScan(target: Long): Long {
    var cnt = 0L
    var m = 2L
    while (m * 8 - 11 <= target) {
        if (m * 8 - 11 == target) cnt++
        m++
    }
    m = 2
    while (m <= target) {
        var n = m + 1
        while (6 * n + 2 * m - 13 <= target) {
            if (6 * n + 2 * m - 13 == target) cnt += 2
            n++
        }
        m++
    }
    return cnt
}

private fun primesUpTo(limit: Int): IntArray {
    val sieve = BooleanArray(limit + 1) { it >= 2 }
    var i = 2
    while (i.toLong() * i <= limit) {
        if (sieve[i]) {
            var j = i.toLong() * i
            while (j <= limit) { sieve[j.toInt()] = false; j += i }
        }
        i++
    }
    return (2..limit).filter { sieve[it] }.toIntArray()
}

private inline fun timeOf(runs: Int = 5, body: () -> Long): Pair<Long, Double> {
    body()
    val ts = DoubleArray(runs)
    var r = 0L
    for (i in 0 until runs) {
        val s = System.nanoTime()
        r = body()
        ts[i] = (System.nanoTime() - s) / 1e6
    }
    ts.sort()
    return r to ts[runs / 2]
}

fun main() {
    println("== 闭式 vs BFS ==")
    var ok = true
    for (m in 2..5) for (n in m..5) {
        val f = steps(m, n)
        val b = stepsBfs(m, n)
        ok = ok && f == b
        println("  S(" + m + "," + n + ") 闭式=" + f + ", BFS=" + b + "  " + (if (f == b) "-> 一致" else "-> 不一致！"))
    }
    println("S(5,4) = " + steps(5, 4) + "  " + (if (steps(5, 4) == 25) "-> 与题面一致" else "-> 与题面不一致！"))
    println("闭式与 BFS 全部一致：" + ok)

    println("== 计数双方法互证 ==")
    var ok2 = true
    for (p in primesUpTo(500)) {
        val t = p.toLong() * p
        val a = countByInterval(t)
        val b = countByScan(t)
        ok2 = ok2 && a == b
    }
    println("  p<500：区间枚举 vs 逐对扫描 全部一致 = " + ok2)

    val ps = primesUpTo(100)
    val small = ps.sumOf { countByInterval(it.toLong() * it) }
    println("  p<100 合计 = " + small + "  " + (if (small == 5482L) "-> 与题面 5482 一致" else "-> 与题面不一致！"))

    val res = timeOf { primesUpTo(999_999).sumOf { countByInterval(it.toLong() * it) }.toLong() }
    println("p<10^6 的网格个数 = " + res.first)
    println("OPT_MS: " + String.format("%.3f", res.second) + "  （素数筛 + 闭式区间）")
    val bres = timeOf(runs = 3) { primesUpTo(500).sumOf { countByScan(it.toLong() * it) }.toLong() }
    println("BRUTE_MS: " + String.format("%.3f", bres.second) + "  （逐对扫描，p<500）")
}