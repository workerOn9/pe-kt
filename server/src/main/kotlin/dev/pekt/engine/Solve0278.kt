package dev.pekt.engine

import dev.pekt.math.primesUpTo

/**
 * PE 278 — Linear Combinations of Semiprimes（半素数的线性组合）：求 Σ f(pq, pr, qr)，
 * p < q < r < 5000 均为素数，其中 f 是非负整数系数线性组合不能表示的最大整数（Frobenius 数）。
 *
 * 推导（完整证明见 content/problems/0278/solution.kt 头部与 0278/analysis.md）：
 *   对两两互素的 a,b,c ≥ 2 有 g(ab, ac, bc) = 2abc − ab − ac − bc。
 *   证明要点：把 N 按 bc·z₀ + a·M 参数化（z₀ = N·(bc)⁻¹ mod a 是唯一可能的首位），
 *   可表示 ⟺ M ≥ w(M mod bc)（w 为 ⟨b,c⟩ 的 Apéry 元素）；ρ 为间隙时 w(ρ) = ρ + bc
 *   （ρ + bc > g(b,c) = bc − b − c 必可表示）；于是不可表示的最大值 = bc(a−1) + a·g(b,c)。
 *
 * 求和：记素数为 p_1<…<p_n（n = π(5000) = 669），e₂ = Σ_{i<j} p_i p_j，e₃ = Σ_{i<j<k} p_i p_j p_k，
 *   则 Σ f = 2e₃ − (n−2)e₂（每个无序对出现在 n−2 个三元组中）。用 Newton 恒等式
 *   e₂ = (s₁²−s₂)/2、e₃ = (s₁³−3s₁s₂+2s₃)/6（s_k = Σ pᵏ）求解，中间量 s₁³ ≈ 3.71×10^18 < 2^63。
 *
 * 复杂度：筛 O(N log log N) + O(n) 求和，实测 < 1 ms。
 * 校验：题面锚点 f(5,7)=23、f(6,10,15)=29、f(14,22,77)=195 由 DP 直算复现；公式对所有素数三元组
 *   r ≤ 31（165 个）与所有两两互素三元组 ≤ 25（619 个）与 DP 对拍一致；滚动前缀和求和路径与本法
 *   一致；brute-force.kt 用朴素三重循环（49679494 个三元组）得到同一结果。逻辑与 solution.kt 主路径一致。
 */
internal fun solve0278Impl(): Long {
    val primes = primesUpTo(4999)          // π(4999) = 669
    val n = primes.size

    val s1 = primes.sum()
    val s2 = primes.sumOf { it * it }
    val s3 = primes.sumOf { it * it * it }
    val e2 = (s1 * s1 - s2) / 2
    val e3 = (s1 * s1 * s1 - 3 * s1 * s2 + 2 * s3) / 6

    return 2 * e3 - (n - 2) * e2
}
