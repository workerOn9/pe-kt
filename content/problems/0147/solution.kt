/**
 * Project Euler 147 — Rectangles in Cross-hatched Grids（交叉阴影网格中的矩形）
 *
 * 思路推导
 * --------
 * m×n 的交叉阴影网格即 m×n 个单位方格、每格再画上两条对角线。整幅图里的"墨迹直线"只有四族
 * （网格区域记为 R = [0,n] × [0,m]，取整数格点坐标）：
 *
 *     水平线 y = j（0 ≤ j ≤ m）、竖直线 x = i（0 ≤ i ≤ n）、
 *     斜率 +1 的线 y = x + c、斜率 −1 的线 y = −x + d。
 *
 * 斜线族的条数：c = j − i ∈ [−(n−1), m−1]，d = i + j + 1 ∈ [1, n+m−1]，各 m+n−1 条。
 *
 * 矩形的相邻边必须垂直，四族线之间垂直的组合只有 (水平, 竖直) 与 (斜率 +1, 斜率 −1)，于是
 *
 *     总数(m,n) = A(m,n) + T(m,n)
 *     A = 轴对齐矩形 = C(m+1,2)·C(n+1,2) = m(m+1)n(n+1)/4
 *     T = 45° 倾斜矩形
 *
 * **引理（墨迹就是直线与 R 的交）**：直线 y = x + c 只在 j − i = c 的方格 (i,j) 内与该格对角线
 * 重合，在其余方格内要么不相交、要么只碰一个格点；而 j − i = c 的方格沿对角方向依次共角，
 * 所以它们的对角线首尾相接成一条线段，恰为直线与 R 的交集。斜率 −1 与水平、竖直三族同理。
 *
 * 于是"图中存在某个矩形"⟺ 取 c1 < c2、d1 < d2 两条斜率 +1 线与两条斜率 −1 线，四交点全在 R 内
 * （此时每条边连接 R 内两点，而 R∩直线是线段，凸性保证整条边都在墨迹上）。交点为
 *
 *     P(c,d) = ((d − c)/2, (d + c)/2)，
 *
 * 可能是格点（c ≡ d mod 2）或格心（否则），两者都落在墨迹上，无需区分。四个交点 ∈ R ⟺
 *
 *     d1 ≥ c2,   d2 ≤ c1 + 2n,   d1 ≥ −c1,   d2 + c2 ≤ 2m。
 *
 * **计数**：令 a = c2 − c1 ≥ 1、b = d2 − d1 ≥ 1（倾斜矩形的两条边长为 a/√2、b/√2），
 * 再令 s = d1 + c1、t = d1 − c1。d1, c1 为整数 ⟺ s ≡ t (mod 2)，于是
 *
 *     T(m,n) = Σ_{a,b ≥ 1} #{(s,t) : s ≡ t (2), 0 ≤ s ≤ 2m − a − b, a ≤ t ≤ 2n − b}
 *
 * 记 S = 2m − a − b、tHi = 2n − b、N = tHi − a + 1（需 N ≥ 1，即 a + b ≤ 2n；S ≥ 0 即 a + b ≤ 2m）：
 *
 *     · a + b 偶 ⇒ S 偶：[0,S] 内偶数 S/2 + 1 个、奇数 S/2 个，贡献 N·S/2 + E（E = [a,tHi] 内偶数个数）
 *     · a + b 奇 ⇒ S 奇：[0,S] 内奇偶各 (S+1)/2 个，贡献 N·(S+1)/2
 *
 * 只需枚举 a + b ≤ 2·min(m,n)，每组常数时间，单个网格 O(min(m,n)²)。
 *
 * 最终答案 = Σ_{w=1..47} Σ_{h=1..43} [A(h,w) + T(h,w)]。
 *
 * 复杂度：单网格 O(min(m,n)²)，全题 O(W·H·min(W,H)²)——43×47 那个网格枚举 3655 组 (a,b)，
 * 2021 个网格合计 1382106 组，每组常数时间；空间 O(1)。全程 Long，不用 toString() 数位数之类的做法。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 轴对齐矩形数：从 m+1 条水平线选 2 条、n+1 条竖直线选 2 条。 */
fun axisAligned(rows: Int, cols: Int): Long {
    val h = rows.toLong() * (rows + 1) / 2
    val v = cols.toLong() * (cols + 1) / 2
    return h * v
}

