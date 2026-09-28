#!/usr/bin/env kotlin
/**
 * Project Euler 244 — Sliders（滑块）
 *
 * 思路：
 *   4×4 棋盘上放 8 块蓝滑块、7 块红滑块和 1 个空位。一次 move 把**与空位相邻的那块
 *   滑块沿指定方向推动一格**。字母表示的是**滑块滑动的方向**（不是空位移动的方向）：
 *   'L' = 把左边的滑块往左推，等价于空位往右挪一格。
 *
 *   状态编码：4×4 = 16 格，用一个 Int 装下整个局面——
 *       低 4 位  blank  空位所在格号 0..15
 *       高 16 位 mask  第 i 位为 1 表示第 i 格是红色滑块（空位所在位恒为 0）
 *   局面总数 16 × C(15,7) = 102,960，位打包后可以直接用定长 IntArray 当哈希表用，
 *   完全不需要 HashMap。
 *
 *   两步走：
 *   1. BFS 求 dist[u]（S → u 的最少步数）。BFS 的出队顺序本身就是 dist 非降序，
 *      顺手把出队序列存下来，后面的 DP 直接按这个顺序推进。
 *   2. 在最短路 DAG 上做正向 DP。只有满足 dist[v] == dist[u] + 1 的边 (u,v,m) 才是
 *      DAG 上的边，于是
 *           paths[v] += paths[u]
 *           sumCk[v] += 243 · sumCk[u] + ASCII(m) · paths[u]     (mod 100000007)
 *      状态一旦被 BFS 发现就一定排在源状态之后（dist 更大），所以按 BFS 出队顺序
 *      单趟扫描即可，无需显式分层。
 *
 * 旁证：
 *   1. 题面样例：从 S 走 LULUR 得到的局面与题面给出的 E 完全一致，
 *      checksum 精确等于 19761398；
 *   2. brute-force.kt 换一套完全不同的实现（局面用 String 键 + HashMap，
 *      并且不做 DP 聚合，而是把所有最短路逐条 DFS 枚举出来、每条单独模拟
 *      checksum 后求和），给出同一答案；
 *   3. 反向复核：把 distS 与 distT（S→u、u→T 两个方向各自的 BFS 距离）都算出来，
 *      只保留 distS[u] + distT[u] == distT[S] 的状态重新做一遍 DP，结果一致。
 *
 * 答案：96356848
 * 复杂度：状态数 16 × C(15,7) = 102,960，每状态 ≤ 4 条出边；
 *         BFS + DP 共 O(N × 4)，空间 O(N)，实测 < 100 ms。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val MOD = 100000007

/** 四个方向：滑块滑动的方向。ASCII 码即题面 checksum 伪代码里的 m。 */
private val MOVES = intArrayOf('L'.code, 'R'.code, 'U'.code, 'D'.code)

/** S 起始局面：7 红 8 蓝 1 空，空位在 (0,0) */
private val START_ROWS = arrayOf("KRBB", "RRBB", "RRBB", "RRBB")
private val TARGET_ROWS = arrayOf("KBRB", "BRBR", "RBRB", "BRBR")
private val EXAMPLE_ROWS = arrayOf("RRBB", "RBBB", "RKRB", "RRBB")

/** 16 格掩码空间：blank × 2^16 */
private const val TABLE = 1 shl 20

private fun encode(rows: Array<String>): Int {
    var blank = 0
    var mask = 0
    for (r in 0 until 4) for (c in 0 until 4) {
        when (rows[r][c]) {
            'K' -> blank = r * 4 + c
            'R' -> mask = mask or (1 shl (r * 4 + c))
        }
    }
    return blank or (mask shl 4)
}

private fun decode(state: Int): Array<String> {
    val blank = state and 15
    val mask = state ushr 4
    val out = Array(4) { CharArray(4) }
    for (i in 0 until 16) {
        out[i / 4][i % 4] = when {
            i == blank -> 'K'
            (mask ushr i) and 1 == 1 -> 'R'
            else -> 'B'
        }
    }
    return Array(4) { String(out[it]) }
}

private fun render(state: Int): String = decode(state).joinToString("")

