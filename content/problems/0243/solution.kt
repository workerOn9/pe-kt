#!/usr/bin/env kotlin
/**
 * Project Euler 243 — Resilience（韧性）
 *
 * 思路：
 *   分母 d 的 d−1 个真分数里，能约分的恰好是「与 d 不互质的那些」——即 n/d 里 gcd(n,d) > 1 的 n，
 *   共 d − φ(d) 个（把 n = d 单独去掉）。所以韧性就是
 *
 *       R(d) = φ(d) / (d − 1)
 *
 *   目标：最小的 d 使 R(d) < A/B（本题 A = 15499, B = 94744）。交叉相乘，全程整数：
 *
 *       B·φ(d) < A·(d − 1)
 *
 *   关键结构：φ(d)/d = ∏_{p | d}(1 − 1/p) 只依赖 d 的**素因子集合**，且对小素数单调递减。
 *   记素数阶乘 P_k = p_1·p_2·…·p_k，则 φ(P_k) = ∏(p_i − 1)，φ(P_k)/P_k 是「恰好 k 个不同素因子」
 *   时能取到的最小比值。于是：
 *
 *   1. 取**最小**的 k 使 B·φ(P_k) < A·P_k（阶乘本身的渐近比值已低于阈值）。任何解至少要 k 个
 *      不同素因子，故 d ≥ P_k。本题 k = 9，基底就是 P_9 = 223092870（φ(P_9)/P_9 = 0.16358819535559
 *      与阈值 0.16358819555855 只差 2.0×10⁻¹⁰，而 φ(P_8)/P_8 = 0.17102 远在阈值之上）。
 *   2. 在 P_k 上试倍数 m = 1, 2, 3, …：只要 m 的素因子都已在 P_k 里，就有 φ(m·P_k) = m·φ(P_k)，
 *      代入条件得
 *
 *          B·m·φ(P_k) < A·(m·P_k − 1)   ⟺   m·(A·P_k − B·φ(P_k)) > A
 *
 *      Δ = A·P_k − B·φ(P_k) 是与 m 无关的正整数，于是 m 的下界是 A/Δ + 1（本题 15499/4290 ≈ 3.61），
 *      第一个整数下界 4 已经只用素因子 2（4 = 2² ∈ P_9），直接可用。答案 = 4 × 223092870。
 *   3. 枚举完整性（下面 main 里逐条打印为证）：
 *      (a) k−1 个素因子的最小比值 ≥ A/B —— 否则解可能落在别的素因子集合上；
 *      (b) 除 {p_1..p_k} 外，第 2 小的 k 元素因子集合的最小比值也 ≥ A/B（本题成立，因为阈值恰好
 *          卡在 φ(P_9)/P_9 与 φ(P_8)/P_8 之间，缝隙只有 2.0×10⁻¹⁰）；
 *      (c) P_{k+1} > 答案 —— k+1 个不同素因子的最小数已经更大，故只需考虑基底 P_k 的倍数。
 *      三条都成立时，上面的枚举覆盖了所有 d < 答案。
 *
 * 旁证：
 *   1. 题面样例：把阈值换成 4/10，同一份代码给出最小 d = 12，与题面一致（k = 2, P_2 = 6, Δ = 4,
 *      m·4 > 4 → m = 2 → d = 12）；另外 1/2 → 6、1/3 → 30、1/4 → 210、2/5 → 12、1/5 → 30030
 *      五个交叉点，两份代码逐位相同。
 *   2. brute-force.kt 用线性/分段欧拉函数筛把 d = 2, 3, 4, … 一个个扫过去，不含任何素因子结构
 *      （含 4/10 → 12 的题面样例），在真实阈值上扫到 892371480 为止，第一个满足不等式的正是它。
 *
 * 答案：892371480
 * 复杂度：素数筛到 1000 + 阶乘递推 k 步 + 倍数 m 试除，O(π(1000))，实测 < 1 μs。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

/** 素数表（筛到 1000 足够：本题只用前 10 个素数，P_10 = 6.5×10^9，交叉相乘后 ~10^14，仍在 Long 内） */
private val PRIMES: List<Long> = buildList {
    val composite = BooleanArray(1001)
    for (i in 2..1000) {
        if (!composite[i]) {
            add(i.toLong())
            var j = i * i
            while (j <= 1000) { composite[j] = true; j += i }
        }
    }
}

/** 第 k 个素数阶乘 P_k 与 φ(P_k)（k = 0 时 P = 1, φ = 1） */
private fun primorial(k: Int): Pair<Long, Long> {
    var p = 1L
    var ph = 1L
    for (i in 0 until k) { p *= PRIMES[i]; ph *= PRIMES[i] - 1 }
    return p to ph
}

/** m 的全部素因子是否都在前 k 个素数之内（即 m 是 k-光滑数） */
private fun isKSmooth(m: Long, k: Int): Boolean {
    var v = m
    for (i in 0 until k) {
        val p = PRIMES[i]
        while (v % p == 0L) v /= p
    }
    return v == 1L
}

