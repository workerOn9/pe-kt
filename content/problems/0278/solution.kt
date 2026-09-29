#!/usr/bin/env kotlin
/**
 * Project Euler 278 — Linear Combinations of Semiprimes（半素数的线性组合）
 *
 * 思路
 * ────
 * 记 g(a_1,…,a_n) 为「不能用非负整数系数表示的最大整数」（Frobenius 数），题目要的是
 *
 *   S = Σ_{p<q<r<5000} g(pq, pr, qr),   p,q,r 为素数。
 *
 * **关键公式：对两两互素的 a,b,c ≥ 2，** g(ab, ac, bc) = 2abc − ab − ac − bc。
 *
 * 证明（本题的完整推导）。写 N = ab·x + ac·y + bc·z（x,y,z ≥ 0）。令 z₀ ∈ [0,a) 是
 * z ≡ N·(bc)⁻¹ (mod a) 的规范余数（必要：N − bc·z ≡ 0 mod a），则 z = z₀ + a·t（t ≥ 0），代入得
 *
 *   N = bc·z₀ + a·M,   M := bx + cy + bc·t  ⇒  M ≡ bx + cy (mod bc), M ≥ 0。
 *
 * 于是（记 w(ρ) = ⟨b,c⟩ 中 ≡ ρ (mod bc) 的最小元素，即 Apéry 元素）
 *
 *   N 可表示 ⟺ M ≥ w(M mod bc)。
 *
 * 对 ρ 为 ⟨b,c⟩ 的**间隙**（不可表示）时 w(ρ) = ρ + bc：因为 ρ + bc > g(b,c) = bc − b − c
 * （两数情形的经典结论）必可表示，而 ρ 本身不可表示、且 ρ 是 < ρ + bc 的同类唯一候选。
 * 于是**不可表示**的 N 恰有参数 (z₀, M) 满足 M ≡ ρ (mod bc) 且 ρ 是间隙、M < ρ + bc ⟹ M = ρ：
 *
 *   N = bc·z₀ + a·ρ,   z₀ ∈ [0,a), ρ 为 ⟨b,c⟩ 的间隙
 *     ⇒ N ≤ bc(a−1) + a·g(b,c) = bc(a−1) + a(bc − b − c) = 2abc − ab − ac − bc。
 *
 * 取 z₀ = a−1、ρ = g(b,c) = bc − b − c 达到上界，且该 N 不可表示；所有更大的整数都可表示，
 * 故上界即 Frobenius 数（另有一类 N 不在上述参数化像里：M = (N − bc·z₀)/a < 0，此时
 * N < bc·z₀ ≤ bc(a−1) < 2abc − ab − ac − bc，同样不超过上界）。∎
 *
 * 求和：记素数表 p_1 < … < p_n（n = π(5000) = 669），用初等对称多项式
 *
 *   e₂ = Σ_{i<j} p_i p_j,  e₃ = Σ_{i<j<k} p_i p_j p_k,
 *   S = Σ_{i<j<k} (2p_ip_jp_k − p_ip_j − p_ip_j − p_jp_k) = 2e₃ − (n−2)·e₂,
 *
 * 第二项用了「每一对 {p_i,p_j} 出现在 n−2 个三元组里，且每个三元组的三个两两乘积项各出现一次」。
 *
 * 复杂度
 * ──────
 * 筛 5000 内素数 O(5000 log log 5000)；求和 O(n)。1 次朴素三元组循环需 ~5×10^7 次（C(669,3)
 * ≈ 4.97×10^7），只作对照。数值上 e₃ ≈ 6.15×10^17、S ≈ 1.23×10^18，全在 Long 内
 * （中间量 s₁³ ≈ 3.71×10^18 < 9.22×10^18）。
 *
 * 验证
 * ────
 * 1. 题面锚点：g(5,7) = 23、g(6,10,15) = 29、g(14,22,77) = 195 全部由 DP 直接算 Frobenius 数复现；
 * 2. 公式与小规模 DP 对拍：所有素数三元组 p<q<r ≤ 31（165 个）以及所有两两互素的三元组
 *    2 ≤ a<b<c ≤ 25（619 个，不限于素数）都与公式一致；
 * 3. 两条独立求和路径：Newton 恒等式（e₂ = (s₁²−s₂)/2, e₃ = (s₁³−3s₁s₂+2s₃)/6）与
 *    滚动前缀和（e₂、e₃ 逐项累加）在完整规模上一致；
 * 4. brute-force.kt 用朴素三元组循环直接累加 2pqr − pq − pr − qr（4.97×10^7 个三元组）对照。
 *
 * 答案
 * ────
 * 1228215747273908452（本机实跑，两条路径一致）
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0278/solution.kt -d /tmp/kc-0278
 * java -cp /tmp/kc-0278:<kotlin-stdlib> SolutionKt
 */

