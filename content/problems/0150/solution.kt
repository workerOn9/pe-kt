/**
 * Project Euler 150 — Sub-triangle Sums（子三角形求和）
 *
 * 思路：设 a(r,c) 为第 r 行（0 起）第 c 个元素，行 r 恰有 r+1 个元素，题面下标
 * k = r(r+1)/2 + c + 1 对应 s_k。顶点 (r,c)、高 h 的子三角形取
 *     第 r+i 行第 c .. c+i 列（i = 0 .. h-1），共 h(h+1)/2 个元素。
 *
 * 把它沿对角线劈成两半：
 *     · 对角线部分 D(r,c,h) = a(r,c) + a(r+1,c+1) + ... + a(r+h-1,c+h-1)
 *     · 其余部分 {(r+i, c+j) : j < i}：令 i' = i-1、j' = j，正是「顶点 (r+1,c)、高 h-1」
 *       的三角形 {(r+1+i', c+j') : j' <= i' <= h-2}
 * 于是核心递推是（层间传递的是「对角线」而不是行段）：
 *     T(r,c,h) = T(r+1,c,h-1) + D(r,c,h),   D(r,c,h) = D(r,c,h-1) + a(r+h-1, c+h-1)
 * 逐行自底向上分层 DP：处理第 r 行时，第 r+1 行所有顶点的「按高度向量」还留在缓冲里，
 * 每个 (r,c,h) 只要「对角线累加一个元素 + 接上下一行高 h-1 的结果」两次加法。
 * 第 r 行的层规模是 (r+1)(n-r)，n = 1000 时最大 250500 个 Long（r ≈ n/2 处），两份缓冲互换复用。
 *
 * 布局：每个顶点的对角线累加走的都是「行列差 r-c 恒定」的右斜对角线。把原数组按对角线
 * 连续存放（对角线 dd 的第 p 项 = 元素 (dd+p, p)），累加便是顺序读；层缓冲也是顺序读写，
 * 于是内层循环的 3 处内存访问全部连续。
 *
 * 复杂度：候选子三角形共 Σ_r (r+1)(n-r) = n(n+1)(n+2)/6 = 167167000 个（n = 1000），
 * 每个恰好访问一次、常数时间，时间 O(n³)（精确 1.67×10⁸ 次迭代、每次 2 次加法），
 * 空间 O(n²)（原数组 + 两份层缓冲）。整三角和最大可达 500500 × 2¹⁹ ≈ 2.6×10¹¹，
 * 中间量必须用 Long（单元素只在 ±2¹⁹ 内，但和的累加会溢出 Int）。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

const val N = 1000
const val TOTAL = 500500                 // N(N+1)/2，题面给出的伪随机数个数

/** 行 r 起始下标（行主序三角数组）。 */
private fun off(r: Int): Int = r * (r + 1) / 2

/** 对角线 dd 的起始下标：前面 dd 条对角线依次长 n, n-1, ..., n-dd+1。 */
private fun diagOff(n: Int, dd: Int): Int = dd * n - dd * (dd - 1) / 2

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

/** 行主序三角数组 → 对角线连续布局：元素 (r,c) 落在对角线 r-c 的第 c 个位置。 */
private fun toDiagonalMajor(a: LongArray, n: Int): LongArray {
    val d = LongArray(a.size)
    for (r in 0 until n) {
        for (c in 0..r) d[diagOff(n, r - c) + c] = a[off(r) + c]
    }
    return d
}

/**
 * 优化解：分层 DP。层缓冲按 [顶点列 c × stride + 高度下标 m] 排布（m = 高 − 1），
 * stride 为本层的高度个数（第 r 行是 n-r）。处理完第 r 行后两份缓冲互换。
 */
fun minSubTriangle(a: LongArray, n: Int): Long {
    val d = toDiagonalMajor(a, n)
    val maxLayer = (n + 1) * (n + 1) / 4 + n + 4
    var prev = LongArray(maxLayer)            // 第 r+1 行的层
    var cur = LongArray(maxLayer)             // 第 r 行的层
    var stridePrev = 0                        // 最后一行之下没有层
    var best = Long.MAX_VALUE
    for (r in n - 1 downTo 0) {
        val strideCur = n - r                 // 顶点在第 r 行时，高度可取 1 .. n-r
        for (c in 0..r) {
            val curBase = c * strideCur
            val prevBase = c * stridePrev
            var i = diagOff(n, r - c) + c     // 对角线 r-c 上第 c 项，即元素 (r,c)
            var diag = d[i]                   // 高 1：只剩对角线上的自己
            cur[curBase] = diag
            if (diag < best) best = diag
            i++
            for (m in 1 until strideCur) {
                diag += d[i]                                  // D(r,c,m+1)
                val v = diag + prev[prevBase + m - 1]         // + T(r+1,c,m)
                cur[curBase + m] = v
                if (v < best) best = v
                i++
            }
        }
        val tmp = prev; prev = cur; cur = tmp
        stridePrev = strideCur
    }
    return best
}

fun solve(n: Int = N): Long = minSubTriangle(generate(n), n)

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

    // 取值范围 ±2¹⁹、总个数 500500（题面：k 从 1 到 500500）
    check(a.size == TOTAL) { "元素个数应为 500500，实际 ${a.size}" }
    check(a.min() >= -524288L && a.max() <= 524287L) { "取值必须落在 ±2¹⁹ 内" }

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
    check(minSubTriangle(flat, 6) == -42L) { "分层 DP 在示例上应复现 −42" }

    // 与定义式逐元素求和交叉验证：n = 64 覆盖浅顶点、深层顶点、首末列等边界
    val small = generate(64)
    val direct = minSubTriangleDefinitional(small, 64)
    check(minSubTriangle(small, 64) == direct) { "n=64：定义式 $direct vs DP ${minSubTriangle(small, 64)}" }
}

fun main() {
    verifySample()
    repeat(3) { solve() }                        // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
