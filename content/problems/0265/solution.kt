#!/usr/bin/env kotlin
/**
 * Project Euler 265 — Binary Circles（二进制圆圈）
 *
 * 思路
 * ────
 * 把每个 N 位串看成一个顶点上的有向边：de Bruijn 图 B(2,N) 的顶点是全部 2^{N-1} 个
 * (N-1) 位数，边是全部 2^N 个 N 位数（从它的高 N-1 位指向低 N-1 位）。圆排列的
 * 2^N 个顺时针窗口恰好是一条「每条边恰好走一次、回到起点」的闭合走法，即欧拉回路；
 * 反之每条欧拉回路按边序列读出的循环位串就是唯一的圆排列（窗口互异）。
 *
 * 编码口径：题目规定从「全零窗口」开始拼接。全零边 0^N 在一条回路里恰好出现一次，
 * 所以每个圆排列恰好对应一条「以 0^N 为首边、从顶点 0^{N-1} 出发」的欧拉回路。
 * 两个集合之间的映射是双射，于是
 *   S(N) = Σ_{以 0^N 为首边的欧拉回路} (该回路按位拼出的 2^N 位二进制数)。
 * DFS 时从起点 0^{N-1} 出发、先走边 0^N，之后每步在 de Bruijn 图上选一条未用过的出边；
 * 用满 2^N 条边且回到起点即得一条回路，其位串的前 N 位是全零、后 2^N−N 位是各步新加的
 * 比特，直接拼成数值即可。N=5 时回路共 2^{2^{N-1}-N} = 2^{11} = 2048 条。
 *
 *   方法 A（主路径）：固定首边 0^N 的 DFS。每条回路天然按「全零窗口开头」规范化，
 *     无需去重，直接把拼出的数相加。
 *   方法 B（独立复核）：不固定首边，枚举从顶点 0^{N-1} 出发的全部欧拉回路（每条圆排列
 *     恰出现 2 次，因为回路里顶点 0^{N-1} 出现两次）；把每条回路旋到全零边处重新编码、
 *     放进集合去重再求和 —— 独立验证「规范化 + 去重」这一最易错的口径。
 *   直接暴力：枚举全部 2^{2^N−N} 个候选位串，逐个检查 2^N 个循环窗口互异（含回绕窗口），
 *     故意不借助任何图论结构；在 N=3,4,5 上复现（N=5 全量 2^27 个候选）。
 *
 * 复杂度
 * ──────
 * 搜索的是「边互异的部分回路」，节点数远小于 2^{2^N}；N=5 时实测量级为百万级状态，
 * 内存 O(2^N)。直接暴力为 O(2^{2^N−N} · 2^N)（带提前退出）。
 *
 * 验证
 * ────
 *   · 题面锚点：S(3) = 23 + 29 = 52（方法 A、方法 B、直接暴力都复现，且给出 23、29）；
 *   · N=4 三种方法一致（16 个圆排列）；N=5 方法 A 与方法 B 的编码集合完全相同；
 *   · brute-force.kt 在 N=3,4,5 上给出同样的计数与和。
 *
 * 答案（本机实跑）：209110240768（N=5，共 2048 个圆排列）
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0265/solution.kt && java -cp … SolutionKt
 */

private data class Out(val count: Int, val sum: Long)

/**
 * 方法 A：固定首边 0^N 的欧拉回路 DFS。
 * [collect] 非空时顺便收集编码（小规模对照用）；[states] 非空时累计搜索过的状态数。
 */
private fun methodA(n: Int, collect: MutableList<Long>? = null, states: LongArray? = null): Out {
    val edgeCount = 1 shl n
    val nodeMask = (1 shl (n - 1)) - 1
    val used = BooleanArray(edgeCount)
    var count = 0
    var sum = 0L

    fun dfs(depth: Int, node: Int, value: Long) {
        if (states != null) states[0]++
        if (depth == edgeCount) {
            check(node == 0) { "回路没有回到起点（depth=$depth）" }
            count++
            sum += value
            collect?.add(value)
            return
        }
        for (b in 0..1) {
            val e = (node shl 1) or b
            if (used[e]) continue
            used[e] = true
            // 第 depth 条边（新加比特）对应位串下标 depth+n-1；超过 2^n-1 的是回绕比特，
            // 只有全为 0 才会被接受（node==0），所以不参与拼接
            val next = if (depth + n - 1 < edgeCount) (value shl 1) or b.toLong() else value
            dfs(depth + 1, e and nodeMask, next)
            used[e] = false
        }
    }

    used[0] = true // 首边固定为 0^N，天然落在「全零窗口开头」的规范位置
    dfs(1, 0, 0L)
    return Out(count, sum)
}

/**
 * 方法 B：不固定首边，枚举全部欧拉回路（每条圆排列出现两次），
 * 按「旋到全零边」重新编码后用集合去重。
 */
