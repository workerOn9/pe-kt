package dev.pekt.engine

import java.math.BigInteger

/**
 * PE 303 — Multiples with Small Digits（小数字倍数）：f(n) = n 的最小只含数字 0/1/2 的倍数。
 * 求 Σ_{n=1}^{10000} f(n)/n。
 *
 * 推导（详见 content/problems/0303/solution.kt 头部与 0303/analysis.md）：
 *   余数图 BFS：节点 = 模 n 余数，边 = 追加数字 c ∈ {0,1,2}（r → 10r+c mod n），
 *   初始为一位数 1、2；层内先 0 再 1 再 2 ⇒ 「位数少、同位数字典序小」最优，
 *   首个到达余数 0 的路径即 f(n)。f(9999) 为 20 位须用 BigInteger。
 * 答案 = 1111981904675169（样例 Σ100 = 11363107 强断言一致）。
 */
internal fun solve0303Impl(): Long {
    fun f(n: Int): BigInteger {
        if (n == 1) return BigInteger.ONE
        val parent = IntArray(n) { -1 }
        val digit = IntArray(n)
        val q = ArrayDeque<Int>()
        for (d in 1..2) {
            val r = d % n
            if (parent[r] == -1) { parent[r] = -2; digit[r] = d; q.addLast(r) }
        }
        while (q.isNotEmpty()) {
            val r = q.removeFirst()
            if (r == 0) {
                val sb = StringBuilder()
                var cur = 0
                while (true) {
                    sb.append(digit[cur])
                    if (parent[cur] == -2) break
                    cur = parent[cur]
                }
                return sb.reverse().toString().toBigInteger()
            }
            for (c in 0..2) {
                val nr = (r * 10 + c) % n
                if (parent[nr] == -1) { parent[nr] = r; digit[nr] = c; q.addLast(nr) }
            }
        }
        error("no multiple for n = $n")
    }
    check(f(2) == BigInteger("2") && f(3) == BigInteger("12") && f(7) == BigInteger("21") &&
        f(42) == BigInteger("210") && f(89) == BigInteger("1121222")) { "题面样例不符" }
    var sum100 = BigInteger.ZERO
    for (n in 1..100) sum100 += f(n) / n.toBigInteger()
    check(sum100 == BigInteger("11363107")) { "Σ_{n=1}^{100} f(n)/n 与题面样例不符" }
    var sum = BigInteger.ZERO
    for (n in 1..10000) sum += f(n) / n.toBigInteger()
    return sum.toLong()
}
