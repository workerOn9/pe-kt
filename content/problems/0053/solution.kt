/**
 * Project Euler 053 — Combinatoric Selections
 *
 * 优化解：按 n 逐行递推 C(n,r) = C(n,r-1)·(n-r+1)/r，且只算到 r ≤ n/2。
 * 二项式系数在 r ≤ n/2 上单调不减，一旦 C(n,r) 超过 10^6，由对称性 C(n,r)=C(n,n-r)
 * 立刻可知 r 到 n-r 这一段共 n-2r+1 个取值全部超标，整段记账后换下一行。
 * 超标前每个中间值都 ≤ 10^6，乘上 (n-r+1) ≤ 100 不会溢出 Long。
 *
 * 复杂度：O(N²) 以内的乘除法，且每行只算到首次超标处；N=100 时不足 10^4 次运算。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val limit = 1_000_000L
    var count = 0L
    for (n in 1..100) {
        var c = 1L
        var r = 1
        while (2 * r <= n) {
            c = c * (n - r + 1) / r
            if (c > limit) {
                count += (n - 2 * r + 1).toLong()
                break
            }
            r++
        }
    }
    return count
}

fun main() {
    println(solve())
}
