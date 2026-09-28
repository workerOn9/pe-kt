#!/usr/bin/env kotlin
/**
 * Project Euler 247 — Squares Under a Hyperbola（双曲线下的正方形）
 *
 * 思路：
 *   把曲线下方还没被占用的「空位」记成 gap(L, B) = {(x, y) : x ≥ L, y ≥ B, y ≤ 1/x}。
 *   它里面能放下的最大正方形，右上角恰好落在曲线上，边长 s 由 (L+s)(B+s) = 1 解出：
 *
 *       s(L, B) = ( −(L+B) + √((L+B)² + 4(1 − L·B)) ) / 2 。
 *
 *   放上这个正方形后，空位分裂成两个子空位——正方形右侧的 (L+s, B) 与正方形上方的
 *   (L, B+s)；两者互不相交（只共一个落在曲线上的点），所以整个填充过程是一棵二叉树：
 *   每个节点（空位）放一个正方形、裂成两个孩子，且孩子边长必严格小于父亲（子空位是父
 *   空位的一部分）。贪心「每次取现存空位中最大的那个」= 按边长从大到小给这棵树编 S 号。
 *
 *   索引的精确含义就是祖先链上的方向计数：
 *       index(S_n) = (链上 R 的个数, 链上 T 的个数)，
 *   其中 R 是「父正方形在左边」的右侧子空位、T 是「父正方形在下面」的上方子空位。
 *   题面给的三条事实恰好钉死这个解释：
 *     · 路径 R 的节点是第 2 个（S_2 索引 (1,0)）；
 *     · 路径 TR、RT 的节点分别是第 32、50 个（S_32、S_50 索引 (1,1)）；
 *     · 长度 2、一 R 一 T 的路径只有 C(2,1) = 2 条，(1,1) 的节点总数就是 2，最晚入列的
 *       是 RT = 第 50 个——「50 是 (1,1) 的最大 n」正是判别模型对错的关键。
 *
 *   于是问题归约成：3 个 R、3 个 T 的路径共 C(6,3) = 20 条，求其中**最后入列**的一条。
 *   贪心按边长递减入列、边长又沿树严格递减，所以它就是 20 条候选里边长最小的那条。
 *   实测最小者是 RRRTTT：
 *
 *       s(RRRTTT) = 8.013765943111907859…×10⁻⁴，
 *
 *   次小者 RRTRTT 是它的 1.968 倍，隔离得非常干净。
 *
 *   最后数「边长严格大于这条」的节点数：对二叉树做 DFS，节点边长 ≤ 阈值时整棵子树都可
 *   剪掉（后代只会更小）。计数 = 782251，所以答案 n = 782251 + 1 = 782252——目标前面
 *   正好排着 782251 个更大的正方形。
 *
 * 精度：
 *   阈值两侧最近的节点相对距离 ≥ 1.5×10⁻⁷，double 的累积误差在 10⁻¹⁵ 量级；main 里再跑
 *   一道 ±10⁻⁹ 的「隔离带」自检——带宽内只圈进目标一个节点才算数；目标边长另用 40 位
 *   BigDecimal 复核（实测相对误差 9.6×10⁻¹⁵）。
 *
 * 旁证：
 *   1. brute-force.kt 完全不数阈值：把贪心过程用大顶堆逐个模拟出来，放下 78 万个正方形，
 *      报出第 20 个 (3,3) 正方形的序号 782252；题面事实（第 2 个是 (1,0)，第 32、50 个
 *      是 (1,1)，(1,1) 全程恰好 2 次）也一并复现。
 *   2. 题图 figure-squares.gif 逐像素核对：1..5 号边长 0.618/0.477/0.401/0.351/0.316，
 *      12 号 0.209、32 号 0.129、75 号 0.085、50 号 0.104、88 号 0.078，全对得上。
 *   3. 自检④：S_1 正上方那块是路径 T、第 12 个节点，与题图编号互证。
 *
 * 答案：782252
 * 复杂度：O(A + P)，A = 782251（边长超过阈值的节点数），P 为被剪掉的子节点数（同量级）；
 *         空间 O(DFS 栈深)（实测栈深只有个位数）。单次完整求解见 main 实测输出。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

import java.math.BigDecimal
import java.math.MathContext

private val MC = MathContext(40)

/**
 * 空位 (L, B) 里最大正方形的边长：满足 (L+s)(B+s) = 1，即 s = (−(L+B) + √((L+B)² + 4(1−LB))) / 2。
 * 直接这样算在 L、B 很大时会灾难性抵消（√(u²+4c) − u，差的量级只有 1/u），改用有理化等价式
 *
 *     s = 2(1 − LB) / ( √((L+B)² + 4(1−LB)) + (L+B) )，
 *
 * 分子分母都不再相消，深链上相对误差从 ~10⁻¹⁰ 降到 ~10⁻¹⁵。空位必满足 L·B < 1（曲线穿过角点）。
 */
private fun sideOf(l: Double, b: Double): Double {
    val c = 1.0 - l * b
    if (c <= 0.0) return 0.0
    val u = l + b
    return 2.0 * c / (Math.sqrt(u * u + 4.0 * c) + u)
}

/** 沿路径走到末端的空位，返回 [L, B, s]；路径是 'R'（右侧子空位）与 'T'（上方子空位）的串。 */
private fun gapAt(path: String): DoubleArray {
    var l = 1.0
    var b = 0.0
    for (ch in path) {
        val s = sideOf(l, b)
        if (ch == 'R') l += s else b += s
    }
    return doubleArrayOf(l, b, sideOf(l, b))
}

