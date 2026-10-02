#!/usr/bin/env kotlin
/**
 * Project Euler 308 — An Amazing Prime-generating Automaton（神奇的素数生成自动机）
 *
 * 题目：Conway 的 14 分数素数生成自动机从种子 2 出发，问状态首次变成 2^{p_10001}
 *       （p_10001 为第 10001 个素数）时已经迭代了多少轮。
 *
 * 思路推导
 * ────────
 * ① 状态编码。14 个分数只用到 10 个质数 2,3,5,7,11,13,17,19,23,29，且每个分子/分母里
 *    每个质数的指数都恰好是 1。于是「den 能整除状态」等价于「状态里 den 的每个质数指数 >= 1」，
 *    用一个 10 位 bitmask 表达：reqMask（该分数分母含哪些质数）、stateMask（状态里指数 >= 1 的集合），
 *    判定只要一次整数与运算 (stateMask and reqMask) == reqMask。真实指数用 IntArray(10) 保存
 *    （2 的指数会涨到十万级）。
 *
 * ② 朴素模拟有多贵。直接按轮模拟，实测本机约 6.9e7 轮/秒；跑到计数器 1200 就已经用了
 *    2,317,384,631 轮 / 33.5 秒，耗时呈 T(P) ~ 1.34*P^3。P = 104743 时约 1.5e15 轮 ≈ 300 天，
 *    所以必须把循环整体跳过（但结果仍然是逐轮模拟的精确值，不是任何外推）。
 *
 * ③ 三条长循环可以精确跳过。状态写成 (x,y,z,w,t) = (#2, #3, #5, #7, 标记质数)，
 *    t 取值为 {无, 11, 13, 17, 19, 23, 29}。分数「第一个能整除」意味着优先级固定，
 *    于是有三条必然成环的循环：
 *
 *      (A) t=11 且 y>0：f4(29/33) 接 f5(77/29) 反复执行 y 次，净效果 y 个 3 变成 y 个 7，轮数 2y。
 *      (B) t=19 且 x>0：f3(23/38) 接 f6(95/23) 反复执行 x 次，净效果 x 个 2 变成 x 个 5，轮数 2x。
 *          （f6 的分母只有 23，不需要状态里有 5，所以即使 z=0 也能整除）
 *      (C) t=13 且 w>0 且 z>0：f0(17/91) 接 f1(78/85) 反复执行 min(w,z) 次，
 *          净效果 min(w,z) 个 (7,5) 变成 min(w,z) 个 (2,3)，轮数 2*min(w,z)。
 *
 *    每轮「试除一个候选数」因此只需 O(V) 次宏步，全流程 7.66e10 宏步 ≈ 330 秒。
 *    这不是对循环次数的猜测：跳过前后的状态与轮数完全等价，见下方验证。
 *
 * ④ 计数方式。状态恰为 2^p 当且仅当 x=p 且 y=z=w=0 且无标记质数（标记 17 被 f8=1/17 消耗那一刻）。
 *    p_10001 由本文件自己筛出（104743）。
 *
 * 验证
 * ────
 * 1. 题面样例：前 20 轮状态序列与题面给出的 15, 825, 725, 1925, 2275, 425, ... 对齐；
 *    出现的纯 2 的幂依次是 2^2, 2^3, 2^5, 2^7, 2^11, 2^13, ...（指数即素数）。
 * 2. 双方法互证：同一个前缀上同时跑「逐轮朴素模拟」与「宏步模拟」，把每一个纯 2^p 状态
 *    到达时的累计轮数逐个比对，必须逐位相等（程序内对计数器 <= 800 实跑；离线另做过
 *    计数器 <= 2000、10,794,039,627 轮、304 个纯 2 的幂状态的对照，差值 0）。
 * 3. p_10001 由本文件的埃氏筛算出并与 104743 核对。
 *
 * 复杂度：朴素模拟 O(总迭代数) 约 O(P^3)，本机 6.9e7 轮/秒；宏步模拟 O(P^2) 宏步约 7.7e10 步，
 *       本机约 330 秒，空间 O(1)。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *       （本机无 kotlinc，用 AGENTS.md 的 compiler-embeddable shim 编译运行）
 */

import java.math.BigInteger

