#!/usr/bin/env kotlin
// PE 204 暴力参照：显式生成所有 ≤1e9 的 100-光滑数放入集合（BFS 式逐素数扩展），
// 数集合大小。内存占用大（数百万个 Long），用于交叉验证 solution.kt 的递归计数。
// 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar

fun main() {
    val limit = 1_000_000_000L
    val primes = (2..100).filter { p -> (2 until p).all { p % it != 0 } }
    var set = HashSet<Long>()
    set.add(1)
    for (p in primes) {
        val pl = p.toLong()
        val next = HashSet<Long>(set)
        for (v in set) {
            var x = v
            while (x <= limit / pl) {
                x *= pl
                next.add(x)
            }
        }
        set = next
    }
    println(set.size)
}
