#!/usr/bin/env kotlin
/**
 * Project Euler 310 — Nim Square（平方尼姆）
 *
 * 题目：三堆石子 (a,b,c)，每次从一堆里取走一个平方数个石子，取不动者输（普通规则）。
 * 已知 0 ≤ a ≤ b ≤ c ≤ 29 时必败局面有 1160 个，求 0 ≤ a ≤ b ≤ c ≤ 100000 的必败局面个数。
 *
 * 思路推导
 * --------
 * 这是三个独立公平组合游戏之和。由 Sprague–Grundy 定理：
 *   局面 (a,b,c) 必败 <=> g(a) ^ g(b) ^ g(c) = 0，
 *   g(n) = mex{ g(n - k^2) : k >= 1, k^2 <= n }，g(0) = 0。
 * 先在 O(M*sqrt(M)) 内递推出 g(0..M)（M = 100000 约 3.2e7 次转移），得到一张 SG 表。
 *
 * 直接枚举三元组是 O(M^3/6) ≈ 1.7e14 次，不可行。关键在于：SG 值的取值集合 V 极小，
 * 可以按 SG 值分组计数，再把「有序三元组」折算成「多重集」。
 *
 *   cnt[v] = 值域 [0,M] 内 g(n) = v 的 n 的个数；
 *   T = 满足 g(a)^g(b)^g(c)=0 的【有序】三元组数
 *     = sum over v1,v2 in V, v3 = v1^v2 in V of cnt[v1]*cnt[v2]*cnt[v3]
 *       （(v1,v2) 按有序枚举，所以 T 已包含全部排列），
 *   E = 恰好两个相等的多重集数，分两种形态：
 *       (a,a,c) with a<c  <=> g(c)=0，a 有 c 个选择    -> sum_{c in Z0} c
 *       (a,b,b) with a<b  <=> g(a)=0，b 有 M-a 个选择 -> sum_{a in Z0} (M-a)
 *     其中 Z0 = { n in [0,M] : g(n)=0 }，两个和用前缀和 O(1) 算出；
 *   Z = 三个全相等的个数 = |Z0|（<=> g(a)=0）。
 *
 * 由 T = 6D + 3E + Z（互不相同 / 恰两等 / 全等 各贡献 6 / 3 / 1 个有序排列）得
 *   D = (T - 3E - Z)/6，答案 = D + E + Z。
 *
 * 验证
 * --------
 * 1. 题面样例外推：M = 29 → 计数公式给出 1160，与题面完全一致；
 * 2. 小规模双方法互证：M ∈ {5,10,20,60,200,1000} 上「三重循环直接枚举」与上述
 *    组合计数公式逐一相等；
 * 3. 第三方法（逐对枚举 a<=b + 每 SG 值的后缀计数，O(m^2)）在 M=20000 上与计数公式互证；
 * 4. SG 值分布：不同 SG 值个数 V 与各 cnt[v] 打印出来供人工复核。
 *
 * 复杂度：SG 表 O(M*sqrt(M)) 时间 / O(M) 空间；计数阶段 O(V^2)，V 很小。
 *         暴力对照为 O(M^3/6) 次判定（M=1000 约 1.68e8 次）。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *       （本机无 kotlinc，用 scripts/kotlinc-shim.ps1 编译运行）
 */

/** 题面规模上限。 */
private const val M = 100_000

/** 暴力对照用的规模（三重循环 M=1000 约 1.68e8 次判定，耗时可测）。 */
private const val BRUTE_MAX = 1000

/** 第三方法（逐对 + 后缀计数）的中等规模对照点。 */
private const val CHECK_MAX = 20_000

/**
 * 0..m 的 SG 表：g(n) = mex{ g(n-k^2) : k^2 <= n }，g(0)=0。
 * 用「时间戳」数组代替每次清零的布尔数组；mex 上界 = 合法着数 = floor(sqrt(m))。
 */
private fun sgTable(m: Int): IntArray {
    val squares = ArrayList<Int>()
    var k = 1
    while (k.toLong() * k <= m) { squares.add(k * k); k++ }
    val g = IntArray(m + 1)
    val stamp = IntArray(squares.size + 2)
    var cur = 0
    for (n in 1..m) {
        cur++
        for (s in squares) {
            if (s > n) break
            stamp[g[n - s]] = cur
        }
        var mex = 0
        while (stamp[mex] == cur) mex++
        g[n] = mex
    }
    return g
}

/**
 * 组合计数：0 <= a <= b <= c <= m 中 g(a)^g(b)^g(c)=0 的多重集个数。
 * 内部先算有序三元组 T，再用 T = 6D + 3E + Z 折算回多重集。
 */
