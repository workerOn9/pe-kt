/**
 * PE 173 — brute force：对每个外边长 n 枚举所有孔边长 m，O(n^2)。
 * 只在较小 LIMIT 下可行，用于与 O(n) 解对拍并给出耗时对比。
 */
fun bruteForce(limit: Long): Long {
    var count = 0L
    var n = 3L
    while (n * n - (n - 2) * (n - 2) <= limit) {
        var m = n - 2
        while (m >= 1) {
            if (n * n - m * m <= limit) count++
            m -= 2
        }
        n++
    }
    return count
}

fun main() {
    println("limit=32   -> " + bruteForce(32))
    println("limit=100  -> " + bruteForce(100))
    println("limit=20000 -> " + bruteForce(20_000))
}
