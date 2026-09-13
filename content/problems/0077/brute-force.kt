/**
 * Project Euler 077 — 暴力解（教学对比用）
 *
 * 思路：对每个 n = 2, 3, 4, ... 直接按定义递归枚举它拆成素数之和的所有方案并计数，
 * 不缓存任何中间结果，也不做跨 n 的复用；第一个计数超过 5000 的 n 就是答案。
 * 递归时要求后续加数不超过当前加数，从而保证顺序无关、每个多重集合只数一次。
 * 复杂度：O(∑_{n≤N} T(n))，T(n) 为 n 的素数分拆递归树规模，N = 71。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    val limit = 100
    val primes = (2..limit).filter { n ->
        var d = 2
        while (d.toLong() * d <= n) {
            if (n % d == 0) return@filter false
            d++
        }
        true
    }

    var answer = 0L
    for (n in 2..limit) {
        var count = 0L

        fun rec(remaining: Int, maxIdx: Int) {
            if (remaining == 0) {
                count++
                return
            }
            var i = maxIdx
            while (i >= 0) {
                val prime = primes[i]
                if (prime <= remaining) rec(remaining - prime, i)
                i--
            }
        }

        rec(n, primes.size - 1)
        if (count > 5000L) {
            answer = n.toLong()
            break
        }
    }
    return answer
}

fun main() {
    println(solveBruteForce())
}
