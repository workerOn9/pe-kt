#!/usr/bin/env kotlin
// PE 220 — Heighway Dragon（海威龙曲线）
// 思路：D_n 由 D_{n-1} 按 a → aRbFR、b → LFaLb 重写而来，D_50 含 2^50 ≈ 1.13×10^15 个 F，
//       无法展开成字符串。关键是把「某个符号在第 n 层展开出的走法」压缩成三个量：
//       净位移 (dx, dy)（以进入该符号时的朝向为「上」）、净转角 Δθ（90° 的整数倍）、
//       以及 F 的个数。这三个量对拼接封闭：先走 A 再走 B 时
//         cnt = cntA + cntB，Δθ = ΔθA + ΔθB，d = dA + rot(ΔθA)·dB，
//       于是能在 50 层上自底向上把 F / a / b 三张表全部建出来（O(50)）。
//       求「第 k 步后的位置」时按符号序列自顶向下走：整段的 F 数不超过剩余步数就整段跳过
//       （位置按当前朝向旋转累加、朝向累加、步数相减），否则说明目标落在该段内部，
//       下沉一层展开该符号（a、b 展开成 5 个符号，F 的 cnt = 1 永远走整段分支）。
//       注意整段跳过时段的「尾部转向」会被一并算进朝向（cnt 正好等于剩余步数时尤其明显），
//       位置不受影响、只有朝向会偏——本题只问位置，故按最简写法处理。
//       起点 (0,0) 朝上（朝向 0 = 上，顺时针 R 为 +1）。题面锚点：D_10 第 500 步在 (18,16)。
// 答案格式：题面要求 x,y 无空格；meta.json 的 answer 是 Long，故约定按 10^6 进制拼接
//       （answer = x·10^6 + y ⟺ x = answer/10^6、y = answer%10^6），详见 analysis.md。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

private const val LEVELS = 50
private val DX = longArrayOf(0, 1, 0, -1)
private val DY = longArrayOf(1, 0, -1, 0)

/** 一个符号展开出的走法：F 个数、净位移、净转角（0..3，R 为 +1）。 */
private class Part(val cnt: Long, val dx: Long, val dy: Long, val dth: Int)

private fun rot(x: Long, y: Long, th: Int): LongArray = when (th and 3) {
    0 -> longArrayOf(x, y)
    1 -> longArrayOf(y, -x)
    2 -> longArrayOf(-x, -y)
    else -> longArrayOf(-y, x)
}

private fun seq(parts: List<Part>): Part {
    var cnt = 0L
    var dx = 0L
    var dy = 0L
    var th = 0
    for (p in parts) {
        val r = rot(p.dx, p.dy, th)
        dx += r[0]
        dy += r[1]
        th = (th + p.dth) and 3
        cnt += p.cnt
    }
    return Part(cnt, dx, dy, th)
}

/** tab[lev][0] = F，[1] = a，[2] = b。 */
private fun buildTable(): Array<Array<Part>> {
    val tab = Array(LEVELS + 1) { arrayOfNulls<Part>(3) }
    tab[0][0] = Part(1, 0, 1, 0)
    tab[0][1] = Part(0, 0, 0, 0)              // 第 0 层的 a、b 不产生任何走法
    tab[0][2] = Part(0, 0, 0, 0)
    for (n in 1..LEVELS) {
        tab[n][0] = Part(1, 0, 1, 0)
        // a_n = a_{n-1} R b_{n-1} F R
        tab[n][1] = seq(listOf(tab[n - 1][1]!!, Part(0, 0, 0, 1), tab[n - 1][2]!!, tab[n - 1][0]!!, Part(0, 0, 0, 1)))
        // b_n = L F a_{n-1} L b_{n-1}
        tab[n][2] = seq(listOf(Part(0, 0, 0, 3), tab[n - 1][0]!!, tab[n - 1][1]!!, Part(0, 0, 0, 3), tab[n - 1][2]!!))
    }
    @Suppress("UNCHECKED_CAST")
    return Array(LEVELS + 1) { n -> Array(3) { k -> tab[n][k] as Part } }
}

