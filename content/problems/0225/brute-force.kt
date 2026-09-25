#!/usr/bin/env kotlin
/**
 * PE 225 对照实现 —— 不用「模 n 转移矩阵可逆 ⇒ 轨道纯周期」这条性质，
 * 改为老老实实用 HashSet 记录出现过的状态三元组，遇到重复就停：
 *   - 出现 0        -> n 整除某一项；
 *   - 状态重复      -> 轨道闭环、环内无 0，n 不整除任何一项。
 * 语义与 solution.kt 等价（多项式个状态的有限状态机），但实现笨重得多，
 * 用来交叉验证答案，也顺便量出「朴素实现」的耗时。
 * 运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 */

private fun dividesSomeNaive(n: Int): Boolean {
    val seen = HashSet<Int>()
    fun key(a: Int, b: Int, c: Int): Int = ((a * n + b) * n + c)
    var x = 1 % n
    var y = x
    var z = x
    while (true) {
        if (x == 0 || y == 0 || z == 0) return true
        if (!seen.add(key(x, y, z))) return false
        val t = (x + y + z) % n
        x = y; y = z; z = t
    }
}

fun main() {
    val t0 = System.nanoTime()
    var found = 0
    var n = 3
    var answer = -1
    while (found < 124) {
        if (!dividesSomeNaive(n)) {
            found++
            if (found == 124) answer = n
        }
        n += 2
    }
    val ms = (System.nanoTime() - t0) / 1_000_000
    println("PE 225 answer = $answer  (朴素 HashSet 实现 ${ms} ms)")
}
