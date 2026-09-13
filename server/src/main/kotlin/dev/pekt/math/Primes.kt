package dev.pekt.math

/**
 * 埃氏筛（Sieve of Eratosthenes）。
 *
 * 返回长度为 [limit] + 1 的布尔数组，`result[i] == true` 表示 `i` 是素数。
 * 索引 0 和 1 恒为 `false`。
 *
 * - 时间复杂度：O(n log log n)
 * - 空间复杂度：O(n)
 *
 * @param limit 上界（含）。必须 ≥ 0，否则抛 [IllegalArgumentException]。
 */
fun sieve(limit: Int): BooleanArray {
    require(limit >= 0) { "limit must be non-negative, was $limit" }
    val isComposite = BooleanArray(limit + 1)
    val result = BooleanArray(limit + 1) { it >= 2 }
    if (limit < 2) return result
    var p = 2
    while (p.toLong() * p <= limit) {
        if (!isComposite[p]) {
            var multiple = p * p
            while (multiple <= limit) {
                isComposite[multiple] = true
                multiple += p
            }
        }
        p++
    }
    for (i in 2..limit) result[i] = !isComposite[i]
    return result
}

/**
 * 返回 ≤ [limit] 的所有素数，升序。
 *
 * - 时间复杂度：O(n log log n)（埃氏筛）
 * - 空间复杂度：O(n)
 *
 * @param limit 上界（含）。必须 ≥ 0，否则抛 [IllegalArgumentException]。
 */
fun primesUpTo(limit: Int): List<Long> {
    val isPrime = sieve(limit)
    val primes = ArrayList<Long>()
    for (i in 2..limit) if (isPrime[i]) primes.add(i.toLong())
    return primes
}

/**
 * 小范围内的素性判定（试除法）。
 *
 * 适用于 [n] 较小的场景（约 n ≤ 10^12 内可接受）；
 * 更大范围请使用筛法批量判定或 Miller-Rabin（本库暂未提供）。
 *
 * - 时间复杂度：O(√n)
 * - 空间复杂度：O(1)
 *
 * @return `n` 为素数时返回 `true`；`n < 2` 返回 `false`。
 */
fun isPrime(n: Long): Boolean {
    if (n < 2) return false
    if (n < 4) return true // 2, 3
    if (n % 2L == 0L) return false
    var d = 3L
    while (d <= n / d) {
        if (n % d == 0L) return false
        d += 2
    }
    return true
}

/**
 * 返回第 [n] 个素数（1-indexed）：nthPrime(1) = 2, nthPrime(6) = 13。
 *
 * 实现：用 Rosser 定理上界 `n * (ln n + ln ln n)` 估算筛的范围，
 * 不足时倍增扩容重筛，直到收齐 n 个素数。
 *
 * - 时间复杂度：O(n log n · log log n)（上界尺度上的一次埃氏筛）
 * - 空间复杂度：O(n log n)
 *
 * @param n 序号，必须 ≥ 1，否则抛 [IllegalArgumentException]。
 */
fun nthPrime(n: Int): Long {
    require(n >= 1) { "n must be >= 1, was $n" }
    if (n <= 5) return listOf(2L, 3L, 5L, 7L, 11L)[n - 1] // ln ln n 对小 n 无定义/为负

    var limit = (n * (Math.log(n.toDouble()) + Math.log(Math.log(n.toDouble())))).toInt() + 10
    while (true) {
        val primes = primesUpTo(limit)
        if (primes.size >= n) return primes[n - 1]
        limit *= 2
    }
}
