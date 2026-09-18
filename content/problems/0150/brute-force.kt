/**
 * Project Euler 150 — Sub-triangle Sums（求最小子三角形和）· 暴力对照解
 *
 * 与 solution.kt 的思路差别：
 *   · 优化解沿对角线把三角形「劈成两半」，用跨顶点的分层 DP：
 *     T(r,c,h) = T(r+1,c,h-1) + D(r,c,h)，每个状态靠下一行同列顶点的高 h-1 结果推进，
 *     预算好的对角线布局 + 层缓冲全是连续内存。
 *   · 本解只按定义从顶点出发一层层「加行」：固定顶点 (r,c) 后，
 *     T(r,c,h+1) = T(r,c,h) + (第 r+h 行第 c..c+h 列的行段和)，
 *     行段和由行前缀数组现取现减。不同顶点之间没有任何复用（不给下一行留状态），
 *     每层的第二次读取落在前缀数组的斜向/竖直位置，访存是跳跃的——这正是两者耗时差距的来源。
 *
 * 复杂度：候选子三角形共 Σ_r (r+1)(n-r) = n(n+1)(n+2)/6 = 167167000 个，
 * 每个 2 次前缀查表 + 2 次加法，时间同为 O(n³)，但常数更大；
 * 空间只有 O(n²) 的行前缀数组（优化解另有 O(n²) 的层缓冲）。
 *
 * 样例断言：题面前三项随机数；题面图示六行小三角形的最小和 −42；
 * 与逐元素定义式在 n = 64 上交叉验证。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

const val N = 1000

private fun off(r: Int): Int = r * (r + 1) / 2

/** 题面 LCG：t ← (615949 t + 797807) mod 2²⁰，s_k = t − 2¹⁹，按行主序铺成三角数组。 */
fun generate(n: Int): LongArray {
    val a = LongArray(n * (n + 1) / 2)
    var t = 0L
    for (r in 0 until n) {
        for (c in 0..r) {
            t = (615949L * t + 797807L) % (1L shl 20)
            a[off(r) + c] = t - (1L shl 19)
        }
    }
    return a
}

/** 每行的前缀和：pre[off(r)+c] = a(r,0) + ... + a(r,c)。 */
fun rowPrefix(a: LongArray, n: Int): LongArray {
    val pre = LongArray(a.size)
    for (r in 0 until n) {
        val base = off(r)
        var s = 0L
        for (c in 0..r) {
            s += a[base + c]
            pre[base + c] = s
        }
    }
    return pre
}

/**
 * 暴力：逐顶点向下加行。顶点 (r,c) 已算出高 h 的三角形和 tri 后，
 * 再补上第 r+h 行第 c..c+h 列的行段和即得高 h+1 的结果。
 */
fun minSubTriangleBrute(a: LongArray, n: Int): Long {
    val pre = rowPrefix(a, n)
    var best = Long.MAX_VALUE
    for (r in 0 until n) {
        for (c in 0..r) {
            var tri = 0L
            var h = 0
            while (h < n - r) {
                val base = off(r + h)
                var seg = pre[base + c + h]                      // 行段右端
                if (c > 0) seg -= pre[base + c - 1]              // 减去左端之前
                tri += seg
                if (tri < best) best = tri
                h++
            }
        }
    }
    return best
}

fun solveBruteForce(n: Int = N): Long = minSubTriangleBrute(generate(n), n)

/** 定义式暴力：把每个三角形里的元素逐个相加。只用于小规模样例交叉验证。 */
fun minSubTriangleDefinitional(a: LongArray, n: Int): Long {
    var best = Long.MAX_VALUE
    for (r in 0 until n) {
        for (c in 0..r) {
            for (h in 1..n - r) {
                var s = 0L
                for (i in 0 until h) for (j in 0..i) s += a[off(r + i) + c + j]
                if (s < best) best = s
            }
        }
    }
    return best
}

fun verifySample() {
    val a = generate(N)

    // 题面给出的前三项随机数：s1 = 273519, s2 = -153582, s3 = 450905
    check(a[0] == 273519L) { "s1 应为 273519，实际 ${a[0]}" }
    check(a[1] == -153582L) { "s2 应为 -153582，实际 ${a[1]}" }
    check(a[2] == 450905L) { "s3 应为 450905，实际 ${a[2]}" }

    // 题面示例（图示六行小三角形）：标注的最小子三角形和为 −42
    val rows = arrayOf(
        longArrayOf(15),
        longArrayOf(-14, -7),
        longArrayOf(20, -13, -5),
        longArrayOf(-3, 8, 23, -26),
        longArrayOf(1, -4, -5, -18, 5),
        longArrayOf(-16, 31, 2, 9, 28, 3),
    )
    val flat = LongArray(21)
    for (r in rows.indices) for (c in rows[r].indices) flat[off(r) + c] = rows[r][c]
    check(minSubTriangleDefinitional(flat, 6) == -42L) { "题面示例的逐元素最小和应为 −42" }
    check(minSubTriangleBrute(flat, 6) == -42L) { "暴力解在示例上应复现 −42" }

    // 与定义式逐元素求和交叉验证：n = 64
    val small = generate(64)
    val direct = minSubTriangleDefinitional(small, 64)
    check(minSubTriangleBrute(small, 64) == direct) { "n=64：定义式 $direct vs 暴力 ${minSubTriangleBrute(small, 64)}" }
}

fun main() {
    verifySample()
    repeat(3) { solveBruteForce() }              // JIT 预热
    val start = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
