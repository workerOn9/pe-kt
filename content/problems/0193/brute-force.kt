package dev.pekt.problems

/**
 * PE 193 暴力解：直接枚举，对每个数检查是否有质因子平方整除它。
 * 仅用于小范围验证，对 2^50 不可行。
 * 用质数表预筛，检查每个质数 p，若 p^2 | n 则非 squarefree。
 */
fun bruteForce193(N: Int = 10000): Int {
    val primes = sievePrimes(sqrtFloor(N.toInt()).toInt())
    var count = 0
    for (n in 1..N) {
        var m = n
        var squarefree = true
        for (p in primes) {
            if (p * p > m) break
            if (m % (p * p) == 0) {
                squarefree = false
                break
            }
        }
        if (squarefree) count++
    }
    return count
}

private fun sievePrimes(n: Int): List<Int> {
    if (n < 2) return emptyList()
    val isPrime = BooleanArray(n + 1) { true }
    isPrime[0] = false
    isPrime[1] = false
    for (i in 2..Math.sqrt(n.toDouble()).toInt()) {
        if (isPrime[i]) {
            for (j in i * i..n step i) isPrime[j] = false
        }
    }
    return (2..n).filter { isPrime[it] }
}

private fun sqrtFloor(n: Int): Int {
    var x = Math.sqrt(n.toDouble()).toInt()
    while ((x + 1) * (x + 1) <= n) x++
    while (x * x > n) x--
    return x
}

fun main() {
    println("暴力解 T(10000) = ${bruteForce193(10000)}")
}
