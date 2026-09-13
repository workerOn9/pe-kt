/**
 * Project Euler 086 — Cuboid Route
 *
 * 思路：把三条棱记为 a ≤ b ≤ c。房间展开后三条候选路径长度为 √((a+b)²+c²)、√((a+c)²+b²)、√((b+c)²+a²)；
 * 因为 (a+b)²+c² ≤ (a+c)²+b² ⟺ b ≤ c、(a+b)²+c² ≤ (b+c)²+a² ⟺ a ≤ c，最短路径恒为 √((a+b)²+c²)。
 * 于是只需统计「a+b 与 c 构成勾股数」的三元组。令 m=a+b，则固定 c 后只需对 m=2..2c 逐个判断 m²+c² 是否平方，
 * 再对每个合法 (m,c) 用 O(1) 组合计数得到满足 a ≤ b ≤ c、a+b=m 的 (a,b) 对数；
 * c（即最大棱长 M）从 1 递增并累加，首次超过 10^6 时的 M 即为答案。
 * 复杂度：O(M²)，M 为答案量级（约 1818）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private fun p086IsSquare(v: Long): Boolean {
    val r = Math.sqrt(v.toDouble()).toLong()
    return r * r == v || (r + 1) * (r + 1) == v
}

fun solve(): Long {
    val target = 1_000_000L
    var count = 0L
    var maxSide = 0L
    while (count <= target) {
        maxSide++
        var m = 2L
        while (m <= 2 * maxSide) {
            if (p086IsSquare(m * m + maxSide * maxSide)) {
                val lo = (m + 1) / 2
                val hi = Math.min(m - 1, maxSide)
                if (hi >= lo) count += hi - lo + 1
            }
            m++
        }
    }
    return maxSide
}

fun main() {
    println(solve())
}
