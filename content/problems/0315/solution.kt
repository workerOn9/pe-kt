#!/usr/bin/env kotlin
/**
 * Project Euler 315 — Digital Root Clocks（数根时钟）
 *
 * 题目：Sam 的时钟每换一个数字就「整块点亮 → 整块熄灭」，Max 的时钟只切换真正变化的笔画。
 *       把 10^7 到 2*10^7 之间所有素数依次喂给两个时钟，求切换总次数之差。
 *
 * 思路推导
 * --------
 * 段位定义（本题专用，与常见七段写法不同：7 含左上段 f，4 是「中横 + 左上 + 右上 + 右下」）：
 *   a=上横 b=右上 c=右下 d=下横 e=左下 f=左上 g=中横
 *   0=abcdef 1=bc 2=abdeg 3=abcdg 4=bcfg 5=acdfg 6=acdefg 7=abcf 8=abcdefg 9=abcdfg
 *
 * 把「显示序列」记作面板状态序列 U0=全灭, U1=p, U2=dr(p), …, Uk=最终一位数, Uk+1=全灭，
 * 每个 Ui 是一组「数位右对齐的笔画掩码」，数位不足处为空白（掩码 0）。
 *
 *   Sam：一次全灭 -> 目标 -> 全灭切换 popcount(U) 两次，于是
 *        Sam = 2 * Σ_i popcount(U_i)。
 *   Max：只切差异，Max = Σ_{i=0..k} popcount(U_i xor U_{i+1})。
 *
 * 对单步 i -> i+1，两钟花销之差
 *        popcount(U)+popcount(V) - popcount(U xor V) = 2*popcount(U and V)，
 *   两端的「全灭」与任何掩码的交都为空，自动贡献 0。所以
 *
 *        总差 = Σ_{相邻两步} 2 * popcount(数位对齐后的 U_i and U_{i+1})。
 *
 * 数位是【右对齐】的：137 -> 11 时，个位 7->1，十位 3->1，百位 1->空白。
 * 素数最多 8 位，数根和 <= 64，故数位链最多 4 个数、3 个相邻步，单价 O(1)。
 *
 * 验证
 * --------
 * 1. 题面样例：137 的数位链 137 -> 11 -> 2，Sam = 22+8+10 = 40、Max = 11+7+0+7+5 = 30、差 = 10；
 * 2. 双方法互证：①「逐段显式模拟两钟」vs「2*popcount(U and V) 差值公式」，
 *    在题面样例与前若干万个素数上逐一比较；
 * 3. 段位表按题面数字 1=2 段、4=4 段、8=7 段、2=5 段、7=4 段逐个打印核对。
 *
 * 复杂度：埃氏筛 O(L log log L)、L=2e7；主循环 O(#primes)，约 6.2e5 个素数。
 *       暴力对照为「逐段模拟」，常数约为公式法的 6 倍，另有素数链构造开销。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *       （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

/** 题面区间上界。 */
private const val LIMIT = 20_000_000

/** 暴力对照覆盖的素数个数（与优化法同规模，便于逐项对照耗时）。 */
private const val BRUTE_PRIMES = 606_028

/** 互证时逐段模拟覆盖的素数个数。 */
private const val CROSS_PRIMES = 20_000

private const val A = 1   // 上横
private const val B = 2   // 右上
private const val C = 4   // 右下
private const val D = 8   // 下横
private const val E = 16  // 左下
private const val F = 32  // 左上
private const val G = 64  // 中横

/** 题面七段掩码表：下标 = 数字，值 = 点亮的笔画集合。 */
private val SEG = intArrayOf(
    A or B or C or D or E or F,          // 0 = abcdef   (6)
    B or C,                              // 1 = bc        (2)
    A or B or D or E or G,               // 2 = abdeg     (5)
    A or B or C or D or G,               // 3 = abcdg     (5)
    B or C or F or G,                    // 4 = bcfg      (4)
    A or C or D or F or G,               // 5 = acdfg     (5)
    A or C or D or E or F or G,          // 6 = acdefg    (6)
    A or B or C or F,                    // 7 = abcf      (4)  <- 含左上 f
    A or B or C or D or E or F or G,     // 8 = abcdefg   (7)
    A or B or C or D or F or G,          // 9 = abcdfg    (6)
)

private val SEG_NAME = arrayOf("a(上横)", "b(右上)", "c(右下)", "d(下横)", "e(左下)", "f(左上)", "g(中横)")

/** 逐段模拟用的位序表（提到顶层，避免热循环里反复分配）。 */
private val BIT_ORDER = intArrayOf(A, B, C, D, E, F, G)

/** 面板宽度（素数最多 8 位，数根链不超过 8 位）。 */
private const val WIDTH = 8

/** n 的数根：数位和反复迭代到一位数。 */
private fun digitalRoot(n: Int): Int {
    var m = n
    while (m >= 10) {
        var s = 0
        var t = m
        while (t > 0) { s += t % 10; t /= 10 }
        m = s
    }
    return m
}

