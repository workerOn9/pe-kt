#!/usr/bin/env kotlin
// PE 214 — Totient Chains（欧拉函数链）暴力参照
// 思路：不建筛表，对每个 n 直接试除分解质因数、按 phi(n) = n·Π(1-1/p) 算欧拉函数，
//       再逐项迭代到 1 数链长；判素数用 phi(n) == n-1。
//       复杂度 O(N^1.5)，只适用于小规模：链长 25 需要 n 达到 10^7 量级，
//       故本文件固定用 N=200000、目标链长 19（该范围内最长的链）与筛法实现对照。
// 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
// 运行：java -jar brute-force.jar

private const val LIMIT = 200_000
private const val CHAIN_TARGET = 19

private fun totient(n: Int): Int {
    var m = n
    var result = n
    var p = 2
    while (p.toLong() * p <= m) {
        if (m % p == 0) {
            while (m % p == 0) m /= p
            result -= result / p
        }
        p++
    }
    if (m > 1) result -= result / m
    return result
}

private fun chainLength(n: Int): Int {
    var x = n
    var length = 1
    while (x > 1) {
        x = totient(x)
        length++
    }
    return length
}

fun main() {
    var sum = 0L
    for (n in 2 until LIMIT) {
        val p = totient(n)
        if (p == n - 1 && chainLength(n) == CHAIN_TARGET) sum += n
    }
    println(sum)
}
