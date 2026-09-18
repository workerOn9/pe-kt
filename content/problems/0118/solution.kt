/**
 * Project Euler 118 — Pandigital Prime Sets（全数字素数集合）
 *
 * 思路：枚举无重复数字的整数，用筛出的素数试除，按数字掩码累计素数数量。
 * 对剩余数字集合做记忆化搜索，每次只选包含其最小数字的块，消除块顺序重复。
 * 复杂度：设 D=9，候选数上界 M<10^D，A=Σ P(D,k)，建表
 * O(√M log log M + A·π(√M))，计数 O(3^D)，空间 O(√M+2^D+D)。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val limit = 31623
    val composite = BooleanArray(limit + 1)
    val primes = ArrayList<Int>()
    for (p in 2..limit) {
        if (!composite[p]) {
            primes.add(p)
            if (p <= limit / p) {
                for (multiple in p * p..limit step p) composite[multiple] = true
            }
        }
    }
    fun isPrime(value: Int): Boolean {
        if (value < 2) return false
        for (p in primes) {
            if (p > value / p) return true
            if (value % p == 0) return false
        }
        return true
    }
    val counts = IntArray(512)
    fun extend(value: Int, used: Int, sum: Int) {
        if (value < 10) {
            if (isPrime(value)) counts[used]++
        } else if (value % 2 != 0 && value % 5 != 0 && sum % 3 != 0 && isPrime(value)) {
            counts[used]++
        }
        for (digit in 1..9) {
            val bit = 1 shl (digit - 1)
            if (used and bit == 0) extend(value * 10 + digit, used or bit, sum + digit)
        }
    }
    extend(0, 0, 0)
    val memo = LongArray(512) { -1L }
    fun count(remaining: Int): Long {
        if (remaining == 0) return 1L
        if (memo[remaining] >= 0) return memo[remaining]
        val lowest = remaining and -remaining
        var total = 0L
        var block = remaining
        while (block != 0) {
            if (block and lowest != 0 && counts[block] != 0) {
                total += counts[block].toLong() * count(remaining xor block)
            }
            block = (block - 1) and remaining
        }
        memo[remaining] = total
        return total
    }
    check(count(7) == 2L)
    check(count(1 shl 2) == 1L)
    return count(511)
}

fun main() { println(solve()) }
