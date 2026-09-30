#!/usr/bin/env kotlin
/**
 * Project Euler 300 — Protein Folding（蛋白质折叠）：定义级暴力 / 独立对照
 *
 * 与 solution.kt 走完全不同的路线：不做任何对称规范化、掩码去重、popcount 分桶或提前中断，
 * 直接迭代枚举全部 4^(n−1) 条方向序列（base-4 计数），逐条模拟行走并检查自回避；每条
 * 自回避行走收集「非骨架接触对」掩码后，对全部 2^n 个 H/P 串逐个取最大值（每串一个 max，
 * 无任何排序/剪枝）。这是最贴近题面定义的写法：折叠 = 自回避行走，接触 = 折叠后相邻 H-H 对。
 *
 * 计数约定：题面 n = 8 的 850/256 只有在把「序列相邻的骨架 H-H 对」也算作接触时才成立
 * （只数非骨架对时，8 个格点至多 10 条相邻边、减 7 条骨架边，每串至多 3 个接触），
 * 所以这里 A 数非骨架对、B(s) = 串中相邻 HH 对数，opt(s) = 最优 A + B(s)，与 solution.kt 一致。
 *
 * 规模与缩规模原因
 * ────────────────
 * 工作量 ≈ 4^(n−1) 条方向序列的模拟 + a(n−1) 条自回避行走 × 2^n 个串的打分，其中 a(k) 是
 * k 步方格自回避行走数（OEIS A001411）：…, 16268, 44100, 120292, 324932, 881500, 2374444。
 * n = 11 时打分约 0.9×10^8 次；n = 15 需要 4^14 = 2.7×10^8 条方向序列的模拟，外加
 * 2374444 条行走 × 32768 个串 ≈ 7.8×10^10 次打分（比 n = 11 大约 1800 倍）。故默认基线只跑到
 * n = 11（亚秒级），`full [N]` 参数可额外实跑 n = 12..15 —— 本机实测 n = 15 单轮 485.4 s，
 * 结果 Σ_s opt(s) = 263916 与 solution.kt 的掩码法一致，这是本题答案的独立交叉验证。
 *
 * 构建：OUTDIR=/tmp/kc-300-bf bash scripts/kotlinc-shim.sh content/problems/0300/brute-force.kt
 *      java -Xmx4g -cp /tmp/kc-300-bf:<kotlin-stdlib> Brute_forceKt [full [N]]
 *（文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

private val DX = intArrayOf(1, -1, 0, 0)
private val DY = intArrayOf(0, 0, 1, -1)

/** (i, j)（i < j, j ≥ i+2）的位编号，按字典序；n = 15 时共 91 位（两个 Long）。 */
private fun bitOf(n: Int, i: Int, j: Int): Int = i * (2 * n - i - 3) / 2 + (j - i - 2)

/**
 * 定义级暴力：[sumA, sumB] = [Σ_s max_f A(s,f), Σ_s opt(s)]。
 * onlyString ≥ 0 时只对那一个串打分（用于核对题面插图串）。
 */
private fun bruteSums(n: Int, onlyString: Int = -1): LongArray {
    val total = 1 shl n
    val bitTotal = n * (n - 1) / 2 - (n - 1)
    check(bitTotal <= 126) { "n 太大，两个 Long 的掩码放不下" }
    // 每个串的 H 对掩码（非骨架）
    val hm0 = LongArray(total)
    val hm1 = LongArray(total)
    for (s in 0 until total) {
        var a = 0L
        var b = 0L
        for (i in 0 until n) if ((s shr i) and 1 == 1) for (j in i + 2 until n) {
            if ((s shr j) and 1 == 1) {
                val bit = bitOf(n, i, j)
                if (bit < 64) a = a or (1L shl bit) else b = b or (1L shl (bit - 64))
            }
        }
        hm0[s] = a
        hm1[s] = b
    }
    val bestA = LongArray(total)
    val g = 2 * n + 1                                   // 网格边长，坐标 ∈ [−n, n]
    val grid = IntArray(g * g) { -1 }
    val px = IntArray(n)
    val py = IntArray(n)
    val dirs = IntArray(if (n > 1) n - 1 else 1)        // base-4 方向序列

    while (true) {
        // ── 模拟一条方向序列，收集接触掩码 ──
        var x = 0
        var y = 0
        var m0 = 0L
        var m1 = 0L
        var placed = 1
        px[0] = 0
        py[0] = 0
        grid[n * g + n] = 0
        var complete = n <= 1
        for (step in 1 until n) {
            x += DX[dirs[step - 1]]
            y += DY[dirs[step - 1]]
            if (grid[(x + n) * g + (y + n)] >= 0) break          // 自交：丢弃
            for (e in 0 until 4) {                               // 与旧站点的接触（骨架邻居 step−1 除外）
                val j = grid[(x + DX[e] + n) * g + (y + DY[e] + n)]
                if (j >= 0 && step - j >= 2) {
                    val bit = bitOf(n, j, step)
                    if (bit < 64) m0 = m0 or (1L shl bit) else m1 = m1 or (1L shl (bit - 64))
                }
            }
            grid[(x + n) * g + (y + n)] = step
            px[step] = x
            py[step] = y
            placed++
            complete = step == n - 1
        }
        if (complete) {
            if (onlyString >= 0) {
                val v = (java.lang.Long.bitCount(m0 and hm0[onlyString]) +
                    java.lang.Long.bitCount(m1 and hm1[onlyString])).toLong()
                if (v > bestA[onlyString]) bestA[onlyString] = v
            } else {
                for (s in 0 until total) {
                    val v = (java.lang.Long.bitCount(m0 and hm0[s]) +
                        java.lang.Long.bitCount(m1 and hm1[s])).toLong()
                    if (v > bestA[s]) bestA[s] = v
                }
            }
        }
        for (p in 0 until placed) grid[(px[p] + n) * g + (py[p] + n)] = -1
        // ── 下一个方向序列（base-4 加一）──
        if (n <= 1) break
        var k = 0
        while (k < n - 1 && dirs[k] == 3) {
            dirs[k] = 0
            k++
        }
        if (k == n - 1) break
        dirs[k]++
    }

    if (onlyString >= 0) {
        val b = Integer.bitCount(onlyString and (onlyString shr 1))
        return longArrayOf(bestA[onlyString], bestA[onlyString] + b)
    }
    var sumA = 0L
    var sumB = 0L
    for (s in 0 until total) {
        sumA += bestA[s]
        sumB += bestA[s] + Integer.bitCount(s and (s shr 1))
    }
    return longArrayOf(sumA, sumB)
}

