/**
 * Project Euler 018 — 暴力解（教学对比用）
 *
 * 递归枚举全部 2^14 = 16384 条路径，O(2^n) 指数时间。
 * 本题规模下勉强可行；同样的方法放到 067 题的 100 行三角形上（2^99 条路径）彻底不可行。
 */

const val TRIANGLE_BRUTE = """
75
95 64
17 47 82
18 35 87 10
20 04 82 47 65
19 01 23 75 03 34
88 02 77 73 07 63 67
99 65 04 28 06 16 70 92
41 41 26 56 83 40 80 70 33
41 48 72 33 47 32 37 16 94 29
53 71 44 65 25 43 91 52 97 51 14
70 11 33 28 77 73 17 78 39 68 17 57
91 71 52 38 17 14 91 43 58 50 27 29 48
63 66 04 68 89 53 67 30 73 16 69 87 40 31
04 62 98 27 23 09 70 98 73 93 38 53 60 04 23
"""

fun parseTriangleBrute(text: String): List<IntArray> =
    text.trim().lines().map { line -> line.trim().split(" ").map { it.toInt() }.toIntArray() }

/** 从 (row, col) 出发到底边的最大路径和（纯递归，无记忆化） */
fun maxPath(triangle: List<IntArray>, row: Int, col: Int): Long {
    if (row == triangle.size - 1) return triangle[row][col].toLong()
    val left = maxPath(triangle, row + 1, col)
    val right = maxPath(triangle, row + 1, col + 1)
    return triangle[row][col] + maxOf(left, right)
}

fun solveBruteForce(): Long {
    val triangle = parseTriangleBrute(TRIANGLE_BRUTE)
    return maxPath(triangle, 0, 0)
}

fun main() {
    println(solveBruteForce())
}