/** 最小的 k，使素数阶乘 P_k 的渐近比值 φ(P_k)/P_k 已低于 A/B */
private fun minimalK(a: Long, b: Long): Int {
    var k = 1
    while (true) {
        val (p, ph) = primorial(k)
        if (b * ph < a * p) return k
        k++
    }
}

/** 最小的 d 使 φ(d)/(d−1) < A/B */
private fun minResilientDenominator(a: Long, b: Long): Long {
    val k = minimalK(a, b)
    val (base, phiBase) = primorial(k)
    val delta = a * base - b * phiBase              // > 0，条件 m·Δ > A
    var m = a / delta + 1
    while (m * delta <= a || !isKSmooth(m, k)) m++
    return m * base
}

/** 第 2 小的 k 元素因子集合 {p_1,..,p_{k−1}, p_{k+1}} 的比值 φ/自身，与 A/B 比较 */
private fun secondBestRatioAboveThreshold(a: Long, b: Long, k: Int): Boolean {
    val (base, phiBase) = primorial(k - 1)
    val p = PRIMES[k]                               // p_{k+1}
    return b * (phiBase * (p - 1)) >= a * (base * p)
}

private fun main() {
    // ---- 题面样例与交叉验证点（阈值 A/B → 最小的 d） ----
    val sample = minResilientDenominator(4, 10)
    println("R(d) <  4/10 -> d = $sample   (题面：12)")
    check(sample == 12L) { "题面样例不吻合：$sample" }

    for ((a, b, expected) in listOf(
        Triple(1L, 2L, 6L), Triple(1L, 3L, 30L), Triple(1L, 4L, 210L),
        Triple(2L, 5L, 12L), Triple(1L, 5L, 30030L),
    )) {
        val got = minResilientDenominator(a, b)
        println("R(d) < $a/$b -> d = $got")
        check(got == expected) { "阈值 $a/$b 不吻合：$got != $expected" }
    }

    // ---- 本题 ----
    val a = 15499L
    val b = 94744L
    val k = minimalK(a, b)
    val (base, phiBase) = primorial(k)
    val (prevBase, prevPhi) = primorial(k - 1)
    val delta = a * base - b * phiBase

    println()
    println("阈值      A/B = $a/$b = ${a.toDouble() / b}")
    println("φ(P_$k)/P_$k   = ${phiBase.toDouble() / base}   (P_$k = $base)")
    println("φ(P_${k - 1})/P_${k - 1} = ${prevPhi.toDouble() / prevBase}   (P_${k - 1} = $prevBase)")
    println("最小 k    = $k   → 任何解至少有 $k 个不同素因子，故 d ≥ P_$k = $base")

    // 枚举完整性三条
    check(b * prevPhi >= a * prevBase) { "k 取小了：k-1 个素因子的最小比值已经低于阈值" }
    println("(a) k−1 个素因子的最小比值 ≥ A/B            成立")
    check(secondBestRatioAboveThreshold(a, b, k)) { "存在更优的 k 元素因子集合，需要更宽的枚举" }
    println("(b) 第 2 小的 k 元集合 {p_1..p_${k - 1}, p_${k + 1}} 比值 ≥ A/B   成立")
    val (nextBase, _) = primorial(k + 1)
    println("(c) P_${k + 1} = $nextBase > 答案（k+1 个素因子的最小数已超出）")

    val answer = minResilientDenominator(a, b)
    val m = answer / base
    println("Δ = A·P_$k − B·φ(P_$k) = $delta   →  m·Δ > $a  →  m ≥ ${a / delta + 1}")
    println("answer = m·P_$k = $m × $base = $answer")
    check(answer == 892371480L) { "答案不吻合：$answer" }

    // 直接复核：φ(answer) 与不等式本身
    val phiAnswer = m * phiBase                                  // φ(m·P_k) = m·φ(P_k)
    println("φ($answer) = $phiAnswer, B·φ = ${b * phiAnswer} < A·(d−1) = ${a * (answer - 1)}")
    check(b * phiAnswer < a * (answer - 1)) { "答案不满足不等式" }
    check(nextBase > answer) { "P_${k + 1} 不大于答案，枚举不完整" }

    // ---- 计时：JIT 预热后单次求解的平均耗时 ----
    repeat(50_000) { minResilientDenominator(a, b) }
    val iterations = 200_000
    val t0 = System.nanoTime()
    var sink = 0L
    for (i in 0 until iterations) sink += minResilientDenominator(a, b)
    val elapsed = System.nanoTime() - t0
    check(sink == answer * iterations) { "计时循环结果漂移" }
    val perCallMs = elapsed.toDouble() / iterations / 1e6
    println("optimized: ${"%.8f".format(perCallMs)} ms/次（$iterations 次，JIT 预热 50000 次）")
    println("check() 全部通过")
}
