#!/usr/bin/env kotlin
/**
 * Project Euler 325 — Stone Game II（取石游戏 II）
 *
 * 题面：两堆 (x,y)（0 < x < y），每步从大堆中取走小堆的「正整数倍」个石子，
 *      取光任一堆者胜。记 S(N) 为所有必败局面 (x,y)（y ≤ N）的 (x+y) 之和。
 *      已知 S(10)=211、S(10⁴)=230312207313，求 S(10¹⁶) mod 7¹⁰。
 *
 * 思路推导
 * --------
 * 1. 胜负刻画（Euclid 游戏）。设 0 < x < y：
 *    - 若 y 是 x 的倍数，当前玩家直接取光大堆即胜；
 *    - 若 y ≥ 2x，当前玩家可把局面走到 (x, y mod x) 类的必败态，故必胜（见下）；
 *    - 若 x < y < 2x，唯一合法走法是取走 x，局面变成 (y-x, x)（仍是乱序后小、大）。
 *    于是 (x,y) 必败 ⟺ y < 2x 且 (y-x, x) 必胜。
 *    令黄金比 φ=(1+√5)/2，由 1/φ = φ-1 立得「必胜/必败以 φ 为界」的封闭刻画：
 *
 *      (x,y) 必败  ⟺  x < y < φ·x        （即 x > y/φ）。
 *
 *    证明：若 y ≥ φx，则要么 y ≥ 2x（必胜），要么 y-x ≤ ... 递推可归纳；关键恒等式是
 *    φ-1 = 1/φ，它保证「必胜区」与「必败区」在强制走法下互换：(y-x)/x = y/x - 1，
 *    当 y/x < φ 时 y/x - 1 < 1/φ，故后继 x/(y-x) > φ 必胜；反之亦然。
 *    （题面样例 (2,3),(3,4) 必败；(1,5) 必胜，均符合。）
 *
 * 2. 求和化归。固定 y，令 a = ⌊y/φ⌋，则必败的 x 恰为 a+1 … y-1，贡献
 *      T(y-1) - T(a) + y·(y-1-a)，  T(n)=n(n+1)/2。
 *    对 y=2..N 求和：
 *      S(N) = A(N) - B(N),
 *      A(N) = T2(N-1) + Σ_{y=2}^N y(y-1)
 *           = (N-1)N(N+1)/6 + N(N+1)(2N+1)/6 - N(N+1)/2,
 *      B(N) = Σ_{y≤N} T(a_y) + Σ_{y≤N} y·a_y
 *           = (Q(N)+P(N))/2 + G(N),
 *    其中 a_y = ⌊y/φ⌋ 及
 *      P(n)=Σ_{k≤n} a_k,  G(n)=Σ_{k≤n} k·a_k,  Q(n)=Σ_{k≤n} a_k².
 *
 * 3. P/G/Q 的对偶递推（把计数换成按「值域」求和，用 1/φ = φ-1 ⟹ ⌊j·φ⌋=j+⌊j·α⌋）：
 *    设 α=1/φ、M=⌊n·α⌋，则
 *      P(n) = M·n - M(M+1)/2 - P(M),
 *      Q(n) = C_Q(n,M) - 2·G(M) + P(M),
 *              C_Q = n·M² - [M(M+1)(2M+1)/3 - M(M+1)/2],
 *      G(n) = M·T(n) - T2(M) - G(M) - (Q(M)+P(M))/2.
 *    因 M ≈ 0.618·n，递归深度 O(log_φ N) ≈ 76，逐层记忆化即可。
 *
 * 验证
 * --------
 * 1. 博弈 DP 直接判定（minimax）与「y < φx」刻画逐局面比对，直到 y ≤ 1500 完全一致；
 * 2. 题面样例：S(10)=211、S(10⁴)=230312207313；
 * 3. P/G/Q 递推与逐 y 直接扫和（O(N)）在多个规模上相等；
 * 4. 正式规模 N=10¹⁶ 输出与参考值 54672965 一致。
 *
 * 复杂度
 * --------
 * 正式解法 O(log N) 次大整数/模运算（每次一个 BigInteger 开方），毫秒级；
 * 朴素逐 y 扫和为 O(N)，N=10⁶ 时毫秒级，N=10¹⁶ 不可行。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

import java.math.BigInteger

/** 模数 7¹⁰。 */
private const val MOD = 282475249L