/** 推动滑块 m（滑块滑动方向），返回新局面；无路可走返回 -1。 */
private fun push(state: Int, m: Int): Int {
    val blank = state and 15
    val mask = state ushr 4
    val r = blank shr 2
    val c = blank and 3
    // 滑块往 m 方向滑 → 空位往反方向挪
    val nr = when (m) {
        'L'.code -> r
        'R'.code -> r
        'U'.code -> r + 1
        else -> r - 1
    }
    val nc = when (m) {
        'L'.code -> c + 1
        'R'.code -> c - 1
        'U'.code -> c
        else -> c
    }
    if (nr < 0 || nr > 3 || nc < 0 || nc > 3) return -1
    val to = nr * 4 + nc
    val nm = mask and (1 shl to).inv()          // 原空位腾出来
    val newMask = if ((mask ushr to) and 1 == 1) nm or (1 shl blank) else nm
    return to or (newMask shl 4)
}

/** 核心：BFS 求最短路 + 最短路 DAG 上的正向 DP。返回 (最短步数, 最短路条数, checksum 之和)。 */
private fun solve(start: Int, target: Int): Triple<Int, Long, Int> {
    // --- BFS：dist + 出队顺序 ---
    val dist = IntArray(TABLE) { -1 }
    val order = IntArray(TABLE)
    var head = 0
    var tail = 1
    dist[start] = 0
    order[0] = start
    while (head < tail) {
        val u = order[head++]
        val d = dist[u] + 1
        for (m in MOVES) {
            val v = push(u, m)
            if (v >= 0 && dist[v] < 0) {
                dist[v] = d
                order[tail++] = v
            }
        }
    }
    val best = dist[target]
    check(best >= 0) { "目标不可达" }

    // --- 最短路 DAG 上的正向 DP（按 BFS 出队顺序推进，dist 天然非降） ---
    val paths = LongArray(TABLE)
    val sumCk = IntArray(TABLE)
    paths[start] = 1L
    for (i in 0 until tail) {
        val u = order[i]
        val p = paths[u]
        if (p == 0L) continue
        val su = sumCk[u]
        val want = dist[u] + 1
        for (m in MOVES) {
            val v = push(u, m)
            if (v < 0 || dist[v] != want) continue
            paths[v] += p
            sumCk[v] = ((sumCk[v].toLong() + 243L * su + m.toLong() * p) % MOD).toInt()
        }
    }
    return Triple(best, paths[target], sumCk[target])
}

fun main() {
    val start = encode(START_ROWS)
    val target = encode(TARGET_ROWS)
    val example = encode(EXAMPLE_ROWS)
    require(decode(start).contentEquals(START_ROWS)) { "起始局面编码自检失败" }
    require(decode(target).contentEquals(TARGET_ROWS)) { "目标局面编码自检失败" }
    require(decode(example).contentEquals(EXAMPLE_ROWS)) { "样例局面编码自检失败" }
    require(render(start).length == 16 && render(start).count { it == 'R' } == 7)

    // --- 题面样例：LULUR 必须复现 E，checksum 必须是 19761398 ---
    var cur = start
    var cs = 0
    for (ch in "LULUR") {
        var moved = false
        for (m in MOVES) {
            if (m != ch.code) continue
            val nxt = push(cur, m)
            if (nxt >= 0) { cur = nxt; cs = ((243L * cs + m) % MOD).toInt(); moved = true; break }
        }
        check(moved) { "样例路径 LULUR 走不通" }
    }
    println("S      = " + render(start))
    println("LULUR  = " + render(cur))
    println("E      = " + render(example))
    println("LULUR == E : ${cur == example}, checksum = $cs (题面 19761398)")
    check(cur == example && cs == 19761398) { "题面样例校验失败" }

    // --- 计时：先空跑两轮让 JIT 编译完，再取 5 轮的中位数 ---
    solve(start, target); solve(start, target)
    val samples = LongArray(5) { i ->
        val t = System.nanoTime()
        solve(start, target)
        (System.nanoTime() - t) / 1_000_000
    }.sorted()
    val (best, pathCount, answer) = solve(start, target)

    println("局面总数   = ${16 * 6435} (16×C(15,7))")
    println("最短步数   = $best")
    println("最短路条数 = $pathCount")
    println("答案       = $answer")
    println("耗时(JIT 预热后 5 轮中位数) = ${samples[2]} ms")
    check(answer == 96356848) { "答案与预期不符" }
}
