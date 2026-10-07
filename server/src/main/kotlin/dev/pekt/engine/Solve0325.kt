package dev.pekt.engine

import java.math.BigInteger

/**
 * PE 325 — Stone Game II（取石游戏 II）：两堆 (x,y)，0 < x < y，每步从大堆取走小堆的正整数倍个，
 * 取光任一堆者胜。S(N) = 所有必败局面 (x,y)（y ≤ N）的 (x+y) 之和；求 S(10¹⁶) mod 7¹⁰。
 *
 * 推导（详见 content/problems/0325/solution.kt 头部与 0325/analysis.md）：
 *
 *   1. 胜负刻画：y ≥ 2x 必胜（可直接走到必败态）；x < y < 2x 时唯一走法到 (y-x, x)。
 *      由 1/φ = φ-1（φ 黄金比）归纳得封闭刻画：(x,y) 必败 ⟺ x < y < φ·x。
 *
 *   2. 求和化归：固定 y，令 a=⌊y/φ⌋，必败的 x 为 a+1..y-1，故
 *        S(N) = A(N) - B(N),
 *        A(N) = (N-1)N(N+1)/6 + N(N+1)(2N+1)/6 - N(N+1)/2,
 *        B(N) = (Q(N)+P(N))/2 + G(N),
 *      其中 P(n)=Σ⌊k/φ⌋、G(n)=Σk⌊k/φ⌋、Q(n)=Σ⌊k/φ⌋²。
 *
 *   3. 对偶递推（用 1/φ = φ-1，令 M=⌊n/φ⌋）：
 *        P(n) = M·n - M(M+1)/2 - P(M),
 *        Q(n) = n·M² - [M(M+1)(2M+1)/3 - M(M+1)/2] - 2G(M) + P(M),
 *        G(n) = M·T(n) - M(M+1)(M+2)/6 - G(M) - (Q(M)+P(M))/2.
 *      因 M≈0.618n，深度 O(log_φ N)≈76，逐层记忆化 + 模 7¹⁰ 运算（2,3,6 均可逆）。
 *
 * 自证：minimax 博弈 DP 与「y<φx」刻画在 y≤1500 上逐局面一致；题面样例 S(10)=211、
 *       S(10⁴)=230312207313 均吻合；递推与 O(N) 逐 y 扫和在多个规模上一致。
 *
 * 最终答案：S(10¹⁶) mod 7¹⁰ = 54672965；本机实测（JIT 预热后 3.5 轮最快）约 0.6 ms。
 */
internal fun solve0325Impl(): Long {
    val mod = 282475249L // 7^10
    val inv2 = BigInteger.valueOf(2).modInverse(BigInteger.valueOf(mod)).toLong()
    val inv3 = BigInteger.valueOf(3).modInverse(BigInteger.valueOf(mod)).toLong()
    val inv6 = BigInteger.valueOf(6).modInverse(BigInteger.valueOf(mod)).toLong()

    fun norm(x: Long): Long {
        val r = x % mod
        return if (r < 0) r + mod else r
    }

    // ⌊n/φ⌋ = ⌊(n√5 − n)/2⌋，n ≤ 10¹⁶，用精确整数开方
    fun floorPhi(n: Long): Long {
        if (n <= 0L) return 0L
        val nb = BigInteger.valueOf(n)
        val s = nb.multiply(nb).multiply(BigInteger.valueOf(5)).sqrt()
        return s.subtract(nb).divide(BigInteger.valueOf(2)).toLong()
    }

    val memo = HashMap<Long, LongArray>()

    // 返回 (P(n), G(n), Q(n)) mod 7¹⁰
    fun pgq(n: Long): LongArray {
        if (n <= 0L) return longArrayOf(0L, 0L, 0L)
        memo[n]?.let { return it }

        val m = floorPhi(n)
        val sub = pgq(m)
        val p = sub[0]
        val g = sub[1]
        val q = sub[2]

        val nm = n % mod
        val mm = m % mod
        val n1 = (n + 1) % mod
        val m1 = (m + 1) % mod
        val m2 = (m + 2) % mod
        val m2p1 = (2 * m + 1) % mod

        val tn = nm * n1 % mod * inv2 % mod
        val t2m = mm * m1 % mod * m2 % mod * inv6 % mod

        val pn = norm(mm * nm % mod - mm * m1 % mod * inv2 % mod - p)

        val cq = norm(
            nm * mm % mod * mm % mod
                - (mm * m1 % mod * m2p1 % mod * inv3 % mod - mm * m1 % mod * inv2 % mod)
        )
        val qn = norm(cq - 2 * g + p)

        val r = (q + p) % mod * inv2 % mod
        val gn = norm(mm * tn % mod - t2m - g - r)

        val res = longArrayOf(pn, gn, qn)
        memo[n] = res
        return res
    }

    val n = 10_000_000_000_000_000L
    val v = pgq(n)
    val p = v[0]
    val g = v[1]
    val q = v[2]

    val nm = n % mod
    val n1 = (n + 1) % mod
    val nminus1 = (n - 1) % mod
    val term1 = nminus1 * nm % mod * n1 % mod * inv6 % mod
    val term2 = nm * n1 % mod * ((2 * n + 1) % mod) % mod * inv6 % mod
    val term3 = nm * n1 % mod * inv2 % mod
    val a = norm(term1 + term2 - term3)
    val b = norm((q + p) % mod * inv2 % mod + g)
    return norm(a - b)
}