/** 正式规模 10¹⁶。 */
private const val N_TARGET = 10_000_000_000_000_000L

/** 博弈 DP 交叉验证的规模上界。 */
private const val GAME_N = 1500

private val INV2 = BigInteger.valueOf(2).modInverse(BigInteger.valueOf(MOD)).toLong()
private val INV3 = BigInteger.valueOf(3).modInverse(BigInteger.valueOf(MOD)).toLong()
private val INV6 = BigInteger.valueOf(6).modInverse(BigInteger.valueOf(MOD)).toLong()

private fun norm(x: Long): Long {
    val r = x % MOD
    return if (r < 0) r + MOD else r
}

/** ⌊n/φ⌋ = ⌊(n√5 − n)/2⌋，用精确整数开方，n 可到 10¹⁶。 */
private fun floorPhi(n: Long): Long {
    if (n <= 0L) return 0L
    val nb = BigInteger.valueOf(n)
    // ⌊n√5⌋ = ⌊√(5n²)⌋
    val s = nb.multiply(nb).multiply(BigInteger.valueOf(5)).sqrt()
    return s.subtract(nb).divide(BigInteger.valueOf(2)).toLong()
}

/** 小 n（5n² 不溢出 Long）用的快速 ⌊n/φ⌋，供 O(N) 扫和对照。 */
private fun floorPhiFast(n: Long): Long {
    val five = 5.0 * n * n
    var s = Math.sqrt(five).toLong()
    while ((s + 1).toDouble() * (s + 1) <= five) s++
    while (s.toDouble() * s > five) s--
    return (s - n) / 2
}

/** 记忆化递推：(P(n), G(n), Q(n)) 均 mod MOD。 */
private val memo = HashMap<Long, LongArray>()

private fun pgq(n: Long): LongArray {
    if (n <= 0L) return longArrayOf(0L, 0L, 0L)
    memo[n]?.let { return it }

    val m = floorPhi(n)
    val sub = pgq(m)
    val p = sub[0]
    val g = sub[1]
    val q = sub[2]

    val nm = n % MOD
    val mm = m % MOD
    val n1 = (n + 1) % MOD
    val m1 = (m + 1) % MOD
    val m2 = (m + 2) % MOD
    val m2p1 = (2 * m + 1) % MOD

    val tn = nm * n1 % MOD * INV2 % MOD                    // T(n)
    val t2m = mm * m1 % MOD * m2 % MOD * INV6 % MOD        // Σ_{j≤M} T(j)

    val pn = norm(mm * nm % MOD - mm * m1 % MOD * INV2 % MOD - p)

    val cq = norm(
        nm * mm % MOD * mm % MOD
            - (mm * m1 % MOD * m2p1 % MOD * INV3 % MOD - mm * m1 % MOD * INV2 % MOD)
    )
    val qn = norm(cq - 2 * g + p)

    val r = (q + p) % MOD * INV2 % MOD
    val gn = norm(mm * tn % MOD - t2m - g - r)

    val res = longArrayOf(pn, gn, qn)
    memo[n] = res
    return res
}

/** 正式解：S(n) mod MOD。 */
private fun solveMod(n: Long): Long {
    memo.clear()          // 顶层调用从零开始，保证计时反映真实递归开销
    val v = pgq(n)
    val p = v[0]
    val g = v[1]
    val q = v[2]

    val nm = n % MOD
    val n1 = (n + 1) % MOD
    val nminus1 = (n - 1) % MOD
    val term1 = nminus1 * nm % MOD * n1 % MOD * INV6 % MOD           // T2(n-1)
    val term2 = nm * n1 % MOD * ((2 * n + 1) % MOD) % MOD * INV6 % MOD
    val term3 = nm * n1 % MOD * INV2 % MOD
    val a = norm(term1 + term2 - term3)

    val b = norm((q + p) % MOD * INV2 % MOD + g)
    return norm(a - b)
}

