package dev.pekt.visualize

/** 015 演示实例：4×4 网格（答案 C(8,4)=70；原题 20×20 同理，答案 C(40,20)=137846528820）。 */
private const val LATTICE_DEMO_N = 4

/**
 * 网格路径 DP（kind=grid2d）：(n+1)×(n+1) 个格点，f(i,j) = 从 (i,j) 到右下角的路径数。
 * 边界（最后一行/列）先初始化为 1；随后按 f(i,j) = f(i+1,j) + f(i,j+1) 自底向上逐点递推，
 * 每步 current 置该点并写入 DP 值标签，两个子节点置 considered，上一步触及的格子复位 done。
 * 最优路径不唯一，故不标完整路径，最后只把起点 (0,0) 置 onPath 并给出答案 70。
 *
 * 015 的正式解法是组合数公式，可视化演示的是等价 DP；codeLine 指向
 * content/problems/0015/solution.kt 第 24 行 `solve = binomial(2 * gridSize, gridSize)`，
 * 即 DP 所计算的同一个量。
 */
fun latticePaths015(): Visualization {
    val n = LATTICE_DEMO_N
    val p = n + 1 // 每边格点数
    fun idx(r: Int, c: Int) = r * p + c

    val cells = (0 until p).flatMap { r ->
        (0 until p).map { c -> Cell(index = idx(r, c), value = 0, row = r, col = c) }
    }
    val f = Array(p) { LongArray(p) }
    val steps = mutableListOf<Step>()

    // 边界初始化：最后一行 f(n,j)=1，最后一列 f(i,n)=1（贴边只能直走，恰一条路径）
    for (j in 0..n) {
        f[n][j] = 1
        steps += Step(
            i = steps.size,
            updates = listOf(Update(state = State.DONE, index = idx(n, j), label = "1")),
            caption = "边界初始化：f($n,$j) = 1（在底边上只能向右直走）",
            codeLine = 24,
        )
    }
    for (i in n - 1 downTo 0) {
        f[i][n] = 1
        steps += Step(
            i = steps.size,
            updates = listOf(Update(state = State.DONE, index = idx(i, n), label = "1")),
            caption = "边界初始化：f($i,$n) = 1（在右边上只能向下直走）",
            codeLine = 24,
        )
    }

    // 自底向上递推 f(i,j) = f(i+1,j) + f(i,j+1)
    var prevTouched: List<Int> = emptyList()
    for (i in n - 1 downTo 0) {
        for (j in n - 1 downTo 0) {
            f[i][j] = f[i + 1][j] + f[i][j + 1]
            steps += Step(
                i = steps.size,
                updates = prevTouched.map { Update(state = State.DONE, index = it) } +
                    Update(state = State.CURRENT, index = idx(i, j), label = f[i][j].toString()) +
                    Update(state = State.CONSIDERED, index = idx(i + 1, j)) +
                    Update(state = State.CONSIDERED, index = idx(i, j + 1)),
                caption = "f($i,$j) = f(${i + 1},$j) + f($i,${j + 1}) = ${f[i + 1][j]} + ${f[i][j + 1]} = ${f[i][j]}",
                codeLine = 24,
            )
            prevTouched = listOf(idx(i + 1, j), idx(i, j + 1))
        }
    }

    // 收尾：只标起点（路径不唯一，任意一条都是 2n 步）
    steps += Step(
        i = steps.size,
        updates = prevTouched.map { Update(state = State.DONE, index = it) } +
            Update(state = State.ON_PATH, index = idx(0, 0), label = f[0][0].toString()),
        caption = "f(0,0) = ${f[0][0]} = C(${2 * n},$n)：共 ${f[0][0]} 条路径，任意一条都是 ${2 * n} 步" +
            "（$n 步向右 + $n 步向下）；原题 20×20 同理，答案 C(40,20) = 137846528820",
        codeLine = 24,
    )

    return Visualization(
        problemId = 15,
        title = "网格路径 DP（演示实例：4×4 网格，答案 C(8,4)=70；原题 20×20 同理）",
        kind = "grid2d",
        scene = Scene(rows = p, triangle = false, cells = cells),
        steps = steps,
    )
}
