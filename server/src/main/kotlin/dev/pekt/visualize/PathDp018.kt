package dev.pekt.visualize

import dev.pekt.content.ContentIndex
import java.io.File

/**
 * 018 路径 DP（kind=grid2d）：原题 15 行三角形。
 * 三角形数据直接解析 content/problems/0018/solution.kt 内嵌的 TRIANGLE 常量，
 * 保证可视化与真实求解同源（"看到的 = 真实执行的"）。
 *
 * codeLine 对应 0018/solution.kt：
 * 33 = `for (i in triangle.size - 2 downTo 0)`，36 = `dp[j] = row[j] + maxOf(dp[j], dp[j + 1])`。
 */
fun pathDp018(): Visualization {
    val triangle = loadTriangle018()
    val n = triangle.size
    fun idx(r: Int, c: Int) = r * (r + 1) / 2 + c

    val cells = triangle.flatMapIndexed { r, row ->
        row.mapIndexed { c, v -> Cell(index = idx(r, c), value = v.toLong(), row = r, col = c) }
    }
    val steps = mutableListOf<Step>()

    // 自底向上 DP；chooseRight 记录每个格子的选择方向，供最后回溯最优路径
    val dp = triangle.last().map { it.toLong() }.toLongArray()
    val chooseRight = Array(n - 1) { BooleanArray(it + 1) }

    for (i in n - 2 downTo 0) {
        for (j in 0..i) {
            val left = dp[j]
            val right = dp[j + 1]
            chooseRight[i][j] = right > left
            val best = triangle[i][j] + maxOf(left, right)
            dp[j] = best
            steps += Step(
                i = steps.size,
                updates = listOf(
                    Update(state = State.CURRENT, index = idx(i, j), label = best.toString()),
                    Update(state = State.CONSIDERED, index = idx(i + 1, j)),
                    Update(state = State.CONSIDERED, index = idx(i + 1, j + 1)),
                ),
                caption = "f(${triangle[i][j]}) = ${triangle[i][j]} + max($left, $right) = $best",
                codeLine = 36,
            )
        }
        // 一层处理完：本层 current → done，下一层 considered 复位（已带 dp 标签，归为收尾态）
        steps += Step(
            i = steps.size,
            updates = buildList {
                for (j in 0..i) add(Update(state = State.DONE, index = idx(i, j)))
                for (j in 0..i + 1) add(Update(state = State.DONE, index = idx(i + 1, j)))
            },
            caption = "第 ${i + 1} 行处理完，向上收拢",
            codeLine = 33,
        )
    }

    // 从顶点沿记录的选择方向回溯最优路径
    var j = 0
    var sum = 0L
    for (i in 0 until n) {
        sum += triangle[i][j]
        steps += Step(
            i = steps.size,
            updates = listOf(Update(state = State.ON_PATH, index = idx(i, j))),
            caption = "回溯最优路径：第 ${i + 1} 行取 ${triangle[i][j]}（累计 $sum）",
            codeLine = 36,
        )
        if (i < n - 1 && chooseRight[i][j]) j++
    }

    return Visualization(
        problemId = 18,
        title = "最大路径和：自底向上 DP（原题 15 行三角形）",
        kind = "grid2d",
        scene = Scene(rows = n, triangle = true, cells = cells),
        steps = steps,
    )
}

/** 读取 0018/solution.kt 中内嵌的 TRIANGLE 常量并解析（解析逻辑与 solution.kt 的 parseTriangle 一致）。 */
private fun loadTriangle018(): List<IntArray> {
    val root = ContentIndex.resolveContentDir()
    val src = File(root, "problems/0018/solution.kt").readText()
    val marker = "const val TRIANGLE = \"\"\""
    val start = src.indexOf(marker)
    require(start >= 0) { "0018/solution.kt 中未找到 TRIANGLE 常量" }
    val bodyStart = start + marker.length
    val end = src.indexOf("\"\"\"", bodyStart)
    require(end > bodyStart) { "0018/solution.kt 中 TRIANGLE 常量未闭合" }
    return src.substring(bodyStart, end).trim().lines()
        .map { line -> line.trim().split(" ").map { it.toInt() }.toIntArray() }
}
