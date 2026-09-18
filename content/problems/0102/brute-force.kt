/**
 * Project Euler 102 — 暴力解（Gram 矩阵重心坐标，教学对比用）
 *
 * 思路：令 a=B−A、b=C−A、p=−A，解 p=u·a+v·b。对 a、b 分别取点积得到 Gram 方程组，
 * 用克拉默法则计算 u、v 的分子：分母 D=(a·a)(b·b)−(a·b)²、U=(b·b)(a·p)−(a·b)(b·p)、
 * V=(a·a)(b·p)−(a·b)(a·p)。非退化三角形 D>0，严格内含等价于 U>0、V>0、U+V<D，
 * 全程 Long 精确整数比较，不做除法，也不使用叉积判号。
 * 复杂度：O(N) 时间、O(1) 额外空间；每行 16 次乘法，比三行列式同号更贵。
 * 需从仓库根目录、server 或题目目录运行（读取 triangles.txt）。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun containsOriginByBarycentric(t: LongArray): Boolean {
    val ax = t[2] - t[0]
    val ay = t[3] - t[1]
    val bx = t[4] - t[0]
    val by = t[5] - t[1]
    val px = -t[0]
    val py = -t[1]
    val aa = ax * ax + ay * ay
    val bb = bx * bx + by * by
    val ab = ax * bx + ay * by
    val ap = ax * px + ay * py
    val bp = bx * px + by * py
    val denominator = aa * bb - ab * ab
    val u = bb * ap - ab * bp
    val v = aa * bp - ab * ap
    return denominator > 0 && u > 0 && v > 0 && u + v < denominator
}

fun solveBruteForce(): Long {
    val file = listOf("content/problems/0102/triangles.txt", "triangles.txt", "../content/problems/0102/triangles.txt")
        .map { java.io.File(it) }.firstOrNull { it.isFile }
        ?: error("找不到 triangles.txt，请在仓库根目录、server 或题目目录运行")
    var rows = 0
    var count = 0L
    file.useLines { lines ->
        lines.forEach { line ->
            val t = line.split(',').map { it.trim().toLong() }.toLongArray()
            require(t.size == 6 && t.all { it in -1000L..1000L }) { "无效三角形坐标：$line" }
            if (containsOriginByBarycentric(t)) count++
            rows++
        }
    }
    check(rows == 1000) { "应有 1000 个三角形，实际 $rows" }
    return count
}

fun main() {
    check(containsOriginByBarycentric(longArrayOf(-340, 495, -153, -910, 835, -947)))
    check(!containsOriginByBarycentric(longArrayOf(-175, 41, -421, -714, 574, -645)))
    check(!containsOriginByBarycentric(longArrayOf(-1, 0, 1, 0, 0, 1)))   // 原点落在边上，非内部
    check(!containsOriginByBarycentric(longArrayOf(-1, 0, 0, 0, 1, 0)))   // 三点共线，退化
    println(solveBruteForce())
}