/** D_{LEVELS} 上走完 target 步后的 (x, y, 朝向)。 */
private fun navigate(tab: Array<Array<Part>>, target: Long): LongArray {
    var x = 0L
    var y = 0L
    var o = 0
    var rem = target
    // 栈元素：符号（-1 = R，-2 = L，0..2 = F/a/b）与层级
    val stack = ArrayDeque<LongArray>()
    stack.addLast(longArrayOf(1, LEVELS.toLong()))    // D_50 = F + a_50
    stack.addLast(longArrayOf(0, LEVELS.toLong()))
    while (stack.isNotEmpty() && rem > 0L) {
        val top = stack.removeLast()
        val sym = top[0].toInt()
        val lev = top[1].toInt()
        if (sym == -1) {
            o = (o + 1) and 3
            continue
        }
        if (sym == -2) {
            o = (o + 3) and 3
            continue
        }
        val p = tab[lev][sym]
        if (p.cnt == 0L) continue
        if (p.cnt <= rem) {                           // 整段跳过；cnt == 1 的 F 也走这里
            val r = rot(p.dx, p.dy, o)
            x += r[0]
            y += r[1]
            o = (o + p.dth) and 3
            rem -= p.cnt
            continue
        }
        // 目标落在该段内部：下沉一层展开（F 的 cnt = 1 不可能走到这里）
        val kids = if (sym == 1) intArrayOf(1, -1, 2, 0, -1) else intArrayOf(-2, 0, 1, -2, 2)
        for (i in kids.indices.reversed()) stack.addLast(longArrayOf(kids[i].toLong(), (lev - 1).toLong()))
    }
    return longArrayOf(x, y, o.toLong())
}

/** D_n 的完整展开（n ≤ 20 可用），按题面规则执行 target 步。 */
private fun bruteDragon(n: Int, target: Long): LongArray {
    var s = StringBuilder("a")
    repeat(n) {
        val b = StringBuilder(s.length * 3)
        for (ch in s) when (ch) {
            'a' -> b.append("aRbFR")
            'b' -> b.append("LFaLb")
            else -> b.append(ch)
        }
        s = b
    }
    var x = 0L
    var y = 0L
    var o = 0
    var c = 0L
    for (ch in "F") {                                 // D_n = "F" + 展开后的 a
        x += DX[o]
        y += DY[o]
        c++
    }
    for (ch in s) {
        if (c >= target) break
        when (ch) {
            'F' -> {
                x += DX[o]
                y += DY[o]
                c++
            }
            'R' -> o = (o + 1) and 3
            'L' -> o = (o + 3) and 3
        }
    }
    return longArrayOf(x, y, o.toLong())
}

fun main() {
    val tab = buildTable()
    // 锚点与外推自查：题面 D_10 第 500 步 = (18,16)；另有 3 组小规模与暴力展开对比
    for (n in intArrayOf(10, 12, 16, 18)) {
        val k = when (n) {
            10 -> 500L
            12 -> 3_000L
            16 -> 40_000L
            else -> 200_000L
        }
        val b = bruteDragon(n, k)
        val f = navigate(tab, k)
        val ok = b[0] == f[0] && b[1] == f[1]
        println("D_$n 第 $k 步：暴力 (${b[0]},${b[1]})  本实现 (${f[0]},${f[1]})  ${if (ok) "OK" else "MISMATCH"}")
    }
    repeat(2) { navigate(tab, 1_000_000_000_000L) }    // JIT 预热
    val t0 = System.nanoTime()
    val r = navigate(tab, 1_000_000_000_000L)
    val ms = (System.nanoTime() - t0) / 1_000_000
    println("${r[0]},${r[1]}")
    System.err.println("navigate(1e12) wall = $ms ms，编码值 = ${r[0] * 1_000_000L + r[1]}")
}
