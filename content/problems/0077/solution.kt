/**
 * Project Euler 077 — Prime Summations
 *
 * 思路：把「n 写成若干素数之和（顺序无关）」看作硬币找零，硬币面额是全体不超过 limit 的素数。
 * 一维完全背包计数：ways[0] = 1，依次处理每一枚素数硬币 p，正序更新 ways[s] += ways[s - p]，
 * 保证每个素数多重集合只被计数一次。然后从小到大扫描 n，第一个 ways[n] > 5000 的 n 即答案。
 * 复杂度：O(limit · π(limit)) 时间，O(limit) 空间；limit 取 100 足够。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val limit = 100
    val isPrime = BooleanArray(limit + 1) { it >= 2 }
    var p = 2
    while (p * p <= limit) {
        if (isPrime[p]) {
            var m = p * p
            while (m <= limit) {
                isPrime[m] = false
                m += p
            }
        }
        p++
    }

    val ways = LongArray(limit + 1)
    ways[0] = 1L
    for (prime in 2..limit) {
        if (!isPrime[prime]) continue
        for (s in prime..limit) ways[s] += ways[s - prime]
    }

    for (n in 2..limit) if (ways[n] > 5000L) return n.toLong()
    error("limit $limit too small")
}

fun main() {
    println(solve())
}
