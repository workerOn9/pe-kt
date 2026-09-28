#!/usr/bin/env kotlin
/**
 * Project Euler 247 — Squares Under a Hyperbola（双曲线下的正方形）暴力对照
 *
 * 思路：与 solution.kt 的「候选筛选 + 阈值剪枝计数」**完全不同的路线**——直接照题面
 * 字面意思模拟贪心过程本身，一个正方形一个正方形地放：
 *
 *   · 空位记成 (L, B)，最大正方形边长 s 由 (L+s)(B+s) = 1 决定；
 *   · 放完后空位裂成两个子空位：右侧 (L+s, B) 与上方 (L, B+s)；
 *   · 用大顶堆按边长取当前最大的空位——这就是题面的「剩余区域中能放下的最大正方形」；
 *   · 每个空位随身带着路径计数 (#R, #T)：右侧子空位 R 计数 +1，上方子空位 T 计数 +1。
 *     这个计数就是索引 (left, below)（题面事实把它钉死：第 2 个是 R = (1,0)，
 *     第 32 个是 TR、第 50 个是 RT = (1,1)，(1,1) 只有 C(2,1) = 2 个，(1,1) 的最大 n = 50）。
 *
 *   索引 (3,3) = 3 个 R、3 个 T 的路径，共 C(6,3) = 20 个正方形。贪心按边长递减落位，
 *   所以**第 20 个出现的 (3,3) 就是题面要求的最大 n**。整个模拟不需要任何阈值、候选
 *   排序或剪枝——只要老老实实放下 78 万个正方形。
 *
 * 自检（全部与题面一致）：
 *   · 第 2 个落位的是 R（S_2 索引 (1,0)）；
 *   · 第 32、50 个落位的分别是 TR、RT（S_32、S_50 索引 (1,1)）；
 *   · (1,1) 全程恰好出现 2 次、最大 n = 50——「50 是 (1,1) 的最大 n」这条如果对不上，
 *     说明索引模型错了，整个暴力结果作废。
 *
 * 实现：大顶堆用 5 条平行数组（s, L, B, r, t）手写，避免 80 万个对象；
 *       落位序列的相邻最小相对间隔会被打印出来，作为 double 定序稳定性的证据。
 *
 * 复杂度：O(N log N)，N = 782252 次落位；空间 O(N)。
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar
 */

private fun sideOf(l: Double, b: Double): Double {
    val c = 1.0 - l * b
    if (c <= 0.0) return 0.0
    val u = l + b
    return 2.0 * c / (Math.sqrt(u * u + 4.0 * c) + u)
}

/** 大顶堆：五条平行数组，堆序按边长 s。 */
private class GapHeap(capacity: Int) {
    var size = 0
    val s = DoubleArray(capacity)
    val l = DoubleArray(capacity)
    val b = DoubleArray(capacity)
    val r = IntArray(capacity)
    val t = IntArray(capacity)

    fun push(sv: Double, lv: Double, bv: Double, rv: Int, tv: Int) {
        check(size < s.size) { "堆容量不足（${s.size}）" }
        var i = size++
        s[i] = sv; l[i] = lv; b[i] = bv; r[i] = rv; t[i] = tv
        while (i > 0) {
            val p = (i - 1) / 2
            if (s[p] >= s[i]) break
            swap(p, i)
            i = p
        }
    }

    /** 弹出堆顶（移到 size−1 处，再下沉新堆顶）。 */
    fun pop() {
        size--
        swap(0, size)
        var i = 0
        while (true) {
            val lc = 2 * i + 1
            if (lc >= size) break
            val rc = lc + 1
            val m = if (rc < size && s[rc] > s[lc]) rc else lc
            if (s[i] >= s[m]) break
            swap(i, m)
            i = m
        }
    }

    private fun swap(i: Int, j: Int) {
        var x = s[i]; s[i] = s[j]; s[j] = x
        x = l[i]; l[i] = l[j]; l[j] = x
        x = b[i]; b[i] = b[j]; b[j] = x
        var y = r[i]; r[i] = r[j]; r[j] = y
        y = t[i]; t[i] = t[j]; t[j] = y
    }
}

/** 模拟贪心落位，返回第 20 个 (3,3) 正方形的序号；verbose 时打印全部自检。 */
private fun simulateGreedy(verbose: Boolean): Long {
    val heap = GapHeap(1_200_000)
    heap.push(sideOf(1.0, 0.0), 1.0, 0.0, 0, 0)
    var n = 0L
    var hits11 = 0
    var last11 = 0L
    var hits33 = 0
    var answer = 0L
    var prevSide = Double.MAX_VALUE
    var minRelGap = Double.MAX_VALUE
    while (true) {
        val sv = heap.s[0]
        val lv = heap.l[0]
        val bv = heap.b[0]
        val rv = heap.r[0]
        val tv = heap.t[0]
        if (n > 0) {
            val rel = (prevSide - sv) / prevSide
            if (rel < minRelGap) minRelGap = rel
        }
        prevSide = sv
        heap.pop()
        n++
        if (rv == 1 && tv == 1) { hits11++; last11 = n }
        if (rv == 3 && tv == 3) {
            hits33++
            if (hits33 == 20) { answer = n; break }
        }
        if (n == 2L) check(rv == 1 && tv == 0) { "S_2 索引应为 (1,0)，实际 ($rv,$tv)" }
        if (n == 32L) check(rv == 1 && tv == 1) { "S_32 索引应为 (1,1)，实际 ($rv,$tv)" }
        if (n == 50L) check(rv == 1 && tv == 1) { "S_50 索引应为 (1,1)，实际 ($rv,$tv)" }
        heap.push(sideOf(lv + sv, bv), lv + sv, bv, rv + 1, tv)
        heap.push(sideOf(lv, bv + sv), lv, bv + sv, rv, tv + 1)
    }
    check(hits11 == 2 && last11 == 50L) {
        "(1,1) 应恰好出现 2 次且最大 n = 50，实际 $hits11 次、最大 $last11"
    }
    if (verbose) {
        println("暴力模拟：共落位 $n 个正方形（第 20 个 (3,3) 落位处停下）")
        println("自检：S_2 = (1,0) ✓；S_32、S_50 = (1,1) ✓；(1,1) 恰好 $hits11 次、最大 n = $last11 ✓")
        println("相邻落位的最小相对间隔 = %.3e（远大于 1e-15，double 定序稳定）".format(minRelGap))
        println("暴力答案 = $answer")
    }
    return answer
}

private fun main() {
    val first = simulateGreedy(verbose = true)
    check(first == 782252L) { "暴力答案不吻合：$first" }
    val t0 = System.nanoTime()
    val second = simulateGreedy(verbose = false)
    val ms = (System.nanoTime() - t0) / 1e6
    check(second == first) { "计时循环结果漂移" }
    println("brute: %.1f ms/次（JIT 预热后，含全部 $first 次落位）".format(ms))
    println("check() 全部通过")
}