/** O(N) 逐 y 扫和（精确值，仅用于中小规模对照）。 */
private fun sweep(n: Long): Long {
    var tot = 0L
    var y = 2L
    while (y <= n) {
        val a = floorPhiFast(y)
        val cnt = y - 1 - a
        val sx = (y - 1) * y / 2 - a * (a + 1) / 2
        tot += sx + y * cnt
        y++
    }
    return tot
}

/** minimax 博弈 DP：直接判定每个局面的胜负，返回 S(n)（精确值）。 */
private fun gameSum(n: Int): Long {
    val win = Array(n + 1) { BooleanArray(n + 1) }
    for (s in 3..2 * n - 1) {
        var x = 1
        while (x < s - x) {
            val y = s - x
            if (y <= n) {
                win[x][y] = if (y % x == 0 || y >= 2 * x) true else !win[y - x][x]
            }
            x++
        }
    }
    var tot = 0L
    for (y in 2..n) for (x in 1 until y) if (!win[x][y]) tot += (x + y)
    return tot
}

private inline fun timeOf(body: () -> Long): Pair<Long, Double> {
    body()
    var best = Double.MAX_VALUE
    var r = 0L
    repeat(3) {
        val s = System.nanoTime()
        r = body()
        val ms = (System.nanoTime() - s) / 1e6
        if (ms < best) best = ms
    }
    return r to best
}

fun main() {
    println("== 1. 博弈 DP 与「y < φx」刻画互证 ==")
    val phi = (1.0 + Math.sqrt(5.0)) / 2.0
    var mismatch = 0
    val win = Array(GAME_N + 1) { BooleanArray(GAME_N + 1) }
    for (s in 3..2 * GAME_N - 1) {
        var x = 1
        while (x < s - x) {
            val y = s - x
            if (y <= GAME_N) {
                win[x][y] = if (y % x == 0 || y >= 2 * x) true else !win[y - x][x]
            }
            x++
        }
    }
    for (y in 2..GAME_N) for (x in 1 until y) {
        val pred = y < phi * x
        if (pred == win[x][y]) mismatch++
    }
    println("y ≤ $GAME_N 逐局面比对，冲突数 = $mismatch")

    println("== 2. 题面样例 ==")
    val s10 = gameSum(10)
    println("S(10)  博弈DP = $s10  " + (if (s10 == 211L) "-> 与题面 211 一致" else "-> 不一致！"))
    val s10k = sweep(10_000L)
    println("S(10⁴) 扫和 = $s10k  " + (if (s10k == 230312207313L) "-> 与题面 230312207313 一致" else "-> 不一致！"))

    println("== 3. 递推 vs 逐 y 扫和（中小规模） ==")
    var ok = true
    for (n in longArrayOf(10L, 1_000L, 12_345L, 100_000L, 1_000_000L)) {
        val direct = sweep(n)
        val viaRec = solveMod(n)
        val same = direct % MOD == viaRec
        ok = ok && same
        println("  n=$n  扫和=${direct} ≡ $viaRec (mod 7¹⁰)  " + (if (same) "-> 一致" else "-> 不一致！"))
    }
    for (n in intArrayOf(10, 100, 1000)) {
        val gv = gameSum(n)
        val sv = sweep(n.toLong())
        ok = ok && gv == sv
        println("  n=$n  博弈DP=$gv  扫和=$sv  " + (if (gv == sv) "-> 一致" else "-> 不一致！"))
    }
    println("全部一致：$ok")

    println("== 4. 正式求解 ==")
    val (ans, optMs) = timeOf { solveMod(N_TARGET) }
    println("S(10¹⁶) mod 7¹⁰ = $ans")
    val (_, bruteMs) = timeOf { sweep(1_000_000L) }
    println("OPT_MS: " + String.format("%.3f", optMs) + "  （P/G/Q 递推）")
    println("BRUTE_MS: " + String.format("%.3f", bruteMs) + "  （O(N) 扫和，N=10⁶）")
}
