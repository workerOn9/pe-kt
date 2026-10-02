package dev.pekt.engine

/**
 * PE 308 — An Amazing Prime-generating自动机（An Amazing Prime-generating Automaton）：
 * Conway 的 14 分数素数生成自动机从种子 2 出发，求状态首次变成 2^{p_10001} 时已迭代的轮数。
 *
 * 推导（详见 content/problems/0308/solution.kt 头部与 0308/analysis.md）：
 *
 *   状态只用 10 个质数 2,3,5,7,11,13,17,19,23,29。把状态记成 (x,y,z,w,t)
 *   = (质数 2 的指数, 3 的, 5 的, 7 的, 标记质数)，t 取值为 {无,11,13,17,19,23,29}。
 *   分子/分母里每个质数的指数都恰好是 1，所以「分母整除状态」等价于「对应质数指数 ≥ 1」，
 *   一次按位与即可判定。
 *
 *   朴素地一轮一轮乘（14 个分数从头扫）要做约 1.5e15 轮（本机约 300 天），不可行。
 *   但因为「永远取第一个能整除的分数」，优先级固定，有三条必然成环的循环可以精确整体跳过：
 *     A. t=11 且 y>0：f4(29/33) 接 f5(77/29) 循环 y 次   → y 个 3 变 y 个 7，轮数 2y
 *     B. t=19 且 x>0：f3(23/38) 接 f6(95/23) 循环 x 次   → x 个 2 变 x 个 5，轮数 2x
 *        （f6 = 95/23 的分母只有 23，z=0 也能整除）
 *     C. t=13 且 w>0 且 z>0：f0(17/91) 接 f1(78/85) 循环 min(w,z) 次
 *        → min(w,z) 个 (7,5) 变 min(w,z) 个 (2,3)，轮数 2*min(w,z)
 *   跳过与否状态与轮数完全等价（solution.kt 里对同一前缀做了逐位对拍）。
 *
 *   状态恰为 2^p 的判据：x=p 且 y=z=w=0 且无标记质数（17 刚被 f8=1/17 消耗的那一刻）。
 *   本机实测完整跑完约 330 秒（7.66e10 次宏步），远超 RunEngine 的 10s 熔断阈值——
 *   熔断对纯 CPU 协程不会中途打断，但接口会挂 5 分钟，需要评估。
 *
 * 最终答案：状态首次达到 2^104743 时共迭代 1,539,669,807,660,924 轮。
 */
internal fun solve0308Impl(): Long {
    // p_10001（埃氏筛，本函数自己算，不写死）
    val p10001 = nthPrimeLocal(10001)
    var x = 1; var y = 0; var z = 0; var w = 0; var t = NONE_TOKEN
    var iters = 0L
    var next = 2
    while (next <= p10001) {
        if (t == T11 && y > 0) { iters += 2L * y; w += y; y = 0 }            // 循环 A：3 → 7
        else if (t == T19 && x > 0) { iters += 2L * x; z += x; x = 0 }        // 循环 B：2 → 5
        else if (t == T13 && w > 0 && z > 0) {                                // 循环 C：(7,5) → (2,3)
            val m = if (w < z) w else z
            iters += 2L * m; w -= m; z -= m; x += m; y += m
        } else {
            val f: Int
            when {
                w > 0 && t == T13 -> f = 0    // 17/91  = 17/(7*13)
                z > 0 && t == T17 -> f = 1    // 78/85  = 2*3*13/(5*17)
                y > 0 && t == T17 -> f = 2    // 19/51  = 19/(3*17)
                x > 0 && t == T19 -> f = 3    // 23/38  = 23/(2*19)
                y > 0 && t == T11 -> f = 4    // 29/33  = 29/(3*11)
                t == T29 -> f = 5             // 77/29  = 7*11/29
                t == T23 -> f = 6             // 95/23  = 5*19/23
                t == T19 -> f = 7             // 77/19  = 7*11/19
                t == T17 -> f = 8             // 1/17
                t == T13 -> f = 9             // 11/13
                t == T11 -> f = 10            // 13/11
                x > 0 -> f = 11               // 15/2 = 3*5/2
                w > 0 -> f = 12               // 1/7
                else -> f = 13                // 55/1
            }
            when (f) {
                0 -> { w--; t = T17 }
                1 -> { z--; x++; y++; t = T13 }
                2 -> { y--; t = T19 }
                3 -> { x--; t = T23 }
                4 -> { y--; t = T29 }
                5 -> { w++; t = T11 }
                6 -> { z++; t = T19 }
                7 -> { w++; t = T11 }
                8 -> { t = NONE_TOKEN }
                9 -> { t = T11 }
                10 -> { t = T13 }
                11 -> { x--; y++; z++ }
                12 -> { w-- }
                13 -> { z++; t = T11 }
            }
            iters++
            // 状态恰为 2^x：x 个 2，其余质数指数与标记全空
            if (t == NONE_TOKEN && y == 0 && z == 0 && w == 0 && x >= next) {
                if (x == p10001) return iters
                next = x + 1
            }
        }
    }
    return iters
}

private const val NONE_TOKEN = 0
private const val T11 = 1
private const val T13 = 2
private const val T17 = 3
private const val T19 = 4
private const val T23 = 5
private const val T29 = 6

/** 第 n 个素数（n 从 1 开始），埃氏筛。 */
private fun nthPrimeLocal(n: Int): Int {
    var limit = 16
    while (true) {
        val isP = BooleanArray(limit + 1) { it >= 2 }
        var i = 2
        while (i.toLong() * i <= limit) {
            if (isP[i]) { var j = i * i; while (j <= limit) { isP[j] = false; j += i } }
            i++
        }
        var count = 0
        for (v in 2..limit) if (isP[v]) { count++; if (count == n) return v }
        limit *= 2
    }
}
