package dev.pekt.problems

/**
 * Problem 187: Semiprimes (Brute Force / Double Pointer Baseline)
 *
 * 朴素筛法 + 双指针向内逼近对照实现。
 */

fun solve0187BruteForce(): Long {
    val limit = 100_000_000
    val maxP = limit / 2

    val isPrime = BooleanArray(maxP + 1) { true }
    isPrime[0] = false
    isPrime[1] = false
    val r = Math.sqrt(maxP.toDouble()).toInt()
    for (i in 2..r) {
        if (isPrime[i]) {
            var j = i * i
            while (j <= maxP) {
                isPrime[j] = false
                j += i
            }
        }
    }

    var count = 0
    for (i in 2..maxP) {
        if (isPrime[i]) count++
    }

    val primes = IntArray(count)
    var pIdx = 0
    for (i in 2..maxP) {
        if (isPrime[i]) primes[pIdx++] = i
    }

    var total = 0L
    var left = 0
    var right = primes.size - 1

    while (left <= right) {
        val p = primes[left].toLong()
        if (p * p >= limit) break
        while (right >= left && p * primes[right] >= limit) {
            right--
        }
        if (right >= left) {
            total += (right - left + 1)
        }
        left++
    }

    return total
}

fun main() {
    println(solve0187BruteForce())
}
