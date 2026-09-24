#!/usr/bin/env kotlin
// PE 219 — Skew-cost Coding（偏斜代价编码）暴力参照
// 思路：用两种彼此独立的方法给出「n 个码字的最小总代价」的下界与对照值：
//   1) 区间 DP：前缀码若不止一个码字，根结点必有两个子树，码字按落在左/右子树分成
//      i 与 n-i 个，左子树每条边（0 比特）1 便士、右子树（1 比特）4 便士，故
//        f(1) = 0,  f(n) = min_{1≤i<n} [ f(i) + f(n-i) + i + 4(n-i) ]。
//      这是最优性的直接定义，可用来验证贪心（劈开最便宜叶子）是否真的最优。
//   2) 优先队列模拟贪心：反复取出最小代价叶子，替换成 c+1、c+4。
//   两者在 n ≤ 300 时逐项相同，且 f(6) = 35 与题面一致。
// 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
// 运行：java -jar brute-force.jar

import java.util.PriorityQueue

/** 区间 DP：前缀码最小总代价（定义式）。 */
private fun costDp(n: Int): Long {
    val f = LongArray(n + 1) { Long.MAX_VALUE / 4 }
    f[1] = 0L
    for (k in 2..n) {
        var best = Long.MAX_VALUE / 4
        for (i in 1 until k) {
            val cand = f[i] + f[k - i] + i + 4L * (k - i)
            if (cand < best) best = cand
        }
        f[k] = best
    }
    return f[n]
}

/** 优先队列模拟：反复劈开当前最便宜的叶子。 */
private fun costHeap(n: Int): Long {
    if (n <= 1) return 0L
    val pq = PriorityQueue<Int>()
    pq.add(0)
    var total = 0L
    repeat(n - 1) {
        val c = pq.poll()
        total += c + 5
        pq.add(c + 1)
        pq.add(c + 4)
    }
    return total
}

fun main() {
    val t0 = System.nanoTime()
    println("Cost(6)（题面锚点）= ${costDp(6)} / heap ${costHeap(6)}")
    var ok = true
    for (n in 2..300) {
        val a = costDp(n)
        val b = costHeap(n)
        if (a != b) {
            println("MISMATCH n=$n: dp=$a heap=$b")
            ok = false
        }
    }
    println(if (ok) "n = 2..300：区间 DP 与堆模拟完全一致" else "存在不一致")
    for (n in intArrayOf(1, 2, 3, 4, 5, 6, 10, 100, 1000)) {
        println("Cost($n) = ${costHeap(n)}")
    }
    val ms = (System.nanoTime() - t0) / 1_000_000
    System.err.println("brute force 219 wall = $ms ms")
}