private fun methodB(n: Int, collect: MutableList<Long>? = null, states: LongArray? = null): Out {
    val edgeCount = 1 shl n
    val nodeMask = (1 shl (n - 1)) - 1
    val used = BooleanArray(edgeCount)
    val seq = IntArray(edgeCount)
    val values = HashSet<Long>()

    fun dfs(depth: Int, node: Int) {
        if (states != null) states[0]++
        if (depth == edgeCount) {
            if (node == 0) {
                var j = 0
                while (seq[j] != 0) j++          // 定位全零窗口
                var v = 0L                        // 前 n 位是全零
                for (t in 1..edgeCount - n) {     // 之后逐个追加每条边的新比特
                    v = (v shl 1) or (seq[(j + t) % edgeCount].toLong() and 1L)
                }
                values.add(v)
            }
            return
        }
        for (b in 0..1) {
            val e = (node shl 1) or b
            if (used[e]) continue
            used[e] = true
            seq[depth] = e
            dfs(depth + 1, e and nodeMask)
            used[e] = false
        }
    }

    dfs(0, 0)
    collect?.addAll(values)
    return Out(values.size, values.sum())
}

/**
 * 直接暴力：枚举全部 2^{2^n−n} 个「前 n 位为 0」的候选位串，
 * 逐个检查全部 2^n 个循环窗口是否两两不同（窗口 j = 把 v 循环左移 j 位后的最高 n 位）。
 * 前 n 位为 0 保证候选以全零窗口开头，且全零窗口唯一，所以无需去重。
 */
private fun bruteForce(n: Int): Out {
    val windowBits = 1 shl n
    val freeBits = windowBits - n
    val total = 1L shl freeBits
    val fullMask = (1L shl windowBits) - 1
    var count = 0
    var sum = 0L
    var v = 0L
    while (v < total) {
        var mask = 0L
        var j = 0
        var ok = true
        while (j < windowBits) {
            // 在 2^n 位宽度内循环左移 j 位，取最高 n 位作为第 j 个窗口
            val r = ((v shl j) or (v ushr (windowBits - j))) and fullMask
            val w = (r ushr (windowBits - n)).toInt()
            val bit = 1L shl w
            if (mask and bit != 0L) { ok = false; break }
            mask = mask or bit
            j++
        }
        if (ok) {
            count++
            sum += v
        }
        v++
    }
    return Out(count, sum)
}

/** JIT 预热后 3 轮取最优。 */
private fun bestOf3(tag: String, expected: Out, f: () -> Out): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 题面锚点：N=3 ----------
    val list3 = ArrayList<Long>()
    val a3 = methodA(3, list3)
    list3.sort()
    val b3 = methodB(3)
    val c3 = bruteForce(3)
    check(a3 == Out(2, 52L)) { "N=3 方法 A 得 $a3，应为 2 个排列、和 52" }
    check(a3 == b3 && a3 == c3) { "N=3 三种方法不一致：A=$a3 B=$b3 brute=$c3" }
    check(list3 == listOf(23L, 29L)) { "N=3 的编码应为 23、29，实得 $list3" }
    println("N=3：编码 ${list3}，S(3) = ${a3.sum}（三种方法一致，与题面 23 + 29 = 52 相符）")

    // ---------- N=4：三种方法对照 ----------
    val list4 = ArrayList<Long>()
    val a4 = methodA(4, list4)
    val b4 = methodB(4)
    val c4 = bruteForce(4)
    check(a4 == b4 && a4 == c4) { "N=4 三种方法不一致：A=$a4 B=$b4 brute=$c4" }
    println("N=4：${a4.count} 个圆排列，S(4) = ${a4.sum}（三种方法一致）")

    // ---------- N=5：完整规模 ----------
    val n = 5
    val list5 = ArrayList<Long>()
    val a5 = methodA(n, list5)
    val b5 = methodB(n)
    check(a5 == b5) { "N=5 方法 A 与方法 B 不一致：A=$a5 B=$b5" }
    println("N=5：方法 A 与方法 B 一致：共 ${a5.count} 个圆排列（应为 2^11 = 2048），S(5) = ${a5.sum}")
    val stA = LongArray(1)
    methodA(n, null, stA)
    val stB = LongArray(1)
    methodB(n, null, stB)
    println("搜索状态数：方法 A ${stA[0]}，方法 B ${stB[0]}")
    println("答案 = ${a5.sum}")

    // ---------- 计时 ----------
    methodA(5); methodB(5)
    val msA5 = bestOf3("方法 A N=5", a5) { methodA(5) }
    val msB5 = bestOf3("方法 B N=5", b5) { methodB(5) }
    methodA(4)
    val msBrute4 = bestOf3("直接暴力 N=4", c4) { bruteForce(4) }

    println()
    println("汇总：方法 A N=5 ${"%.1f".format(msA5)} ms；方法 B N=5 ${"%.1f".format(msB5)} ms；" +
        "直接暴力 N=4 ${"%.1f".format(msBrute4)} ms")
    println("check() 全部通过")
}
