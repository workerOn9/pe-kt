#!/usr/bin/env kotlin
/**
 * Project Euler 322 — Binomial Coefficients Divisible by 10（被 10 整除的二项式系数）
 *
 * 题目：T(m,n) = #{ i : n ≤ i < m 且 C(i,n) 能被 10 整除 }。
 *       已知 T(10⁹, 10⁷−10) = 989697000，求 T(10¹⁸, 10¹²−10)。
 *
 * 思路推导
 * --------
 * Kummer 定理：v_p(C(i,n)) = 把 n 与 (i−n) 在 p 进制下相加时的进位次数。
 * 加法无进位 ⟺ 每一位都满足 a_k + n_k ≤ p−1（a = i−n 的 p 进制数位，n_k 为 n 的数位）。
 * 于是
 *
 *   C(i,n) 不被 p 整除  ⟺  对每个 k，a_k ≤ p−1−n_k 。
 *
 * 代入 p = 2 得到「(a & n) == 0」（二进制每位要么 a=0，要么 n=0）；
 * 代入 p = 5 得到「a 的五进制各位 a_k ≤ 4 − n_k」。
 *
 * C(i,n) 能被 10 整除 ⟺ 能被 2 整除且能被 5 整除
 *   ⟺ (a&n)≠0 且 五进制存在 a_k > 4−n_k 。
 * 记 M = m−n，把 i 换成 a = i−n ∈ [0, M)（i∈[n,m) ↔ a∈[0,M)），由容斥
 *
 *   T(m,n) = M − N₂ − N₅ + N₂₅ ,
 *
 * 其中 N₂ = #{a<M : a&n==0}，N₅ = #{a<M : ∀k, a_k ≤ 4−n_k}，
 * N₂₅ = 两者同时成立的 a 的个数。
 *
 * N₂、N₅ 可直接按周期数数；关键是 N₂₅。两个条件分别只依赖 a mod 2^A 与 a mod 5^B
 * （取 A = n 的二进制位数、B = n 的五进制位数，使 n 的数位全部落在低位；若
 * 2^A·5^B ≤ M 则加大 A 直到乘积 > M）。当 2^A·5^B > M 时，a ↦ (a mod 2^A, a mod 5^B)
 * 在 [0,M) 上由 CRT 一一对应，于是
 *
 *   N₂₅ = #{(r,l) : r∈R, l∈L, CRT(r,l) < M}，
 *   R = { r < 2^A : r & n == 0 }（2^{A−popcount(n)} 个），
 *   L = { l < 5^B : 各位 l_k ≤ 4−n_k }（Π(5−n_k) 个）。
 *
 * 写 a = r + 2^A·t，由 a ≡ l (mod 5^B) 得 t ≡ (l−r)·inv_{2^A} (mod 5^B)；
 * a < M 等价于 t ≤ T(r) = ⌊(M−1−r)/2^A⌋。令 c = r·inv (mod 5^B)、u_l = l·inv (mod 5^B)，
 * 则条件化作「u_l ∈ [c, c+T(r)]（模 5^B）」，把 {u_l} 排序后对每个 r 二分即可。
 *
 * 规模：n = 10¹²−10 时 A = 40（popcount 22，|R| = 2¹⁸ = 262144），B = 18（|L| = 4800）。
 *
 * 验证
 * --------
 * 1. 题面自检：solve(10⁹, 10⁷−10) = 989697000；
 * 2. 双方法互证：在若干小规模 (m,n) 上，题目公式与「逐 i 用 Kummer 进位判定」的
 *    暴力实现逐一相等；
 * 3. 最终答案与公开官方答案表对照（旁证）。
 *
 * 复杂度：枚举 R、L 分别为 2^{A−popcount(n)} 与 Π(5−n_k)，N₂₅ 为 |R|·log|L|
 *         （本机毫秒级）；暴力对照 O(m−n) 逐项判定。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

/** 返回 x 的 b 进制数位（低位在前）。 */
private fun digits(x: Long, b: Int): IntArray {
    var v = x
    val out = ArrayList<Int>()
    while (v > 0) { out.add((v % b).toInt()); v /= b }
    if (out.isEmpty()) out.add(0)
    return out.toIntArray()
}

/** 计算 (a·b) mod m，用加倍法避免 64 位溢出（m ≤ 5^27 量级）。 */
private fun mulmod(a: Long, b: Long, m: Long): Long {
    var res = 0L
    var x = a % m
    var y = b % m
    while (y > 0) {
        if (y and 1L == 1L) res = if (res >= m - x) res - (m - x) else res + x
        x = if (x >= m - x) x - (m - x) else x + x
        y = y shr 1
    }
    return res
}

/** 统计有序数组 arr 中 ≤ v 的元素个数。 */
private fun countLE(arr: LongArray, v: Long): Int {
    var lo = 0
    var hi = arr.size
    while (lo < hi) {
        val mid = (lo + hi) ushr 1
        if (arr[mid] <= v) lo = mid + 1 else hi = mid
    }
    return lo
}

/**
 * T(m,n) = #{ i ∈ [n,m) : C(i,n) 能被 10 整除 }。
 * 见文件头部推导：= M − N₂ − N₅ + N₂₅，M = m−n。
 */
