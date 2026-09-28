#!/usr/bin/env kotlin
/**
 * Project Euler 260 — Stone Game：直接暴力对照（独立实现，与 solution.kt 不共享核心代码路径）
 *
 * 完全按题面定义求解，不做任何「唯一补全 / 剪枝」优化：
 *   · 按总和 s = x + y + z 递增处理全部排序三元组 (x ≤ y ≤ z)；
 *   · 对每个状态枚举 7 个移法族（只减 1 堆 ×3、同减 2 堆 ×3、三堆同减）× 全部 N ∈ [1, z]；
 *   · 只要有一条合法走法到达必败局面，本状态为 N；否则为 P（必败），把 x+y+z 计入 Σ。
 *
 * 复杂度 O(Z⁴)：Z=1000 时要检查约 7.5×10¹¹ 个「一步后继」，不可行；
 * 本文件只跑到 Z=100（题面给出的 173895 断言）与 Z=200 / Z=250（本机计时基准）。
 *
 * 输出：
 *   · Z=100 → Σ = 173895（与题面一致，作为正确性锚点）；
 *   · Z=200、Z=250 的 JIT 预热后 3 轮最优毫秒数（meta.bruteForceBaselineMs 的数据来源）。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

private data class Small(val sum: Long, val count: Long, val states: Long, val successorChecks: Long)

/**
 * 直接枚举：对每个排序三元组 (x ≤ y ≤ z) 逐条试完全部走法。
 * lost[排序后三元组] 记录该局面是否必败；所有后继的和都更小，按 s 递增即可递推。
 */
private fun brute(zMax: Int): Small {
    val sz = zMax + 1
    val lost = BooleanArray(sz * sz * sz)

    /** 查询任意三堆（自动排序成规范下标）是否已知必败。 */
    fun lostAt(a: Int, b: Int, c: Int): Boolean {
        var u = a
        var v = b
        var w = c
        if (u > v) { val t = u; u = v; v = t }
        if (v > w) { val t = v; v = w; w = t }
        if (u > v) { val t = u; u = v; v = t }
        return lost[(u * sz + v) * sz + w]
    }

    var sum = 0L
    var count = 0L
    var states = 0L
    var checks = 0L
    for (s in 0..3 * zMax) {
        var x = 0
        while (x <= zMax && x <= s) {
            var y = x
            while (y <= zMax && x + y <= s) {
                val z = s - x - y
                if (z in y..zMax) {
                    states++
                    var selfLost = true
                    var n = 1
                    while (n <= z && selfLost) {
                        if (n <= x) {
                            // 只减第 1 堆 / 同减 1&2 / 同减 1&3 / 三堆同减
                            checks += 4
                            if (lostAt(x - n, y, z)) selfLost = false
                            else if (lostAt(x - n, y - n, z)) selfLost = false
                            else if (lostAt(x - n, y, z - n)) selfLost = false
                            else if (lostAt(x - n, y - n, z - n)) selfLost = false
                        }
                        if (selfLost && n <= y) {
                            // 只减第 2 堆 / 同减 2&3
                            checks += 2
                            if (lostAt(x, y - n, z)) selfLost = false
                            else if (lostAt(x, y - n, z - n)) selfLost = false
                        }
                        // 只减第 3 堆
                        if (selfLost) {
                            checks++
                            if (lostAt(x, y, z - n)) selfLost = false
                        }
                        n++
                    }
                    if (selfLost) {
                        lost[(x * sz + y) * sz + z] = true
                        sum += x + y + z
                        count++
                    }
                }
                y++
            }
            x++
        }
    }
    return Small(sum, count, states, checks)
}

/** JIT 预热后 3 轮取最优。 */
private fun bestOf3(tag: String, f: () -> Small): Pair<Double, Small> {
    var best = Double.MAX_VALUE
    var out: Small? = null
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val r = f()
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) { best = ms; out = r }
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best to out!!
}

fun main() {
    val anchor = brute(100)
    check(anchor.sum == 173895L) { "Z=100 应得 173895，实得 ${anchor.sum}" }
    println(
        "Z=100：Σ = ${anchor.sum}（局面 ${anchor.count} 个，状态 ${anchor.states} 个，" +
            "后继检查 ${anchor.successorChecks} 次），与题面 173895 一致",
    )

    // 预热 JIT
    brute(100)
    brute(150)

    val (ms200, r200) = bestOf3("直接暴力 Z=200") { brute(200) }
    val (ms250, r250) = bestOf3("直接暴力 Z=250") { brute(250) }

    println()
    println("Z=200：Σ = ${r200.sum}（局面 ${r200.count} 个，后继检查 ${r200.successorChecks} 次），最优 ${"%.1f".format(ms200)} ms")
    println("Z=250：Σ = ${r250.sum}（局面 ${r250.count} 个，后继检查 ${r250.successorChecks} 次），最优 ${"%.1f".format(ms250)} ms")
    println()
    println("bruteForceBaselineMs 取 Z=250 的 ${"%.1f".format(ms250)} ms")
}