/**
 * 45° 倾斜矩形数：按 (a,b) = 两条斜线族的间距枚举，配对 (s,t) 的合法取值个数。
 * rows 为竖直方向的格数 m，cols 为水平方向的格数 n。
 */
fun tilted(rows: Int, cols: Int): Long {
    val lim = 2 * minOf(rows, cols)          // a + b ≤ 2·min(m,n) 是两侧约束的交
    var total = 0L
    for (a in 1..lim) {
        val bMax = lim - a
        for (b in 1..bMax) {
            val sSpan = 2 * rows - a - b      // s ∈ [0, sSpan]
            val tHi = 2 * cols - b            // t ∈ [a, tHi]
            val cnt = (tHi - a + 1).toLong()  // t 的取值个数
            total += if ((a + b) % 2 == 0) {
                val evens = (tHi / 2).toLong() - ((a - 1) / 2).toLong()   // [a,tHi] 内偶数个数
                cnt * (sSpan / 2) + evens
            } else {
                cnt * ((sSpan + 1) / 2)
            }
        }
    }
    return total
}

/** 一个 m×n（rows × cols）交叉阴影网格里的矩形总数。 */
fun rectanglesIn(rows: Int, cols: Int): Long = axisAligned(rows, cols) + tilted(rows, cols)

/** 所有 rows ≤ maxRows、cols ≤ maxCols 的网格里的矩形总数。 */
fun solve(maxRows: Int = 43, maxCols: Int = 47): Long {
    var total = 0L
    for (rows in 1..maxRows) {
        for (cols in 1..maxCols) total += rectanglesIn(rows, cols)
    }
    return total
}

fun verifySample() {
    // 题面：3×2 网格内 37 个矩形（2 行 3 列）
    check(rectanglesIn(2, 3) == 37L) { "3×2 应为 37，实得 ${rectanglesIn(2, 3)}" }
    // 题面：更小的五个网格（2×1 即 1 行 2 列，1×2 即 2 行 1 列）
    check(rectanglesIn(1, 1) == 1L) { "1×1 应为 1，实得 ${rectanglesIn(1, 1)}" }
    check(rectanglesIn(1, 2) == 4L) { "2×1 应为 4，实得 ${rectanglesIn(1, 2)}" }
    check(rectanglesIn(1, 3) == 8L) { "3×1 应为 8，实得 ${rectanglesIn(1, 3)}" }
    check(rectanglesIn(2, 1) == 4L) { "1×2 应为 4，实得 ${rectanglesIn(2, 1)}" }
    check(rectanglesIn(2, 2) == 18L) { "2×2 应为 18，实得 ${rectanglesIn(2, 2)}" }
    // 题面：3×2 及更小网格累计 72
    var cum = 0L
    for (rows in 1..2) for (cols in 1..3) cum += rectanglesIn(rows, cols)
    check(cum == 72L) { "3×2 及更小网格应累计 72，实得 $cum" }

    // 推导自检：轴对齐部分即组合数乘积
    check(axisAligned(2, 3) == 18L && tilted(2, 3) == 19L) { "3×2 应拆成 18 + 19" }
    // 推导自检：旋转 90° 不改变倾斜矩形数（tilted 应对称）；轴对齐数同样对称
    for (m in 1..12) {
        for (n in 1..12) {
            check(tilted(m, n) == tilted(n, m)) { "tilted($m,$n) ≠ tilted($n,$m)" }
            check(axisAligned(m, n) == axisAligned(n, m))
        }
    }
    // 推导自检：单行网格 1×n 里斜置方框只有"一格宽"这一种，倾斜数恰为 n−1
    for (n in 1..20) check(tilted(1, n) == (n - 1).toLong()) { "1×$n 的倾斜数应为 ${n - 1}" }
    // 推导自检：2×4（与 4×2）倾斜数为 29，与几何枚举给出的 29 一致
    check(tilted(2, 4) == 29L && tilted(4, 2) == 29L)
}

fun main(args: Array<String>) {
    verifySample()
    if (args.size == 2) {
        // 仅用于和 brute-force.kt 在缩减规模上对齐（brute-force 的几何枚举跑不动 47×43）
        println(solve(args[0].toInt(), args[1].toInt()))
        return
    }
    repeat(5) { solve() }                        // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
