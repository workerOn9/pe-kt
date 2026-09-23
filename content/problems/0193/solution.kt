package dev.pekt.problems

/**
 * Problem 193: Squarefree Numbers
 *
 * 思路：Mobius 函数容斥原理
 *
 * 定义 Mobius 函数 $\mu(n)$：
 * - $\mu(1) = 1$
 * - 若 $n$ 有质因子平方，则 $\mu(n) = 0$
 * - 否则 $\mu(n) = (-1)^k$，$k$ 为不同质因子个数
 *
 * 关键公式：
 * $$\sum_{d|n} \mu(d) = \begin{cases} 1 & n = 1 \\ 0 & n > 1 \end{cases}$$
 *
 * 小于 $N$ 的无平方因子数个数：
 * $$Q(N) = \sum_{n=1}^{N} \sum_{d^2|n} \mu(d) = \sum_{d=1}^{\lfloor\sqrt{N}\rfloor} \mu(d) \cdot \left\lfloor\frac{N}{d^2}\right\rfloor$$
 *
 * 证明：交换求和顺序，枚举 $d^2$ 的倍数个数。
 *
 * $N = 2^{50}$，$\sqrt{N} = 2^{25} = 33554432$，线性筛 Mobius 后 O($\sqrt{N}$) 求和。
 *
 * 复杂度：O($\sqrt{N}$) 时间，O($\sqrt{N}$) 空间。
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
fun solve193(): Long {
    val N = 1L.shl(50) // 2^50
    val limit = sqrtFloor(N).toInt()

    // 线性筛求 Mobius 函数
    val mu = IntArray(limit + 1)
    val primes = mutableListOf<Int>()
    val isComposite = BooleanArray(limit + 1)
    mu[1] = 1

    for (i in 2..limit) {
        if (!isComposite[i]) {
            primes.add(i)
            mu[i] = -1
        }
        for (p in primes) {
            if (i * p > limit) break
            isComposite[i * p] = true
            if (i % p == 0) {
                mu[i * p] = 0
                break
            } else {
                mu[i * p] = -mu[i]
            }
        }
    }

    var count = 0L
    for (d in 1..limit) {
        if (mu[d] == 0) continue
        count += mu[d] * (N / (d.toLong() * d))
    }
    return count
}

private fun sqrtFloor(n: Long): Long {
    var x = Math.sqrt(n.toDouble()).toLong()
    while ((x + 1) * (x + 1) <= n) x++
    while (x * x > n) x--
    return x
}

fun main() { println(solve193()) }
