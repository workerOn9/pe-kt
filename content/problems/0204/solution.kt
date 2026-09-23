#!/usr/bin/env kotlin
// PE 204 — Generalised Hamming Numbers（广义哈明数计数）
// 思路：100 以内共 25 个素数。按素数从大到小递归：
//       f(i, L) = Σ_{k≥0} f(i+1, L / p_i^k)，f(25, ·) = 1。
//       每个 ≤1e9 的 100-光滑数被恰好枚举一次（素因子幂次的唯一分解）。
// 复杂度：递归节点数 ~ 答案量级 × 常数（数百万级调用，JVM 毫秒-百毫秒级）。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

fun main() {
    val limit = 1_000_000_000L
    val primes = (2..100).filter { p -> (2 until p).all { p % it != 0 } }
    fun f(i: Int, l: Long): Long {
        if (i == primes.size) return 1
        var total = 0L
        var rem = l
        val p = primes[i].toLong()
        while (rem >= 1) {
            total += f(i + 1, rem)
            rem /= p
        }
        return total
    }
    println(f(0, limit))
}