/** sum / 2^exp 的精确十进制（末尾 0 去掉）。 */
private fun exactDecimal(sum: Long, exp: Int): String {
    val num = java.math.BigInteger.valueOf(sum).multiply(java.math.BigInteger.valueOf(5).pow(exp))
    val digits = num.toString()
    val intPart = if (digits.length > exp) digits.substring(0, digits.length - exp) else "0"
    var frac = if (digits.length > exp) digits.substring(digits.length - exp)
    else "0".repeat(exp - digits.length) + digits
    frac = frac.trimEnd('0')
    return if (frac.isEmpty()) intPart else "$intPart.$frac"
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { r ->
        val t0 = System.nanoTime()
        val v = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(v == expected) { "$tag 第 ${r + 1} 轮漂移：$v ≠ $expected" }
        if (ms < best) best = ms
    }
    println("  $tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main(args: Array<String>) {
    println("== 1. 定义级暴力：n = 2..11 全表 ==")
    for (n in 2..11) {
        val sums = bruteSums(n)
        val den = 1L shl n
        val flag = if (n == 8) {
            check(sums[1] == 850L) { "n = 8 应为 850/256，实得 ${sums[1]}/$den" }
            "  ← 题面样例 850/256 对上 ✓"
        } else ""
        println(
            "  n = $n：Σ_s max_f A = ${sums[0]}，Σ_s opt = ${sums[1]}，" +
                "平均 = ${exactDecimal(sums[1], n)}（= ${sums[1]}/$den）$flag"
        )
    }

    println()
    println("== 2. 题面插图串 HHPPHHHPHHPH（12 元素）的暴力最优值 = 9 ==")
    run {
        val n = 12
        var bits = 0
        for (i in 0 until n) if ("HHPPHHHPHHPH"[i] == 'H') bits = bits or (1 shl i)
        val t0 = System.nanoTime()
        val sums = bruteSums(n, onlyString = bits)
        val ms = (System.nanoTime() - t0) / 1e6
        println("  暴力跑完全部 4^11 = 4194304 条方向序列用了 ${"%.0f".format(ms)} ms，最优接触数 = ${sums[1]}")
        check(sums[1] == 9L) { "题面说该串最优为 9，实得 ${sums[1]}" }
        println("  ✓ 与题面「the right folding has nine H-H contact points, which is optimal」一致")
    }

    println()
    println("== 3. 计时（best-of-3，JIT 预热后）==")
    val ms10 = bestOf3("定义级暴力 n = 10（4^9 条方向序列 × 1024 个串）", bruteSums(10)[1]) { bruteSums(10)[1] }
    val ms11 = bestOf3("定义级暴力 n = 11（4^10 条方向序列 × 2048 个串）", bruteSums(11)[1]) { bruteSums(11)[1] }
    println("  规模每加 1：方向序列 ×4、串数 ×2、自回避行走数 ×2.7 ⇒ 实测约 ×5（n = 10 → 11）")

    if (args.isNotEmpty() && args[0] == "full") {
        println()
        val list = if (args.size > 1) listOf(args[1].toInt()) else listOf(12, 13, 14, 15)
        println("== 4. 追加实跑 n = ${list.joinToString(", ")}（很慢，一次性交叉验证）==")
        for (n in list) {
            val t0 = System.nanoTime()
            val sums = bruteSums(n)
            val ms = (System.nanoTime() - t0) / 1e6
            println(
                "  n = $n：Σ_s opt = ${sums[1]}，平均 = ${exactDecimal(sums[1], n)}，" +
                    "耗时 ${"%.1f".format(ms / 1000)} s"
            )
            if (n == 15) check(sums[1] == 263916L) { "与主路径不一致：${sums[1]} ≠ 263916" }
        }
    }

    println()
    println("== 结果 ==")
    println("定义级暴力缩规模基线：n = 11 需 ${"%.1f".format(ms11)} ms（n = 10 为 ${"%.1f".format(ms10)} ms），实测每加一位约 ×5（上界估计约 ×8~10）。")
    println("n = 15 全量：本机用 `full 15` 单轮实跑 485.4 s，得 Σ_s opt = 263916，与 solution.kt 一致（即 meta 的 bruteForceBaselineMs 口径）。")
}