private const val PRIME_LIMIT = 5000

/** 素筛（独立实现，不使用仓库工具库）。 */
private fun sieve(limit: Int): BooleanArray {
    val isPrime = BooleanArray(limit + 1) { it >= 2 }
    var p = 2
    while (p.toLong() * p <= limit) {
        if (isPrime[p]) {
            var k = p * p
            while (k <= limit) {
                isPrime[k] = false
                k += p
            }
        }
        p++
    }
    return isPrime
}

/**
 * 用 DP 直接算 Frobenius 数（只在 [0, limit] 上前推可达性）。
 * 要求调用者保证 limit 足够大：函数末尾会检查「结尾处存在 min(nums) 个连续可达位置」，
 * 一旦成立则之后不可能再有不可表示的数（加上最小生成元即可平移）。
 */
private fun frobeniusByDp(nums: IntArray, limit: Int): Long {
    val reach = BooleanArray(limit + 1)
    reach[0] = true
    for (i in 0..limit) {
        if (!reach[i]) continue
        for (a in nums) if (i + a <= limit) reach[i + a] = true
    }
    var last = -1L
    for (i in 0..limit) if (!reach[i]) last = i.toLong()
    val minGen = nums.min()
    var tail = 0
    var i = limit
    while (i >= 0 && reach[i] && tail < minGen) { tail++; i-- }
    check(tail >= minGen) { "DP 上界 $limit 不够大：结尾没有 $minGen 个连续可达位置" }
    return last
}

/** 公式：两两互素的 a,b,c 应有 g(ab,ac,bc) = 2abc − ab − ac − bc。 */
private fun formula(a: Long, b: Long, c: Long): Long = 2 * a * b * c - (a * b + a * c + b * c)

/** 方法 A（主路径）：Newton 恒等式求 e₂、e₃。 */
private fun solveByNewton(primes: LongArray): Long {
    val n = primes.size
    val s1 = primes.sum()
    val s2 = primes.sumOf { it * it }
    val s3 = primes.sumOf { it * it * it }
    val e2 = (s1 * s1 - s2) / 2
    val e3 = (s1 * s1 * s1 - 3 * s1 * s2 + 2 * s3) / 6
    return 2 * e3 - (n - 2) * e2
}

/** 方法 B（独立复核）：滚动前缀和直接累加 e₂、e₃。 */
private fun solveByRunningSums(primes: LongArray): Long {
    var prefixSum = 0L            // Σ_{i<k} p_i
    var prefixPairSum = 0L        // Σ_{i<j<k} p_i p_j
    var e2 = 0L
    var e3 = 0L
    for (p in primes) {
        e2 += p * prefixSum
        e3 += p * prefixPairSum
        prefixPairSum += p * prefixSum
        prefixSum += p
    }
    val n = primes.size
    return 2 * e3 - (n - 2) * e2
}

/** 筛出 limit 内的素数。 */
private fun primesBelow(limit: Int): LongArray {
    val isPrime = sieve(limit)
    return (2 until limit).filter { isPrime[it] }.map { it.toLong() }.toLongArray()
}

/** 主路径 A（含筛）。 */
private fun solveFullA(): Long = solveByNewton(primesBelow(PRIME_LIMIT))