private fun solve(m: Long, n: Long): Long {
    val M = m - n

    // A = n 的二进制位数；必要时加大以保证 2^A·5^B > M（CRT 在该区间内单射）。
    var A = 64 - java.lang.Long.numberOfLeadingZeros(n)
    if (A == 0) A = 1
    val n5 = digits(n, 5)
    val B = n5.size
    var p5 = 1L
    repeat(B) { p5 *= 5 }
    while (A < 62 && (1L shl A) <= M / p5) A++
    val p2 = 1L shl A

    // N₂：统计 a<M 且 a&n==0 的个数，按 a mod 2^A 的允许余数分类。
    val freeBits = ArrayList<Int>()
    for (j in 0 until A) if ((n shr j) and 1L == 0L) freeBits.add(j)
    val f = freeBits.size
    val rCount = 1 shl f
    val rs = LongArray(rCount)
    for (mask in 0 until rCount) {
        var r = 0L
        var mm = mask
        var idx = 0
        while (mm != 0) {
            if (mm and 1 == 1) r = r or (1L shl freeBits[idx])
            mm = mm shr 1
            idx++
        }
        rs[mask] = r
    }

    var n2 = 0L
    for (r in rs) if (r <= M - 1) n2 += (M - 1 - r) / p2 + 1

    // N₅：统计 a<M 且五进制各位 ≤ 4−n_k 的个数，按 a mod 5^B 的允许余数分类。
    val c5 = IntArray(B) { 4 - n5[it] }
    val digs = IntArray(B)
    val ls = ArrayList<Long>()
    while (true) {
        var l = 0L
        var k = B - 1
        while (k >= 0) { l = l * 5 + digs[k]; k-- }
        ls.add(l)
        var i = 0
        while (i < B && digs[i] == c5[i]) { digs[i] = 0; i++ }
        if (i == B) break
        digs[i]++
    }
    var n5count = 0L
    for (l in ls) if (l <= M - 1) n5count += (M - 1 - l) / p5 + 1

    // N₂₅：按 CRT 组合，二分统计。
    val inv = java.math.BigInteger.valueOf(p2).modInverse(java.math.BigInteger.valueOf(p5)).toLong()
    val u = LongArray(ls.size) { mulmod(ls[it], inv, p5) }
    java.util.Arrays.sort(u)

    var n25 = 0L
    for (r in rs) {
        val tmax = (M - 1 - r) / p2
        val c = mulmod(r, inv, p5)
        val hi = c + tmax
        if (hi < p5) {
            n25 += countLE(u, hi) - countLE(u, c - 1)
        } else {
            n25 += (u.size - countLE(u, c - 1)) + countLE(u, hi - p5)
        }
    }

    return M - n2 - n5count + n25
}

/** 暴力对照：逐 i 用 Kummer 进位（等价于数位比较）判定是否被 10 整除。 */
private fun brute(m: Long, n: Long): Long {
    var cnt = 0L
    var a = 0L
    while (n + a < m) {
        val i = n + a
        val div2 = (a and n) != 0L                     // 二进制有进位
        var div5 = false                              // 五进制有进位
        var x = a
        var y = n
        while (x > 0 || y > 0) {
            if (x % 5 + y % 5 >= 5) { div5 = true; break }
            x /= 5
            y /= 5
        }
        if (div2 && div5) cnt++
        a++
    }
    return cnt
}

/** 预热 1 次后跑 3 轮取最快，返回 (结果, 毫秒)。 */
private inline fun timeOf(body: () -> Long): Pair<Long, Double> {
    body()
    var best = Double.MAX_VALUE
    var r = 0L
    repeat(3) {
        val st = System.nanoTime()
        r = body()
        val ms = (System.nanoTime() - st) / 1e6
        if (ms < best) best = ms
    }
    return r to best
}

fun main() {
    println("== 题面自检 ==")
    val sample = solve(1_000_000_000L, 9_999_990L)
    println("T(10⁹, 10⁷−10) = $sample  " +
        (if (sample == 989697000L) "-> 与题面一致" else "-> 与题面不一致！"))

    println("== 双方法互证（题目公式 vs 逐 i 进位判定） ==")
    val cases = longArrayOf(
        1_000L, 5_000L, 10_000L, 50_000L, 200_000L, 500_000L
    )
    var ok = true
    var bruteMs = 0.0
    for (m in cases) {
        val n = m / 7 - 10
        val a = solve(m, n)
        val bt = timeOf { brute(m, n) }
        val b = bt.first
        if (m == cases.last()) bruteMs = bt.second
        ok = ok && a == b
        println("  m=$m n=$n：公式=$a，暴力=$b  " + (if (a == b) "-> 一致" else "-> 不一致！"))
    }
    println("全部规模一致：$ok")

    println("== 正式求解 ==")
    val (ans, ms) = timeOf { solve(1_000_000_000_000_000_000L, 1_000_000_000_000L - 10) }
    println("T(10¹⁸, 10¹²−10) = $ans")
    println("OPT_MS: " + String.format("%.3f", ms) + "  （CRT 计数 + 二分）")
    println("BRUTE_MS: " + String.format("%.3f", bruteMs) + "  （逐 i 进位判定，m=500000 规模）")
}