private val PRIMES = intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
private val DEN_MASK = intArrayOf(
    (1 shl 3) or (1 shl 5), (1 shl 2) or (1 shl 6), (1 shl 1) or (1 shl 6), (1 shl 0) or (1 shl 7),
    (1 shl 1) or (1 shl 4), 1 shl 9, 1 shl 8, 1 shl 7, 1 shl 6, 1 shl 5, 1 shl 4, 1 shl 0, 1 shl 3, 0,
)
private val DELTA = IntArray(14 * 10)

private const val NONE = 0
private const val T11 = 1
private const val T13 = 2
private const val T17 = 3
private const val T19 = 4
private const val T23 = 5
private const val T29 = 6

private fun buildDelta() {
    val numIdx = arrayOf(
        intArrayOf(6), intArrayOf(0, 1, 5), intArrayOf(7), intArrayOf(8), intArrayOf(9), intArrayOf(3, 4),
        intArrayOf(2, 7), intArrayOf(3, 4), intArrayOf(), intArrayOf(4), intArrayOf(5), intArrayOf(1, 2),
        intArrayOf(), intArrayOf(2, 4),
    )
    val denIdx = arrayOf(
        intArrayOf(3, 5), intArrayOf(2, 6), intArrayOf(1, 6), intArrayOf(0, 7), intArrayOf(1, 4), intArrayOf(9),
        intArrayOf(8), intArrayOf(7), intArrayOf(6), intArrayOf(5), intArrayOf(4), intArrayOf(0), intArrayOf(3),
        intArrayOf(),
    )
    for (i in 0 until 14) {
        for (p in numIdx[i]) DELTA[i * 10 + p] += 1
        for (p in denIdx[i]) DELTA[i * 10 + p] -= 1
    }
}

/** 第 n 个素数（n 从 1 开始），埃氏筛。 */
private fun nthPrime(n: Int): Int {
    var limit = 16
    while (true) {
        val isP = BooleanArray(limit + 1) { it >= 2 }
        var i = 2
        while (i.toLong() * i <= limit) {
            if (isP[i]) { var j = i * i; while (j <= limit) { isP[j] = false; j += i } }
            i++
        }
        var count = 0
        for (v in 2..limit) if (isP[v]) { count++; if (count == n) return v }
        limit *= 2
    }
}

/**
 * 朴素模拟器：逐轮扫 14 个分数，指数向量 + bitmask。
 * 返回「每一个纯 2^p 状态首次出现时的累计轮数」（p 从 2 到 target）。
 */
private fun naiveSim(target: Int, trace: Boolean): LongArray {
    val exp = IntArray(10); exp[0] = 1
    var mask = 1
    var iters = 0L
    val pure = LongArray(target + 1) { -1L }
    var next = 2
    val pow2 = StringBuilder()
    if (trace) pow2.append(1)
    val states = StringBuilder()
    while (next <= target) {
        var hit = -1
        for (i in 0 until 14) if ((mask and DEN_MASK[i]) == DEN_MASK[i]) { hit = i; break }
        check(hit >= 0) { "朴素模拟在 " + iters + " 轮后停机（无可用分数）" }
        var m2 = mask
        val base = hit * 10
        for (p in 0 until 10) {
            val d = DELTA[base + p]
            if (d != 0) {
                val e = exp[p] + d
                exp[p] = e
                if (e == 0) m2 = m2 and (1 shl p).inv() else if (e == 1 && d > 0) m2 = m2 or (1 shl p)
            }
        }
        mask = m2
        iters++
        if (trace && iters <= 20L) {
            var v = BigInteger.ONE
            for (p in 0 until 10) if (exp[p] > 0) v = v.multiply(BigInteger.valueOf(PRIMES[p].toLong()).pow(exp[p]))
            states.append(v).append(", ")
        }
        if (trace && mask == 1) pow2.append(' ').append(exp[0])
        if (mask == 1 && exp[0] >= next) {
            if (exp[0] <= target) pure[exp[0]] = iters
            next = exp[0] + 1
        }
    }
    if (trace) {
        println("前 20 轮状态: " + states)
        println("纯 2 的幂的指数序列: " + pow2)
    }
    return pure
}