/**
 * 数边长严格大于阈值 th 的节点个数。DFS + 剪枝：子空位是父空位的一部分，
 * 边长必严格小于父亲，所以节点边长 ≤ th 时整棵子树都可以丢掉。
 */
private fun countAbove(th: Double): Long {
    var stackL = DoubleArray(64)
    var stackB = DoubleArray(64)
    var top = 0
    stackL[top] = 1.0
    stackB[top] = 0.0
    top++
    var count = 0L
    while (top > 0) {
        top--
        val l = stackL[top]
        val b = stackB[top]
        val s = sideOf(l, b)
        if (s <= th) continue
        count++
        if (top + 2 > stackL.size) {
            stackL = stackL.copyOf(stackL.size * 2)
            stackB = stackB.copyOf(stackB.size * 2)
        }
        stackL[top] = l + s; stackB[top] = b; top++        // R 子空位
        stackL[top] = l; stackB[top] = b + s; top++        // T 子空位
    }
    return count
}

/** 20 条「3 个 R + 3 个 T」的候选路径，按末端边长升序（升序第一 = 贪心入列最晚）。 */
private val candidates33: List<Pair<String, Double>> by lazy {
    (0 until 64)
        .filter { Integer.bitCount(it) == 3 }
        .map { mask ->
            val path = buildString { for (i in 0 until 6) append(if (mask shr i and 1 == 1) 'R' else 'T') }
            path to gapAt(path)[2]
        }
        .sortedBy { it.second }
}

/** 完整求解：定位候选 → 剪枝计数 → 返回最大 n。 */
private fun solve(): Long {
    val (minPath, minSide) = candidates33[0]
    check(minPath == "RRRTTT") { "(3,3) 候选最小者应为 RRRTTT，实际 $minPath" }
    check(candidates33[1].second > 1.5 * minSide) { "候选隔离度不足" }
    return countAbove(minSide) + 1
}

/** 精度自检（求解前跑一次）：相对宽 ±10⁻⁹ 的隔离带里只允许目标一个节点，double 定序才可靠。 */
private fun checkIsolation() {
    val s = candidates33[0].second
    val bandHigh = countAbove(s * (1 + 1e-9))
    val bandLow = countAbove(s * (1 - 1e-9))
    check(bandLow - bandHigh == 1L) { "阈值附近有 ${bandLow - bandHigh} 个节点，double 精度不足以定序" }
    check(countAbove(s) == bandHigh) { "精确阈值计数与带宽计数不一致" }
}

/** 40 位精度算一步边长（复核 double 结果用）。 */
private fun sideHighPrecision(l: BigDecimal, b: BigDecimal): BigDecimal {
    val u = l + b
    val disc = u.multiply(u) + BigDecimal(4) * (BigDecimal.ONE - l.multiply(b))
    return disc.sqrt(MC).subtract(u).divide(BigDecimal(2), MC)
}

/** 40 位精度重算某条路径末端的边长。 */
private fun highPrecisionSide(path: String): BigDecimal {
    var l = BigDecimal.ONE
    var b = BigDecimal.ZERO
    for (ch in path) {
        val s = sideHighPrecision(l, b)
        if (ch == 'R') l += s else b += s
    }
    return sideHighPrecision(l, b)
}

private fun main() {
    // ---- 题面给定事实自检 ----
    check(countAbove(gapAt("R")[2]) == 1L)
    println("自检①：路径 R 是第 2 个节点 = S_2，索引 (left, below) = (1,0) ✓")
    check(countAbove(gapAt("TR")[2]) == 31L)
    println("自检②：路径 TR 是第 32 个节点 = S_{32}，索引 (1,1) ✓")
    check(countAbove(gapAt("RT")[2]) == 49L)
    println("自检③：路径 RT 是第 50 个节点 = S_{50}，索引 (1,1) ✓")
    println("        索引 (1,1) 的路径只有 RT、TR 两条，故 (1,1) 的最大 n = 50 ✓")
    check(countAbove(gapAt("T")[2]) == 11L)
    println("自检④：路径 T（S_1 正上方那块）是第 12 个节点，与题图编号一致 ✓")

    // ---- 目标边长的高精度复核 ----
    val minSide = candidates33[0].second
    val hp = highPrecisionSide("RRRTTT").toDouble()
    println(
        "自检⑤：RRRTTT 边长 double = %.16e，40 位精度 = %.16e，相对误差 %.2e ✓"
            .format(minSide, hp, Math.abs(minSide - hp) / hp)
    )
    val (secondPath, secondSide) = candidates33[1]
    println(
        "候选概览：(3,3) 共 ${candidates33.size} 条；最小 RRRTTT = %.16e，次小 $secondPath = %.16e（相差 %.3f 倍）"
            .format(minSide, secondSide, secondSide / minSide)
    )

    // ---- 求解 + JIT 预热后计时 ----
    checkIsolation()
    println("自检⑥：阈值相对带宽 ±10⁻⁹ 的隔离带内只有目标一个节点，double 定序可证 ✓")
    val first = solve()
    check(first == 782252L) { "答案不吻合：$first" }
    val t0 = System.nanoTime()
    val answer = solve()
    val ms = (System.nanoTime() - t0) / 1e6
    check(answer == first) { "计时循环结果漂移" }
    println("答案 = $answer")
    println("optimized: %.1f ms/次（JIT 预热后；含 20 条候选与 1 次剪枝 DFS 计数）".format(ms))
    println("check() 全部通过")
}
