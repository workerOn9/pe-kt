#!/usr/bin/env kotlin
/**
 * Project Euler 278 — Linear Combinations of Semiprimes：直接暴力对照（独立实现，与 solution.kt 不共享核心代码）
 *
 * 三条路：
 *
 *   路径 1（题面锚点）：用「反向 DP」直接算 Frobenius 数（can[i] = can[i−a] ∨ can[i−b] ∨ can[i−c]，
 *   与 solution.kt 的前推式写法相反），复现 f(5,7) = 23、f(6,10,15) = 29、f(14,22,77) = 195。
 *
 *   路径 2（小规模整段对拍）：对所有素数三元组 p<q<r ≤ 47（455 个），用「每个三元组单独跑 DP 求
 *   真实 Frobenius 数」的原始方式求和，与按公式 2pqr − pq − pr − qr 求和对照。
 *
 *   路径 3（完整规模，brute 口径）：朴素三重循环，对全部 C(669,3) = 49679494 个三元组累加公式值。
 *   这是可行范围内最接近「直接枚举」的求和方式；真正对每个三元组跑 Frobenius DP 无法承受——
 *   r < 5000 时单个三元组的 DP 上界就有 ~10^11 量级，乘 5×10^7 个三元组完全不可行。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 */

private const val PRIME_LIMIT = 5000

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
 * 反向 DP 求 Frobenius 数：can[i] = 「i 可表示」，自左向右用 can[i] = can[i−a] ∨ can[i−b] ∨ can[i−c]
 * 递推（与 solution.kt 的「从 i 往前推」写法不同）。
 * 结束时检查末尾有 min(nums) 个连续可达位置，以保证 limit 足够大、结果就是真正的 Frobenius 数。
 */
private fun frobeniusDpBackward(nums: IntArray, limit: Int): Long {
    val can = BooleanArray(limit + 1)
    can[0] = true
    for (i in 1..limit) {
        for (a in nums) {
            if (i - a >= 0 && can[i - a]) { can[i] = true; break }
        }
    }
    val minGen = nums.min()
    var tail = 0
    var i = limit
    while (i >= 0 && can[i] && tail < minGen) { tail++; i-- }
    check(tail >= minGen) { "limit=$limit 太小，无法确认这不是区间内的假 Frobenius 数" }
    var last = -1L
    for (j in 0..limit) if (!can[j]) last = j.toLong()
    return last
}

/** 公式值（仅用于对照，不参与路径 1/2 的原始计算）。 */
private fun formula(p: Long, q: Long, r: Long): Long = 2 * p * q * r - (p * q + p * r + q * r)

private fun timed(tag: String, rounds: Int, body: () -> Unit): Double {
    body()
    var best = Double.MAX_VALUE
    repeat(rounds) { round ->
        val t0 = System.nanoTime()
        body()
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（$rounds 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 路径 1：题面锚点 ----------
    val f1 = frobeniusDpBackward(intArrayOf(5, 7), 210)
    val f2 = frobeniusDpBackward(intArrayOf(6, 10, 15), 900)
    val f3 = frobeniusDpBackward(intArrayOf(14, 22, 77), 10164)
    check(f1 == 23L) { "f(5,7) 应为 23，实际 $f1" }
    check(f2 == 29L) { "f(6,10,15) 应为 29，实际 $f2" }
    check(f3 == 195L) { "f(14,22,77) 应为 195，实际 $f3" }
    println("路径 1（反向 DP 直算）：f(5,7) = $f1；f(6,10,15) = $f2；f(14,22,77) = $f3")

    // ---------- 路径 2：素数三元组 r ≤ 47，逐个 DP 求和 vs 公式求和 ----------
    val isPrime = sieve(PRIME_LIMIT)
    val primes = (2 until PRIME_LIMIT).filter { isPrime[it] }.map { it.toLong() }.toLongArray()
    var dpSum = 0L
    var fmSum = 0L
    var cnt = 0
    for (i in primes.indices) {
        if (primes[i] > 47) break
        for (j in i + 1 until primes.size) {
            if (primes[j] > 47) break
            for (k in j + 1 until primes.size) {
                if (primes[k] > 47) break
                val p = primes[i]; val q = primes[j]; val r = primes[k]
                dpSum += frobeniusDpBackward(
                    intArrayOf((p * q).toInt(), (p * r).toInt(), (q * r).toInt()),
                    (6 * p * q * r).toInt(),
                )
                fmSum += formula(p, q, r)
                cnt++
            }
        }
    }
    check(dpSum == fmSum) { "路径 2：DP 求和 $dpSum ≠ 公式求和 $fmSum" }
    println("路径 2：素数三元组 r ≤ 47（$cnt 个）逐个 DP 求真实 Frobenius 数，总和 = $dpSum；" +
        "与公式求和一致")

    // ---------- 路径 3：完整规模的朴素三重循环（brute 口径） ----------
    val n = primes.size
    val tripleCount = n.toLong() * (n - 1) * (n - 2) / 6
    var full = 0L
    for (i in 0 until n) for (j in i + 1 until n) for (k in j + 1 until n) {
        full += formula(primes[i], primes[j], primes[k])
    }
    println("路径 3：完整规模朴素三元组循环（$tripleCount 个三元组）得到 $full")

    val msPath2 = timed("路径 2：r ≤ 47 逐个 DP 求和（$cnt 个三元组）", 1) {
        var s = 0L
        for (i in primes.indices) {
            if (primes[i] > 47) break
            for (j in i + 1 until primes.size) {
                if (primes[j] > 47) break
                for (k in j + 1 until primes.size) {
                    if (primes[k] > 47) break
                    val p = primes[i]; val q = primes[j]; val r = primes[k]
                    s += frobeniusDpBackward(
                        intArrayOf((p * q).toInt(), (p * r).toInt(), (q * r).toInt()),
                        (6 * p * q * r).toInt(),
                    )
                }
            }
        }
        check(s == dpSum) { "路径 2 计时轮结果漂移：$s ≠ $dpSum" }
    }
    val msPath3 = timed("路径 3：朴素三元组循环（brute 口径）", 3) {
        var s = 0L
        for (i in 0 until n) for (j in i + 1 until n) for (k in j + 1 until n) {
            s += formula(primes[i], primes[j], primes[k])
        }
        check(s == full) { "路径 3 计时轮结果漂移：$s ≠ $full" }
    }

    println()
    println("汇总：路径 2（r ≤ 47 逐个 DP）${"%.1f".format(msPath2)} ms（$cnt 个三元组）；" +
        "路径 3（$tripleCount 个三元组的朴素循环，brute 口径）${"%.1f".format(msPath3)} ms")
    println("外推：若对 r < 5000 的全部三元组逐个跑 Frobenius DP，单个三元组的上界就有 2pqr ~ 10^11 " +
        "量级，$tripleCount 个三元组需 ~10^18 次操作，完全不可行——所以必须先证公式再求和")
    println("check() 全部通过")
}