/** 复核路径 B（含筛）。 */
private fun solveFullB(): Long = solveByRunningSums(primesBelow(PRIME_LIMIT))

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.3f".format(ms)} ms")
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 1. 题面锚点（DP 直算 Frobenius 数） ----------
    val anchor1 = frobeniusByDp(intArrayOf(5, 7), 210)
    val anchor2 = frobeniusByDp(intArrayOf(6, 10, 15), 900)
    val anchor3 = frobeniusByDp(intArrayOf(14, 22, 77), 10164)
    check(anchor1 == 23L) { "g(5,7) 应为 23，实际 $anchor1" }
    check(anchor2 == 29L) { "g(6,10,15) 应为 29，实际 $anchor2" }
    check(anchor3 == 195L) { "g(14,22,77) 应为 195，实际 $anchor3" }
    println("题面锚点（DP 直算）：g(5,7) = $anchor1；g(6,10,15) = $anchor2；g(14,22,77) = $anchor3")
    check(formula(2, 3, 5) == anchor2) { "公式与 f(6,10,15) 不符" }
    check(formula(2, 7, 11) == anchor3) { "公式与 f(14,22,77) 不符" }

    // ---------- 2. 公式与小规模 DP 对拍 ----------
    val isPrime = sieve(PRIME_LIMIT)
    val primes = (2 until PRIME_LIMIT).filter { isPrime[it] }.map { it.toLong() }.toLongArray()

    var triplesSmall = 0
    for (i in 0 until primes.size) {
        if (primes[i] > 31) break
        for (j in i + 1 until primes.size) {
            if (primes[j] > 31) break
            for (k in j + 1 until primes.size) {
                if (primes[k] > 31) break
                val p = primes[i]; val q = primes[j]; val r = primes[k]
                val dp = frobeniusByDp(
                    intArrayOf((p * q).toInt(), (p * r).toInt(), (q * r).toInt()),
                    (6 * p * q * r).toInt(),
                )
                check(dp == formula(p, q, r)) { "素数三元组 ($p,$q,$r)：DP $dp ≠ 公式 ${formula(p, q, r)}" }
                triplesSmall++
            }
        }
    }
    var coprimeSmall = 0
    for (a in 2L..25L) for (b in a + 1..25L) for (c in b + 1..25L) {
        if (gcd(a, b) != 1L || gcd(a, c) != 1L || gcd(b, c) != 1L) continue
        val dp = frobeniusByDp(
            intArrayOf((a * b).toInt(), (a * c).toInt(), (b * c).toInt()),
            (6 * a * b * c).toInt(),
        )
        check(dp == formula(a, b, c)) { "互素三元组 ($a,$b,$c)：DP $dp ≠ 公式 ${formula(a, b, c)}" }
        coprimeSmall++
    }
    println("公式对拍：所有素数三元组 r ≤ 31（$triplesSmall 个）与所有两两互素三元组 ≤ 25" +
        "（$coprimeSmall 个，不限于素数）都与 DP 直算一致")

    // ---------- 3. 完整规模：两条求和路径 ----------
    val n = primes.size
    check(n == 669) { "π(5000) 应为 669，实际 $n" }
    val answerA = solveByNewton(primes)
    val answerB = solveByRunningSums(primes)
    check(answerA == answerB) { "方法 A $answerA ≠ 方法 B $answerB" }
    val tripleCount = n.toLong() * (n - 1) * (n - 2) / 6
    println("完整规模：素数个数 $n，三元组数 C($n,3) = $tripleCount；" +
        "方法 A（Newton）与方法 B（滚动前缀和）一致 = $answerA")

    // ---------- 4. 计时 ----------
    solveFullA(); solveFullB()
    val msA = bestOf3("方法 A：Newton 恒等式（含筛）", answerA) { solveFullA() }
    val msB = bestOf3("方法 B：滚动前缀和（含筛）", answerB) { solveFullB() }
    val msSieve = bestOf3("对照：筛 5000 内素数", 669L) { sieve(PRIME_LIMIT).count { it }.toLong() }
    val msTripleLoop = bestOf3("对照：朴素三元组循环（公式求和，见 brute-force.kt）", answerA) {
        var s = 0L
        for (i in 0 until n) for (j in i + 1 until n) for (k in j + 1 until n) {
            s += 2 * primes[i] * primes[j] * primes[k] -
                (primes[i] * primes[j] + primes[i] * primes[k] + primes[j] * primes[k])
        }
        s
    }

    println()
    println("答案 = $answerA")
    println("汇总：方法 A ${"%.3f".format(msA)} ms；方法 B ${"%.3f".format(msB)} ms；" +
        "筛选 ${"%.3f".format(msSieve)} ms；朴素三元组循环 ${"%.1f".format(msTripleLoop)} ms（$tripleCount 个三元组）")
    println("check() 全部通过")
}

/** 辗转相除（供互素三元组枚举用）。 */
private fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}
