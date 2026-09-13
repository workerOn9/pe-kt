package dev.pekt.visualize

/** 007 演示实例：埃氏筛筛 2..100（演示参数，与原题第 10001 个素数分离）。 */
private const val SIEVE_LIMIT = 100
private const val SIEVE_COLUMNS = 10

/**
 * 埃氏筛（kind=grid1d）：2..100，10 列。
 * 步骤粒度为"每个素数一组"：先 current 标记该素数，再一步批量划去它尚未被划去的倍数。
 * 只需筛到 √100 = 10，之后一步把剩余 idle 单元格全部置为 prime。
 *
 * codeLine 对应 content/problems/0007/solution.kt：
 * 23 = `if (!isComposite[i])`，29 = `isComposite[j.toInt()] = true`，24 = `count++`。
 */
fun sieve007(): Visualization {
    val cells = (2..SIEVE_LIMIT).map { Cell(index = it - 2, value = it.toLong()) }
    val eliminated = BooleanArray(SIEVE_LIMIT + 1)
    val confirmedPrimes = mutableSetOf<Int>()
    val steps = mutableListOf<Step>()

    for (p in 2..SIEVE_LIMIT) {
        if (eliminated[p]) continue
        if (p * p > SIEVE_LIMIT) break // 只需筛到 √100，剩余的都是素数
        confirmedPrimes += p

        steps += Step(
            i = steps.size,
            updates = listOf(Update(state = State.CURRENT, index = p - 2)),
            caption = "$p 是素数，划去它的倍数",
            codeLine = 23,
        )

        // 从 p*p 开始（更小的倍数已被更小的素数划去），与 solution.kt 一致
        val multiples = (p * p..SIEVE_LIMIT step p).filter { !eliminated[it] }
        multiples.forEach { eliminated[it] = true }
        val shown = multiples.take(10).joinToString(", ")
        val suffix = if (multiples.size > 10) " …（共 ${multiples.size} 个）" else ""
        steps += Step(
            i = steps.size,
            updates = multiples.map { Update(state = State.ELIMINATED, index = it - 2) } +
                Update(state = State.PRIME, index = p - 2),
            caption = "划去 $p 的倍数：$shown$suffix",
            codeLine = 29,
        )
    }

    val rest = (2..SIEVE_LIMIT).filter { !eliminated[it] && it !in confirmedPrimes }
    steps += Step(
        i = steps.size,
        updates = rest.map { Update(state = State.PRIME, index = it - 2) },
        caption = "剩余的都是素数",
        codeLine = 24,
    )

    return Visualization(
        problemId = 7,
        title = "埃氏筛法（演示实例：筛到 100）",
        kind = "grid1d",
        scene = Scene(columns = SIEVE_COLUMNS, cells = cells),
        steps = steps,
    )
}