/** 数位链：p -> 数位和 -> ... -> 一位数，写进 buf，返回长度。 */
private fun rootChain(p: Int, buf: IntArray): Int {
    var len = 1
    var t = p
    buf[0] = p
    while (t >= 10) {
        var s = 0
        var u = t
        while (u > 0) { s += u % 10; u /= 10 }
        t = s
        buf[len++] = s
    }
    return len
}

/**
 * 把整数渲染成右对齐的面板状态。
 * 面板最多 WIDTH=8 个数位、每位 7 段，共 56 位，正好塞进一个 Long：
 * 第 7*i + s 位表示「从右数第 i 个数位的第 s 段」是否点亮，数位不足处自然为 0（全灭）。
 */
private fun pack(n: Int): Long {
    var m = 0L
    var v = n
    var i = 0
    while (v > 0 && i < WIDTH) {
        m = m or (SEG[v % 10].toLong() shl (7 * i))
        v /= 10
        i++
    }
    return m
}

/** 兼容旧接口：把面板状态拆成每数位一个段掩码的数组（仅用于样例打印）。 */
private fun unpack(m: Long): IntArray {
    val p = IntArray(WIDTH)
    for (i in 0 until WIDTH) p[i] = ((m ushr (7 * i)) and 0x7FL).toInt()
    return p
}

/** Sam 的切换次数：每个数「整块点亮 + 整块熄灭」。masks 是每数位一个段掩码。 */
private fun samToggles(panels: Array<IntArray>): Int {
    var t = 0
    for (p in panels) for (i in 0 until WIDTH) t += Integer.bitCount(p[i])
    return 2 * t
}

/** Max 的切换次数：逐段显式模拟——维护面板亮段状态，只翻转状态确实不同的段。 */
private fun maxTogglesSim(panels: Array<IntArray>): Int {
    val state = IntArray(WIDTH)              // 全灭
    var t = 0
    for (target in panels) {
        for (i in 0 until WIDTH) {
            for (b in BIT_ORDER) {
                if ((state[i] and b != 0) != (target[i] and b != 0)) t++
            }
            state[i] = target[i]
        }
    }
    for (i in 0 until WIDTH) for (b in BIT_ORDER) if (state[i] and b != 0) t++  // 最终熄灭
    return t
}

/** 公式法：Sam - Max = Σ_{相邻两步} 2 * popcount(右对齐后的 U and V)。 */
private fun diffByFormula(panels: Array<IntArray>): Int {
    val blank = IntArray(WIDTH)
    var t = 0
    for (i in 0 until panels.size - 1) {
        val u = panels[i]
        val v = panels[i + 1]
        for (j in 0 until WIDTH) t += Integer.bitCount(u[j] and v[j])
    }
    val last = panels.last()
    for (j in 0 until WIDTH) t += Integer.bitCount(last[j] and blank[j])   // 首尾与全灭的交恒为 0
    return 2 * t
}

/** 埃氏筛，闭区间 [limitLo, limitHi]。 */
private fun primesInRange(limitLo: Int, limitHi: Int): IntArray {
    val comp = java.util.BitSet(limitHi + 1)
    val out = IntArray(900_000)
    var m = 0
    var i = 2
    while (i.toLong() * i <= limitHi) {
        if (!comp.get(i)) {
            var j = i.toLong() * i
            while (j <= limitHi) { comp.set(j.toInt()); j += i }
        }
        i++
    }
    for (i in limitLo..limitHi) if (i >= 2 && !comp.get(i)) out[m++] = i
    return out.copyOf(m)
}

/**
 * 全区间合计（公式法，零分配）：整块面板压在一个 Long 里，
 * 每步只需一次 java.lang.Long.bitCount(u and v)；首尾的全灭状态打包为 0，交集自动为 0。
 */
private fun totalFormula(primes: IntArray): Long {
    val buf = IntArray(WIDTH)
    var sum = 0L
    for (p in primes) {
        val len = rootChain(p, buf)
        var prev = 0L
        var acc = 0
        for (i in 0 until len) {
            val cur = pack(buf[i])
            acc += java.lang.Long.bitCount(prev and cur)
            prev = cur
        }
        acc += java.lang.Long.bitCount(prev and 0L)   // 末位数 -> 全灭，恒为 0
        sum += 2L * acc
    }
    return sum
}

/** 全区间合计（暴力法：逐段模拟两钟后相减，零分配）。 */
private fun totalBrute(primes: IntArray, count: Int): Long {
    val chain = IntArray(WIDTH)
    val state = IntArray(WIDTH)
    var sum = 0L
    for (i in 0 until count) {
        val len = rootChain(primes[i], chain)
        java.util.Arrays.fill(state, 0)            // 面板全灭
        var sam = 0
        var max = 0
        for (s in 0 until len) {
            val target = unpack(pack(chain[s]))
            for (d in 0 until WIDTH) {
                sam += Integer.bitCount(target[d])
                for (b in BIT_ORDER) if ((state[d] and b != 0) != (target[d] and b != 0)) max++
                state[d] = target[d]
            }
        }
        for (d in 0 until WIDTH) for (b in BIT_ORDER) if (state[d] and b != 0) max++   // 最终熄灭
        sum += (2 * sam - max).toLong()
    }
    return sum
}

