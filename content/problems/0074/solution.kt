/**
 * Project Euler 074 — Digit Factorial Chains
 *
 * 优化解：把「链长」做成记忆化 DP。f(n) = 各位数字阶乘之和；任何链最终都会撞进
 * 三个已知环之一，所以从某个数出发的「不重复项个数」满足
 *   len(x) = 1 + len(f(x))      （f(x) 不在当前路径上）
 * 一趟行走中用路径栈记录下标，撞到缓存值或撞回路径内的结点即可一次性回填整条链。
 * f(n) ≤ 6·9! = 2177280，所有中间值都落在定长数组内，缓存命中率极高。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val limit = 1_000_000
    val fact = intArrayOf(1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880)
    val cap = 2_177_280
    val len = IntArray(cap + 1) { -1 }
    val seen = IntArray(cap + 1) { -1 }
    val path = IntArray(256)
    var total = 0L
    for (start in 1 until limit) {
        var x = start
        var depth = 0
        while (true) {
            val cached = len[x]
            if (cached >= 0) {
                for (j in 0 until depth) len[path[j]] = (depth - j) + cached
                break
            }
            val at = seen[x]
            if (at >= 0) {
                for (j in at until depth) len[path[j]] = depth - at
                for (j in 0 until at) len[path[j]] = depth - j
                break
            }
            seen[x] = depth
            path[depth] = x
            depth++
            x = p074DigitFactSum(x, fact)
        }
        for (j in 0 until depth) seen[path[j]] = -1
        if (len[start] == 60) total++
    }
    return total
}

fun p074DigitFactSum(n: Int, fact: IntArray): Int {
    var x = n
    var s = 0
    while (x > 0) {
        s += fact[x % 10]
        x /= 10
    }
    return s
}

fun main() {
    println(solve())
}
