#!/usr/bin/env kotlin
// PE 214 — Totient Chains（欧拉函数链）
// 思路：用「筛法求欧拉函数」一次性算出 n < 4·10^7 的全部 phi 值：
//       先令 phi[i] = i，再对每个素数 p 把 phi[j] -= phi[j]/p（j 取 p 的所有倍数），
//       复杂度 O(N log log N)。因为 phi[n] < n，链长可以按 n 递增递推：
//       len[1] = 1，len[n] = len[phi[n]] + 1（链长 = 到 1 的迭代步数 + 1）。
//       n 为素数当且仅当 phi[n] = n - 1，据此挑出链长为 25 的素数求和。
// 复杂度：筛 O(N log log N)，递推 O(N)，N = 4·10^7；内存 phi(int) + len(byte) ≈ 200 MB。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

private const val LIMIT = 40_000_000
private const val CHAIN_TARGET = 25

private fun solve214(): Long {
    val phi = IntArray(LIMIT) { it }
    for (p in 2 until LIMIT) {
        if (phi[p] != p) continue          // 合数，跳过
        var j = p
        while (j < LIMIT) {
            phi[j] -= phi[j] / p
            j += p
        }
    }

    val chain = ByteArray(LIMIT)
    chain[1] = 1
    var sum = 0L
    for (n in 2 until LIMIT) {
        val length = (chain[phi[n]] + 1).toByte()
        chain[n] = length
        if (length == CHAIN_TARGET.toByte() && phi[n] == n - 1) sum += n
    }
    return sum
}

fun main() {
    println(solve214())
}