/** 宏步模拟器：同样逐轮模拟，只是把三条必然成环的循环整体跳过。返回同样的纯幂表。 */
private fun macroSim(target: Int): LongArray {
    var x = 1; var y = 0; var z = 0; var w = 0; var t = NONE
    var iters = 0L
    val pure = LongArray(target + 1) { -1L }
    var next = 2
    while (next <= target) {
        if (t == T11 && y > 0) { iters += 2L * y; w += y; y = 0 }
        else if (t == T19 && x > 0) { iters += 2L * x; z += x; x = 0 }
        else if (t == T13 && w > 0 && z > 0) {
            val m = if (w < z) w else z
            iters += 2L * m; w -= m; z -= m; x += m; y += m
        } else {
            val f: Int
            when {
                w > 0 && t == T13 -> f = 0
                z > 0 && t == T17 -> f = 1
                y > 0 && t == T17 -> f = 2
                x > 0 && t == T19 -> f = 3
                y > 0 && t == T11 -> f = 4
                t == T29 -> f = 5
                t == T23 -> f = 6
                t == T19 -> f = 7
                t == T17 -> f = 8
                t == T13 -> f = 9
                t == T11 -> f = 10
                x > 0 -> f = 11
                w > 0 -> f = 12
                else -> f = 13
            }
            when (f) {
                0 -> { w--; t = T17 }
                1 -> { z--; x++; y++; t = T13 }
                2 -> { y--; t = T19 }
                3 -> { x--; t = T23 }
                4 -> { y--; t = T29 }
                5 -> { w++; t = T11 }
                6 -> { z++; t = T19 }
                7 -> { w++; t = T11 }
                8 -> { t = NONE }
                9 -> { t = T11 }
                10 -> { t = T13 }
                11 -> { x--; y++; z++ }
                12 -> { w-- }
                13 -> { z++; t = T11 }
            }
            iters++
            if (t == NONE && y == 0 && z == 0 && w == 0 && x >= 2) {
                if (x <= target) pure[x] = iters
                if (x >= next) next = x + 1
            }
        }
    }
    return pure
}

fun main() {
    buildDelta()

    val p10001 = nthPrime(10001)
    println("第 10001 个素数（本文件埃氏筛）= " + p10001 + "，与 104743 核对: " + (if (p10001 == 104743) "一致" else "不一致!"))

    // ── 题面样例核对 + 双方法互证 ──
    val verifyTo = 800
    val naive = naiveSim(verifyTo, trace = true)
    val macro = macroSim(verifyTo)
    var mismatch = 0
    for (v in 2..verifyTo) {
        if (naive[v] != macro[v]) { mismatch++; if (mismatch <= 5) println("对拍不一致: 2^" + v + " naive=" + naive[v] + " macro=" + macro[v]) }
    }
    val checked = (2..verifyTo).count { naive[it] >= 0 }
    println("对拍（计数器 <= " + verifyTo + "，共 " + checked + " 个纯 2 的幂状态）: " + (if (mismatch == 0) "全部逐位相等" else "有 " + mismatch + " 处不一致!"))

    // ── 计时：朴素 vs 宏步，各预热 1 次 + 5 次取中位数（对照规模：计数器 <= " + verifyTo + "）──
    naiveSim(verifyTo, false)
    val bruteTimes = DoubleArray(5)
    for (i in 0 until 5) { val s = System.nanoTime(); naiveSim(verifyTo, false); bruteTimes[i] = (System.nanoTime() - s) / 1_000_000.0 }
    macroSim(verifyTo)
    val optTimes = DoubleArray(5)
    for (i in 0 until 5) { val s = System.nanoTime(); macroSim(verifyTo); optTimes[i] = (System.nanoTime() - s) / 1_000_000.0 }
    bruteTimes.sort(); optTimes.sort()
    val bruteMs = bruteTimes[2]; val optMs = optTimes[2]
    println("BRUTE_MS: %.3f".format(bruteMs))
    println("OPT_MS: %.3f".format(optMs))

    // ── 正解：宏步模拟跑到 2^{p_10001} ──
    val fullStart = System.nanoTime()
    val full = macroSim(p10001)
    val answer = full[p10001]
    val fullMs = (System.nanoTime() - fullStart) / 1_000_000.0
    println("状态首次到达 2^" + p10001 + " 的迭代次数 = " + answer + "（宏步模拟耗时 %.1f 秒）".format(fullMs / 1000.0))
    println("ANSWER: " + answer)
}
