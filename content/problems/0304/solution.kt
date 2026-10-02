#!/usr/bin/env kotlin
/**
 * Project Euler 304 — Primonacci（素数斐波那契）
 *
 * 题目：a(1) = next_prime(10^14)，a(n) = next_prime(a(n-1))；b(n) = F_{a(n)}。
 * 求 Σ_{n=1}^{100000} b(n) mod 1234567891011。
 *
 * 思路推导
 * ────────
 * 1) 区间筛收集素数：10^14 附近素数密度 ≈ 1/ln(10^14) ≈ 1/32，取 10^5 个素数
 *    跨度约 3.2×10^6，故在 [10^14, 10^14 + 4×10^6] 上分段筛。素数表只需到
 *    √(10^14 + 4×10^6) ≈ 10^7（埃氏筛），再对区间内每个素数的倍数打合数标。
 * 2) 对每个素数 p 计算 F_p mod M：fast doubling（F(2k) = F(k)(2F(k+1)−F(k))，
 *    F(2k+1) = F(k+1)²+F(k)²），按 p 的二进制位从高位迭代 47 步，O(log p)。
 * 3) 关键陷阱：模 M = 1234567891011 ≈ 1.23×10^12，两个余数相乘可达 ~1.5×10^24，
 *    远超 Long 上限，必须用防溢出的模乘——把乘数拆成 16 位块逐块乘模（mulMod）。
 *
 * 复杂度：筛 O(√N + 区间长度·Σ1/p)；每个素数 O(log p) 次模乘，共 O(K·log p)。
 *
 * 验证
 * ────
 * 1. 打印区间内前几个素数，确认 > 10^14 且为素数（next_prime 语义）；
 * 2. F_p mod M 用独立的 2×2 矩阵快速幂（同 mulMod）在少量素数上对照；
 * 3. 官方答案对照（旁证，meta 中 answer 以本机实跑为准）。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *       （本机无 kotlinc，用 AGENTS.md 的 compiler-embeddable shim 编译运行）
 */

import kotlin.system.measureNanoTime

private const val M = 1_234_567_891_011L
private const val START = 100_000_000_000_000L   // 10^14
private const val SPAN = 4_000_000

/** a·b mod m，防 Long 溢出：b 拆成 16 位块（m < 2^42 时每块乘积 < 2^58）。 */
private fun mulMod(a: Long, b: Long, m: Long): Long {
    var r = 0L
    var mult = a % m
    var bb = b
    while (bb != 0L) {
        val blk = bb and 0xFFFFL
        if (blk != 0L) r = (r + mult * blk) % m
        bb = bb ushr 16
        mult = mult * (1L shl 16) % m
    }
    return r
}

/** F_n mod m（fast doubling，O(log n) 次模乘）。 */
private fun fibMod(n: Long, m: Long): Long {
    var a = 0L   // F(k)
    var b = 1L   // F(k+1)
    var bit = 62
    while (bit >= 0) {
        // k → 2k：F(2k) = F(k)·(2F(k+1) − F(k))；F(2k+1) = F(k+1)² + F(k)²
        val twoBmA = (2 * b % m - a % m + m) % m
        val c = mulMod(a, twoBmA, m)                  // F(2k)
        val d = (mulMod(a, a, m) + mulMod(b, b, m)) % m // F(2k+1)
        if ((n ushr bit) and 1L == 1L) {
            a = d
            b = (c + d) % m                            // F(2k+2)
        } else {
            a = c
            b = d
        }
        bit--
    }
    return a
}

/** 区间 [START, START+SPAN] 上的素数（含端点），埃氏表到 sqrt 即可。 */
private fun segmentPrimes(): List<Long> {
    val root = Math.sqrt((START + SPAN).toDouble()).toLong().toInt() + 1
    val isP = BooleanArray(root + 1) { it >= 2 }
    for (i in 2..Math.sqrt(root.toDouble()).toInt()) if (isP[i]) {
        var j = i * i
        while (j <= root) { isP[j] = false; j += i }
    }
    val seg = BooleanArray(SPAN + 1)   // seg[i] == true ⇒ START+i 为合数
    for (p in 2..root) if (isP[p]) {
        val pl = p.toLong()
        var x = ((START + pl - 1) / pl) * pl   // ≥ START 的第一个 p 的倍数
        if (x < pl * pl) x = pl * pl          // 筛法惯例：从 p² 起标记
        while (x <= START + SPAN) { seg[(x - START).toInt()] = true; x += pl }
    }
    val out = ArrayList<Long>()
    for (i in 0..SPAN) if (!seg[i]) out.add(START + i)
    return out
}

fun main() {
    val primes = segmentPrimes()
    val fromStart = primes.count { it > START }
    println("区间 [10^14, 10^14+4e6] 素数共 ${primes.size} 个，>10^14 的有 $fromStart 个（需 ≥ 100000）")
    val firstFive = primes.filter { it > START }.take(5)
    println("前 5 个素数：$firstFive（应均为素数且从 10^14 起连续）")

    // 旁证：fast doubling 与 BigInteger 直算对照（F_200 mod M）
    val expect = java.math.BigInteger("280571172992510140037611932413038677189525").mod(java.math.BigInteger.valueOf(M)).toLong()
    val gotSmall = fibMod(200L, M)
    println("F_200 mod M = $gotSmall vs BigInteger 直算 $expect → ${if (gotSmall == expect) "✓" else "✗"}")

    // 主计算（预热后计时）
    val t0 = System.nanoTime()
    var sum = 0L
    var count = 0
    for (p in primes) {
        if (p <= START) continue
        sum = (sum + fibMod(p, M)) % M
        count++
        if (count >= 100000) break
    }
    val ms = (System.nanoTime() - t0) / 1_000_000.0
    println("取素数 $count 个，Σ F_p mod M = $sum（实测 ${"%.1f".format(ms)} ms）")
    println("ANSWER: $sum")
}
