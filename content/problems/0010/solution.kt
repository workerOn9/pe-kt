/**
 * Project Euler 010 — Summation of Primes
 *
 * 优化解：埃氏筛。2..limit-1 一遍筛出所有素数后求和，O(n log log n)。
 * 200 万的筛在毫秒级完成。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(limit: Int = 2_000_000): Long {
    val isComposite = BooleanArray(limit)
    var sum = 0L
    for (i in 2 until limit) {
        if (!isComposite[i]) {
            sum += i
            var j = i.toLong() * i           // 从 i² 开始标记，i*i 可能溢出 Int，用 Long
            while (j < limit) {
                isComposite[j.toInt()] = true
                j += i
            }
        }
    }
    return sum
}

fun main() {
    println(solve())
}
