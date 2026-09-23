package dev.pekt.problems

/**
 * PE 194 暴力解：按真实图结构逐顶点回溯染色，直接数出配置数。
 *
 * 单元图（取自题面插图，7 个顶点）：
 *   1=左上 2=右上 6=左下 7=右下 3=上中 4=正中 5=下中
 *   单元 A 的边：12,13,16,23,27,34,45,56,57,67（10 条，含底边 67）
 *   单元 B 的边：去掉底边 67 的 9 条
 * 相邻单元沿竖直边粘合：下一单元的左竖边 (1,6) 与上一单元的右竖边 (2,7) 重合，
 * 因此 n 个单元的链图有 7 + 5(n-1) = 5n + 2 个顶点。
 *
 * 本文件完全不使用色多项式公式，只做回溯染色计数，用于验证
 * N(a,b,c) = C(a+b,a) * c(c-1) * X_A(c)^a * X_B(c)^b
 * （对 (1,0,3)、(0,2,4)、(1,1,3)、(2,2,3) 等小参数交叉核对）。
 *
 * 复杂度：O(C(a+b,a) * 分支数)，仅适合小参数。
 */

private val UNIT_A = listOf(1 to 2, 1 to 3, 1 to 6, 2 to 3, 2 to 7, 3 to 4, 4 to 5, 5 to 6, 5 to 7, 6 to 7)
private val UNIT_B = listOf(1 to 2, 1 to 3, 1 to 6, 2 to 3, 2 to 7, 3 to 4, 4 to 5, 5 to 6, 5 to 7)

/** 按给定单元顺序拼接链图，返回邻接表 */
private fun buildChain(order: List<Boolean>): List<IntArray> {
    val vertices = 5 * order.size + 2
    val adj = Array(vertices) { sortedSetOf<Int>() }
    fun add(u: Int, v: Int) { adj[u].add(v); adj[v].add(u) }
    var prevLeftTop = 0
    var prevLeftBottom = 1
    for ((i, isA) in order.withIndex()) {
        val g = IntArray(8)
        g[1] = prevLeftTop
        g[6] = prevLeftBottom
        g[2] = 2 + 5 * i
        g[7] = 3 + 5 * i
        g[3] = 4 + 5 * i
        g[4] = 5 + 5 * i
        g[5] = 6 + 5 * i
        for ((u, v) in if (isA) UNIT_A else UNIT_B) add(g[u], g[v])
        prevLeftTop = g[2]
        prevLeftBottom = g[7]
    }
    return adj.map { it.toIntArray() }
}

/** 回溯统计用 c 种颜色（颜色可不用）的合法染色数 */
private fun countColourings(adj: List<IntArray>, c: Int): Long {
    val col = IntArray(adj.size) { -1 }
    fun rec(v: Int): Long {
        if (v == adj.size) return 1L
        var total = 0L
        colour@ for (k in 0 until c) {
            for (u in adj[v]) if (col[u] == k) continue@colour
            col[v] = k
            total += rec(v + 1)
            col[v] = -1
        }
        return total
    }
    return rec(0)
}

/** 枚举 a 个 A 与 b 个 B 的全部排列，逐个链图暴力计数 */
fun bruteForce194(a: Int, b: Int, c: Int): Long {
    val orders = mutableListOf<List<Boolean>>()
    fun gen(cur: MutableList<Boolean>, restA: Int, restB: Int) {
        if (restA == 0 && restB == 0) { orders.add(cur.toList()); return }
        if (restA > 0) { cur.add(true); gen(cur, restA - 1, restB); cur.removeAt(cur.size - 1) }
        if (restB > 0) { cur.add(false); gen(cur, restA, restB - 1); cur.removeAt(cur.size - 1) }
    }
    gen(mutableListOf(), a, b)
    var total = 0L
    for (o in orders) total += countColourings(buildChain(o), c)
    return total
}

fun main() {
    println("N(1,0,3) = ${bruteForce194(1, 0, 3)}  (题面 24)")
    println("N(0,2,4) = ${bruteForce194(0, 2, 4)}  (题面 92928)")
    println("N(2,2,3) = ${bruteForce194(2, 2, 3)}  (题面 20736)")
    println("N(1,1,3) = ${bruteForce194(1, 1, 3)}  (公式 288)")
    println("N(2,1,3) = ${bruteForce194(2, 1, 3)}  (公式 3*6*4^2*6 = 1728)")
}
