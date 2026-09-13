package dev.pekt.visualize

/** 014 演示实例：Collatz 起点 27（经典长链，111 步到达 1，峰值 9232）。 */
private const val COLLATZ_START = 27L
private const val COLLATZ_PEAK = 9232L

/**
 * Collatz 轨道（kind=sequence）：逐步 append 每个值。
 * caption 只在关键处展开（起点、首次突破 1000、到达峰值、回到 1），
 * 其余步骤给简短的"n 是奇数：3n+1 / n 是偶数：n/2"。
 *
 * codeLine 对应 content/problems/0014/solution.kt：
 * 24 = `n = if (n % 2 == 0L) n / 2 else 3 * n + 1`。
 */
fun collatz014(): Visualization {
    val steps = mutableListOf<Step>()
    var n = COLLATZ_START
    steps += Step(
        i = 0,
        updates = listOf(Update(state = State.CURRENT, append = n)),
        caption = "起点 $n，开始迭代",
        codeLine = 24,
    )

    var exceeded1000 = false
    while (n != 1L) {
        val odd = n % 2 == 1L
        val next = if (odd) 3 * n + 1 else n / 2
        val rule = if (odd) "$n 是奇数：3×$n+1 = $next" else "$n 是偶数：$n/2 = $next"
        val caption = when {
            next == 1L -> "回到 1，链结束（共 ${steps.size} 步）"
            next == COLLATZ_PEAK -> "到达峰值 $COLLATZ_PEAK"
            !exceeded1000 && next > 1000 -> "首次突破 1000：$next"
            else -> rule
        }
        if (next > 1000) exceeded1000 = true
        steps += Step(
            i = steps.size,
            updates = listOf(Update(state = if (next == 1L) State.DONE else State.CURRENT, append = next)),
            caption = caption,
            codeLine = 24,
        )
        n = next
    }

    return Visualization(
        problemId = 14,
        title = "Collatz 轨道（演示实例：起点 27）",
        kind = "sequence",
        scene = Scene(start = COLLATZ_START, xLabel = "步数", yLabel = "值"),
        steps = steps,
    )
}
