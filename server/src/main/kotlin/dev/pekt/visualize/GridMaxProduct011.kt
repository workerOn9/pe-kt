package dev.pekt.visualize

import dev.pekt.content.ContentIndex
import java.io.File

/**
 * 011 网格最大乘积（kind=grid2d）：原题 20×20 网格。
 * 网格数据直接解析 content/problems/0011/solution.kt 内嵌的 GRID_TEXT 常量，
 * 保证可视化与真实求解同源（"看到的 = 真实执行的"）。
 *
 * 步骤粒度：20×20 四方向（横/竖/主对角/副对角）四连窗口共 1464 个，远超协议
 * 300 步上限，因此这是"关键窗口演示"——只为以下两类窗口生成步骤：
 *   1. 每个扫描方向内乘积前 3 大（乘积 > 0）的窗口；
 *   2. 扫描过程中刷新当前最大值的窗口（caption 标 "★ 新最大"，状态用 onPath）。
 * 扫过的旧窗口在下一步复位为 considered（表示已检查）。
 * 最后一步把答案窗口 4 格置 onPath，caption 给出最终答案。
 *
 * codeLine 对应 0011/solution.kt：
 * 47 = `for (k in 0 until 4) product *= g[...][...]`，48 = `if (product > best) best = product`。
 */
fun gridMaxProduct011(): Visualization {
    val g = loadGrid011()
    val n = g.size
    fun idx(r: Int, c: Int) = r * n + c

    val cells = g.flatMapIndexed { r, row ->
        row.mapIndexed { c, v -> Cell(index = idx(r, c), value = v.toLong(), row = r, col = c) }
    }

    // 与 solution.kt 的 dirs 一致：右、下、右下、左下，即可不重不漏覆盖四个方向
    data class Dir(val name: String, val dr: Int, val dc: Int)
    val dirs = listOf(Dir("横向", 0, 1), Dir("竖向", 1, 0), Dir("主对角", 1, 1), Dir("副对角", 1, -1))

    data class Window(
        val dir: Dir,
        val r: Int,
        val c: Int, // 起点（0 基）
        val cells: List<Int>,
        val nums: List<Int>,
        val product: Long,
    )

    // 按 横→竖→主对角→副对角 顺序收集全部窗口
    val windows = dirs.flatMap { dir ->
        buildList {
            for (r in 0 until n) for (c in 0 until n) {
                val endR = r + 3 * dir.dr
                val endC = c + 3 * dir.dc
                if (endR !in 0 until n || endC !in 0 until n) continue
                val nums = (0 until 4).map { k -> g[r + k * dir.dr][c + k * dir.dc] }
                add(Window(dir, r, c, (0 until 4).map { k -> idx(r + k * dir.dr, c + k * dir.dc) }, nums,
                    nums.fold(1L) { acc, v -> acc * v }))
            }
        }
    }
    val totalWindows = windows.size

    // 过滤策略：每个方向乘积前 3 大（乘积 > 0）的窗口
    val topPerDir = windows.groupBy { it.dir.name }
        .flatMap { (_, ws) -> ws.filter { it.product > 0 }.sortedByDescending { it.product }.take(3) }
        .mapTo(mutableSetOf()) { it.cells.first() }

    // 展示坐标（1 基）：横向给列区间，竖向给行区间，对角线给起点
    fun Window.pos(): String = when (dir.name) {
        "横向" -> "行${r + 1}，列${c + 1}-${c + 4}"
        "竖向" -> "行${r + 1}-${r + 4}，列${c + 1}"
        else -> "起点(行${r + 1}，列${c + 1})"
    }

    fun Window.expr(): String = nums.joinToString("×") + " = $product"

    val steps = mutableListOf<Step>()
    var best = 0L
    var bestWindow: Window? = null
    var prevCells: List<Int> = emptyList()
    var firstStep = true

    for (w in windows) {
        val isRecord = w.product > best
        if (w.cells.first() !in topPerDir && !isRecord) continue
        if (isRecord) {
            best = w.product
            bestWindow = w
        }
        val filterNote = if (firstStep) "（共 $totalWindows 个四连窗口，仅演示各方向乘积前 3 大与刷新纪录的关键窗口）" else ""
        val recordNote = if (isRecord) "★ 新最大！" else ""
        steps += Step(
            i = steps.size,
            updates = prevCells.map { Update(state = State.CONSIDERED, index = it) } +
                w.cells.map { Update(state = if (isRecord) State.ON_PATH else State.CURRENT, index = it) },
            caption = "$filterNote${w.dir.name}（${w.pos()}）：${w.expr()}$recordNote",
            codeLine = if (isRecord) 48 else 47,
        )
        prevCells = w.cells
        firstStep = false
    }

    // 收尾：复位最后一个展示窗口，把答案窗口 4 格置 onPath
    val answer = bestWindow ?: error("未找到任何窗口")
    steps += Step(
        i = steps.size,
        updates = prevCells.map { Update(state = State.CONSIDERED, index = it) } +
            answer.cells.map { Update(state = State.ON_PATH, index = it) },
        caption = "扫描完成，最终答案 $best：${answer.dir.name}（${answer.pos()}）：${answer.expr()}",
        codeLine = 48,
    )

    return Visualization(
        problemId = 11,
        title = "网格最大乘积：四方向四连窗口扫描（20×20 原题网格，关键窗口演示）",
        kind = "grid2d",
        scene = Scene(rows = n, triangle = false, cells = cells),
        steps = steps,
    )
}

/** 读取 0011/solution.kt 中内嵌的 GRID_TEXT 常量并解析（解析逻辑与 solution.kt 的 parseGrid 一致）。 */
private fun loadGrid011(): Array<IntArray> {
    val root = ContentIndex.resolveContentDir()
    val src = File(root, "problems/0011/solution.kt").readText()
    val marker = "const val GRID_TEXT = \"\"\""
    val start = src.indexOf(marker)
    require(start >= 0) { "0011/solution.kt 中未找到 GRID_TEXT 常量" }
    val bodyStart = start + marker.length
    val end = src.indexOf("\"\"\"", bodyStart)
    require(end > bodyStart) { "0011/solution.kt 中 GRID_TEXT 常量未闭合" }
    return src.substring(bodyStart, end).trim().lines()
        .map { line -> line.trim().split(Regex("\\s+")).map { it.toInt() }.toIntArray() }
        .toTypedArray()
}