private fun countLosing(g: IntArray, m: Int): Long {
    val maxV = g.max()
    val cnt = LongArray(maxV + 1)
    for (v in g) cnt[v]++

    // T：按 SG 值分组的卷积式统计，(v1,v2) 有序枚举即为有序三元组数
    var t = 0L
    for (v1 in 0..maxV) {
        if (cnt[v1] == 0L) continue
        for (v2 in 0..maxV) {
            if (cnt[v2] == 0L) continue
            val v3 = v1 xor v2
            if (v3 <= maxV) t += cnt[v1] * cnt[v2] * cnt[v3]
        }
    }

    // Z0：SG 值为 0 的下标集合，遍历一遍即得 |Z0| 与下标和（等价于其前缀和）
    var zCount = 0L
    var sumIdx = 0L
    for (i in g.indices) if (g[i] == 0) { zCount++; sumIdx += i }

    // E = (a,a,c) 形态 sum_{c in Z0} c  +  (a,b,b) 形态 sum_{a in Z0} (m-a)
    val e = sumIdx + (m.toLong() * zCount - sumIdx)
    val z = zCount
    val d = (t - 3L * e - z) / 6L
    return d + e + z
}

/** 暴力对照：三重循环直接枚举 0 <= a <= b <= c <= m 的必败局面（小规模用）。 */
private fun countLosingBrute(g: IntArray, m: Int): Long {
    var c = 0L
    for (a in 0..m) {
        for (b in a..m) {
            val t = g[a] xor g[b]
            for (cc in b..m) if ((t xor g[cc]) == 0) c++
        }
    }
    return c
}

/**
 * 第三方法（与前两者结构都不同）：逐对枚举 a<=b，用每个 SG 值的后缀计数
 * 一次性数出 c in [b,m] 中 g(c) = g(a)^g(b) 的个数，O(m^2)。中等规模交叉验证用。
 */
private fun countLosingPairwise(g: IntArray, m: Int): Long {
    val maxV = g.max()
    val suf = Array(maxV + 1) { LongArray(m + 2) }
    for (v in 0..maxV) {
        val row = suf[v]
        for (n in m downTo 0) row[n] = row[n + 1] + if (g[n] == v) 1L else 0L
    }
    var c = 0L
    for (a in 0..m) {
        for (b in a..m) {
            val t = g[a] xor g[b]
            if (t <= maxV) c += suf[t][b]
        }
    }
    return c
}

/** 预热 1 次后跑 runs 轮，返回毫秒中位数。 */
private inline fun medianMs(runs: Int = 5, body: () -> Long): Double {
    body()
    val ts = DoubleArray(runs)
    for (i in 0 until runs) {
        val s = System.nanoTime()
        body()
        ts[i] = (System.nanoTime() - s) / 1_000_000.0
    }
    ts.sort()
    return ts[runs / 2]
}

fun main() {
    val g = sgTable(M)

    // SG 值分布
    val maxV = g.max()
    println("不同 SG 值个数 V = " + (maxV + 1) + "（取值 0.." + maxV + "）")
    println("cnt[v] 分布：")
    val cnt = LongArray(maxV + 1)
    for (v in g) cnt[v]++
    for (v in 0..maxV) println("  g = " + v + " -> " + cnt[v])
    println("SG=0 的下标个数 |Z0| = " + cnt[0])

    // 1) 题面样例外推
    val s29 = countLosing(sgTable(29), 29)
    println("M=29 计数公式 = " + s29 + (if (s29 == 1160L) " -> 与题面 1160 一致" else " -> 与题面不一致!"))

    // 2) 双方法互证：三重循环暴力 vs 组合计数公式
    println("双方法互证（三重循环 vs 计数公式）：")
    var allOk = true
    for (m in intArrayOf(5, 10, 20, 60, 200, 1000)) {
        val gm = sgTable(m)
        val f = countLosing(gm, m)
        val b = countLosingBrute(gm, m)
        val ok = f == b
        allOk = allOk && ok
        println("  M=" + m + " : 公式=" + f + ", 暴力=" + b + (if (ok) " -> 一致" else " -> 不一致!"))
    }
    println("全部规模一致：" + allOk)

    // 3) 第三方法（逐对 + 后缀计数）在中等规模上再互证一次
    val gCheck = sgTable(CHECK_MAX)
    val fCheck = countLosing(gCheck, CHECK_MAX)
    val pCheck = countLosingPairwise(gCheck, CHECK_MAX)
    println("第三方法互证 M=" + CHECK_MAX + " : 公式=" + fCheck + ", 逐对后缀=" + pCheck
        + (if (fCheck == pCheck) " -> 一致" else " -> 不一致!"))

    // 4) 计时（预热 1 次 + 5 次取中位数）
    val gBrute = sgTable(BRUTE_MAX)
    val bruteMs = medianMs { countLosingBrute(gBrute, BRUTE_MAX) }
    val optMs = medianMs { countLosing(sgTable(M), M) }   // 含 SG 表构建，端到端
    println("BRUTE_MS: " + "%.3f".format(bruteMs) + "  (三重循环，M=" + BRUTE_MAX + ")")
    println("OPT_MS: " + "%.3f".format(optMs) + "  (SG 表 + 组合计数，M=" + M + ")")

    val result = countLosing(g, M)
    println("必败局面数（0 <= a <= b <= c <= " + M + "）= " + result)
    println("ANSWER: " + result)
}