/** 预热 1 次后跑 runs 轮，返回毫秒中位数。 */
private inline fun medianMs(runs: Int = 5, body: () -> Long): Double {
    body()
    val ts = DoubleArray(runs)
    for (i in 0 until runs) {
        val s = System.nanoTime()
        body()
        ts[i] = (System.nanoTime() - s) / 1_000_000.0
    }
    ts.sort()
    return ts[runs / 2]
}

fun main() {
    // 0) 段位表自检：题面说 2 要 5 次点亮、7 只要 4 次
    println("七段掩码表（a=上横 b=右上 c=右下 d=下横 e=左下 f=左上 g=中横）：")
    for (dgt in 0..9) {
        val m = SEG[dgt]
        val segs = ArrayList<String>()
        for (i in 0..6) if (m shr i and 1 == 1) segs.add(SEG_NAME[i].substring(0, 1))
        println("  " + dgt + " = " + segs.joinToString("") + "  (" + Integer.bitCount(m) + " 段)")
    }
    check(Integer.bitCount(SEG[1]) == 2) { "1 应为 2 段" }
    check(Integer.bitCount(SEG[4]) == 4) { "4 应为 4 段" }
    check(Integer.bitCount(SEG[8]) == 7) { "8 应为 7 段" }
    check(Integer.bitCount(SEG[2]) == 5) { "2 应为 5 段" }
    check(Integer.bitCount(SEG[7]) == 4) { "7 应为 4 段（含左上 f）" }
    check(SEG[7] and F != 0) { "7 必须含左上段 f" }
    println("段位数自检：1=2, 4=4, 8=7, 2=5, 7=4 且含 f -> 全部通过")

    // 1) 题面样例：137
    val buf137 = IntArray(WIDTH)
    val len137 = rootChain(137, buf137)
    val chain137 = buf137.copyOf(len137)
    val panels137 = Array(len137) { unpack(pack(chain137[it])) }
    val sam = samToggles(panels137)
    val maxS = maxTogglesSim(panels137)
    val difF = diffByFormula(panels137)
    println()
    println("题面样例 137，数位链 = " + chain137.joinToString(" -> "))
    for (i in chain137.indices) {
        val segs = Integer.bitCount(panels137[i].sum())
        println("  「" + chain137[i] + "」点亮 " + segs + " 段，Sam 计 " + (2 * segs) + " 次切换")
    }
    println("  Sam = " + sam + (if (sam == 40) " -> 与题面 40 一致" else " -> 与题面不一致!"))
    println("  Max = " + maxS + (if (maxS == 30) " -> 与题面 30 一致" else " -> 与题面不一致!"))
    println("  差  = " + (sam - maxS) + (if (sam - maxS == 10) " -> 与题面 10 一致" else " -> 与题面不一致!"))
    println("  差值公式法 = " + difF + (if (difF == 10) " -> 一致" else " -> 不一致!"))
    println("  逐步核对：137->11 交段 popcount = " + (0 until WIDTH).sumOf { Integer.bitCount(panels137[0][it] and panels137[1][it]) }
        + "，11->2 交段 popcount = " + (0 until WIDTH).sumOf { Integer.bitCount(panels137[1][it] and panels137[2][it]) })

    // 2) 素数与双方法互证
    val primes = primesInRange(10_000_000, LIMIT)
    println()
    println("区间 [10^7, 2*10^7] 内素数个数 = " + primes.size)

    println("双方法互证（逐段模拟 vs 差值公式）：")
    var ok = true
    for (i in 0 until CROSS_PRIMES) {
        val cb = IntArray(WIDTH)
        val lc = rootChain(primes[i], cb)
        val pa = Array(lc) { unpack(pack(cb[it])) }
        val sim = samToggles(pa) - maxTogglesSim(pa)
        val fml = diffByFormula(pa)
        if (sim != fml) { ok = false; println("  不一致 p=" + primes[i] + " sim=" + sim + " fml=" + fml) }
    }
    println("  前 " + CROSS_PRIMES + " 个素数逐一比较：全部一致 = " + ok)

    val brutePrefix = totalBrute(primes, BRUTE_PRIMES)
    val formulaPrefix = totalFormula(primes.copyOf(BRUTE_PRIMES))
    println("  前 " + BRUTE_PRIMES + " 个素数合计：逐段模拟=" + brutePrefix + "，差值公式=" + formulaPrefix
        + (if (brutePrefix == formulaPrefix) " -> 一致" else " -> 不一致!"))

    // 3) 计时
    val bruteMs = medianMs { totalBrute(primes, BRUTE_PRIMES) }
    val optMs = medianMs { totalFormula(primes) }
    println()
    println("BRUTE_MS: " + "%.3f".format(bruteMs) + "  (逐段模拟两钟，" + BRUTE_PRIMES + " 个素数)")
    println("OPT_MS: " + "%.3f".format(optMs) + "  (差值公式，" + primes.size + " 个素数)")
    println("同规模加速比 = " + "%.2f".format(bruteMs / optMs) + "x")

    // 4) 答案
    val ans = totalFormula(primes)
    println("总切换次数之差 = " + ans)
    println("ANSWER: " + ans)
}
