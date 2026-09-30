#!/usr/bin/env kotlin
/**
 * Project Euler 299 — Three Similar Triangles（三个相似三角形）
 *
 * 题目：A(a,0), B(b,0), C(0,c), D(0,d)，0<a<b、0<c<d；P 是直线 AC 上坐标全整的点，
 * 使三角形 ABP、CDP、BDP 两两相似。可以证明必须 a=c，于是 P=(p,q)、p+q=a。
 * 数 (a,b,d) 的个数（b+d<N；b,d 有序，故 (2,3,4) 与 (2,4,3) 各算一次）。
 * 已知 b+d<100 有 92 个、(b+d)<100000 有 320471 个；求 b+d<10^8 的个数。
 *
 * 结构推导（全部使用精确有理运算，不用浮点）
 * ────────────────────────────────────────
 * 记 u=b−a>0, v=d−a>0, P=(p,q), p+q=a（可证 P 必须落在线段 AC 内部：P 在线段外时
 * CDP 凑不出 ABP 的 135° 角——唯一靠整数能成立的候选 v=−2p 或 v=−p 都退化/矛盾）。
 * 1) ABP 在 A 处的角恒为 135°（A→B 沿 +x，A→P 沿 (−1,1)），CDP 在 C 处同理由 45°+90° 合成；
 *    两个三角形的 135° 角必须互相对应。
 * 2) P 处四个方向角（从 +x 轴逆时针）：PB∈(−45°,0°)、PA=−45°、PD∈(90°,135°)、PC=135°。
 *    故 ∠BPD∈(90°,180°) 是 BDP 的唯一钝角；BDP 要与 ABP 相似就必须 ∠BPD=135°，
 *    取正切差 ⇒ tan(b′−a)=1 ⇒
 *        u·v = 2·p·q.                                        … (★)
 * 3) ABP~CDP 的两种角度对应：B↔D（tan 相等 ⇒ p·u=q·v）与 B↔P、P↔D（由 (★) 保
 *    |CP|/|AB|=|PD|/|BP| 而自动成立）。第一种与 (★) 联立给出 u²=2q²，无整数解，舍去。
 * 4) BDP~ABP：设 BDP 在 B、D 处的角为 α′、γ′，ABP 在 B、P 处的角为 α、β。
 *    用 tan α=q/(u+q)、tan β=u/(u+2q) 及 α′、γ′ 的正切公式（差向量叉积/点积），
 *    在 (★) 之下：
 *        α′=α  ⇔ (p−q)·u·(u²+2qu+2q²)=0 ⇔ p=q          … 情形 A；
 *        α′=β  ⇔ u⁴+2qu³+2q²u²−2pqu²−4pq²u−4pq³=0，
 *                代入 x=u/q、t=p/q 化简为 t=x²/2 ⇔ u²=2pq，再与 (★) 联立 ⇒
 *                u=v（记为 u=v=2w）且 2pq=u².           … 情形 B
 *
 * 计数参数化（关键：uv=2p² 与 pq=2w² 都是「乘积=2×平方」）
 * ────────────────────────────────────────────────
 * 引理：xy=2z²（x,y>0）的充要参数化 —— 恰有一个因子的 2 的指数为奇数，取 C=另一个因子的
 * 无平方因子部分（C 必为奇数且无平方因子），则 {x,y}={C·r², 2C·s²}，z=C·r·s；映射双射。
 * 情形 A（p=q，a=2p，uv=2p²）：{u,v}={C·x², 2C·y²}，p=C·x·y，两种次序 ⇒
 *        b+d = 4Cxy + Cx² + 2Cy² = C·(x²+4xy+2y²)，每个 (C,x,y) 贡献 2 个三元组。
 * 情形 B（u=v=2w，pq=2w²，a=p+q）：{p,q}={C·x², 2C·y²}，w=C·x·y，
 *        a=C(x²+2y²)，b=d=C(x²+2xy+2y²)，b+d = 2C·(x²+2xy+2y²)。
 * 计数 = 2·Σ_{(x,y): f_A<N} #{C 奇数无平方因子: C·f_A<N}
 *      + Σ_{(x,y): 2f_B<N} #{C 奇数无平方因子: 2C·f_B<N}.
 * 其中 f_A=x²+4xy+2y²（不定二次型，正象限内是双曲形区域）、f_B=x²+2xy+2y²（椭圆型）。
 *
 * 两条独立路径
 * ────────────
 * 路径 1（(x,y) 直方图 + Möbius 平方因子计数）：遍历约 4.5×10^7 个 (x,y)，把 f 值分箱
 *    （f≤20000 直接记 f；否则记 L=⌊(N−1)/f⌋<5000），再对约 2×10^4 个不同的 L 用
 *    #{奇数无平方因子 ≤ L} = Σ_{d 奇} μ(d)·⌊(⌊L/d²⌋+1)/2⌋ 求值。
 * 路径 2（对 C 求和）：total = Σ_{C 奇无平方} [2·N_A(⌊(N−1)/C⌋) + N_B(⌊(N−1)/(2C)⌋)]，
 *    N_A(M)=#{(x,y)≥1: x²+4xy+2y²≤M} 按 y 逐行用 isqrt 求行内点数；C≤20000 直接算，
 *    C>20000 时 M 只有 O(√N) 种取值，用直方图归并。两条路径在结构与实现上完全不同。
 *
 * 验证
 * ────
 * 1. b+d<100 ⇒ 92（题面样例），b+d<10^5 ⇒ 320471（题面样例）；
 * 2. b+d<10^6、10^7 两级与另写的定义级暴力/除数枚举（brute-force.kt）一致；
 * 3. 两路径在 10^8 全尺寸上给出同一个 9 位数。
 *
 * 复杂度
 * ──────
 * 路径 1：O(Σ_{(x,y)} 1) ≈ O(N)（精确约 4.5×10^7 次分箱 + 10^4 量级的 Möbius 求和），内存
 *   只有两张 2×10^4 的直方图；路径 2：O(N^{1/2}·B) 的网点计数 + O(N/B + N/7) 的筛与扫描。
 * 定义级暴力 b+d<N₀ 需枚举 (a,b,d,p) 四元组约 N₀⁴/96 次，只能做到 N₀≈几百。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0299/solution.kt -d /tmp/kc-0299-sol
 * java -cp /tmp/kc-0299-sol:<kotlin-stdlib> SolutionKt
 */

private const val SMALL = 20000L   // 直方图阈值：f≤SMALL 记 f，否则记 L=(N−1)/f

/** 整数平方根（向下取整），Long 安全（本题范围内 r ≤ 10^4）。 */
private fun isqrt(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0 && r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return r
}

/** μ(d) 的线性筛（标准积性筛，p² 处清零）。 */
private fun mobiusSieve(limit: Int): IntArray {
    val mu = IntArray(limit + 1) { 1 }
    val composite = BooleanArray(limit + 1)
    for (p in 2..limit) {
        if (composite[p]) continue
        var m = p
        while (m <= limit) { mu[m] = -mu[m]; m += p }
        val p2 = p.toLong() * p
        if (p2 <= limit) {
            var q = p2.toInt()
            while (q <= limit) { mu[q] = 0; q += p2.toInt() }
        }
        var c = p.toLong() * p
        while (c <= limit) { composite[c.toInt()] = true; c += p }
    }
    mu[0] = 0
    mu[1] = 1
    return mu
}

/** #{奇数无平方因子整数 ≤ L} = Σ_{d 奇} μ(d)·⌊(⌊L/d²⌋+1)/2⌋（奇数的 d² 倍数里取奇数倍）。 */
private fun oddSquarefreeUpTo(L: Long, mu: IntArray): Long {
    var s = 0L
    var d = 1L
    while (d * d <= L) {
        val m = mu[d.toInt()]
        if (m != 0) {
            val q = L / (d * d)
            s += m.toLong() * ((q + 1) / 2)
        }
        d += 2
    }
    return s
}

/** 情形 A 的二次型 f_A=x²+4xy+2y²。 */
private fun fA(x: Long, y: Long) = x * x + 4 * x * y + 2 * y * y

/** 情形 B 的二次型 f_B=x²+2xy+2y²。 */
private fun fB(x: Long, y: Long) = x * x + 2 * x * y + 2 * y * y

/**
 * 路径 1：(x,y) 遍历 + 直方图 + Möbius 计数。
 * 返回 b+d<N 的三元组个数。
 */
private fun countMethod1(n: Long): Long {
    val mu = mobiusSieve(isqrt((n - 1) / 7 + 1).toInt() + 2)

    // ── 情形 A：2·Σ_{(x,y): f_A<N} #{C 奇无平方: C·f_A<N} ──
    val histA = LongArray((SMALL + 1).toInt())
    val lgASize = ((n - 1) / (SMALL + 1)).toInt() + 2
    val histLgA = LongArray(lgASize)
    var y = 1L
    while (fA(1, y) < n) {
        var x = 1L
        while (true) {
            val f = fA(x, y)
            if (f >= n) break
            if (f <= SMALL) histA[f.toInt()]++ else histLgA[((n - 1) / f).toInt()]++
            x++
        }
        y++
    }
    var caseA = 0L
    for (f in 1..SMALL.toInt()) if (histA[f] != 0L) caseA += histA[f] * oddSquarefreeUpTo((n - 1) / f, mu)
    for (L in 1 until lgASize) if (histLgA[L] != 0L) caseA += histLgA[L] * oddSquarefreeUpTo(L.toLong(), mu)
    caseA *= 2

    // ── 情形 B：Σ_{(x,y): 2f_B<N} #{C 奇无平方: 2C·f_B<N} ──
    val histB = LongArray((SMALL + 1).toInt())
    val lgBSize = ((n - 1) / (2 * (SMALL + 1))).toInt() + 2
    val histLgB = LongArray(lgBSize)
    y = 1L
    while (2 * fB(1, y) < n) {
        var x = 1L
        while (true) {
            val f = fB(x, y)
            if (2 * f >= n) break
            if (f <= SMALL) histB[f.toInt()]++ else histLgB[((n - 1) / (2 * f)).toInt()]++
            x++
        }
        y++
    }
    var caseB = 0L
    for (f in 1..SMALL.toInt()) if (histB[f] != 0L) caseB += histB[f] * oddSquarefreeUpTo((n - 1) / (2 * f), mu)
    for (L in 1 until lgBSize) if (histLgB[L] != 0L) caseB += histLgB[L] * oddSquarefreeUpTo(L.toLong(), mu)
    return caseA + caseB
}

/** N_A(M)=#{(x,y)≥1: x²+4xy+2y²≤M}：按 y 逐行，行内点数由 isqrt 解出。 */
private fun latticeA(M: Long): Long {
    if (M < 7) return 0
    var t = 0L
    var y = 1L
    while (2 * y * y + 4 * y + 1 <= M) {
        var x = isqrt(M + 2 * y * y) - 2 * y
        while (x >= 1 && fA(x, y) > M) x--
        while (fA(x + 1, y) <= M) x++
        if (x >= 1) t += x
        y++
    }
    return t
}

/** N_B(M)=#{(x,y)≥1: x²+2xy+2y²≤M}（正定型：椭圆型区域）。 */
private fun latticeB(M: Long): Long {
    if (M < 5) return 0
    var t = 0L
    var y = 1L
    while (1 + 2 * y + 2 * y * y <= M) {
        var x = isqrt(M - y * y) - y
        while (x >= 1 && fB(x, y) > M) x--
        while (fB(x + 1, y) <= M) x++
        if (x >= 1) t += x
        y++
    }
    return t
}

/**
 * 路径 2：对奇数无平方因子 C 求和：
 *   total = Σ_C [2·N_A(⌊(N−1)/C⌋) + N_B(⌊(N−1)/(2C)⌋)]。
 * C>SMALL 时参数 M 只会取到 <N/SMALL 个值，用直方图归并。
 */
private fun countMethod2(n: Long): Long {
    val cmax = (n - 1) / 7                       // f_A 最小为 7（x=y=1）
    val bad = BooleanArray((cmax + 1).toInt())   // bad[m]：m 被某个 d² 整除
    var d = 2L
    while (d * d <= cmax) {
        val dd = (d * d).toInt()
        var m = dd
        while (m <= cmax) { bad[m] = true; m += dd }
        d++
    }
    var total = 0L
    val b = minOf(SMALL, cmax)
    var c = 1L
    while (c <= b) {
        if (!bad[c.toInt()]) total += 2 * latticeA((n - 1) / c) + latticeB((n - 1) / (2 * c))
        c += 2
    }
    val size = (n / (SMALL + 1)).toInt() + 2
    val histA = LongArray(size)
    val histB = LongArray(size)
    var c0 = if (b % 2 == 0L) b + 1 else b + 2
    while (c0 <= cmax) {
        if (!bad[c0.toInt()]) {
            histA[((n - 1) / c0).toInt()]++
            histB[((n - 1) / (2 * c0)).toInt()]++
        }
        c0 += 2
    }
    for (m in 1 until size) {
        if (histA[m] != 0L) total += 2 * histA[m] * latticeA(m.toLong())
        if (histB[m] != 0L) total += histB[m] * latticeB(m.toLong())
    }
    return total
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    println("== 1. 题面样例与中间规模（两条独立路径互证）==")
    // 前两级是题面给定的样例值；后三级来自 brute-force.kt 的独立实现（除数枚举/剪枝暴力）
    val sizes = longArrayOf(100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L)
    val expect = longArrayOf(92L, 1677L, 24385L, 320471L, 3969774L, 47345573L)
    for (i in sizes.indices) {
        val n = sizes[i]
        val a = countMethod1(n)
        val b = countMethod2(n)
        check(a == b && a == expect[i]) { "N=$n：路径 1 = $a，路径 2 = $b，期望 ${expect[i]}" }
        val tag = if (i == 0 || i == 3) "题面样例" else "中间规模"
        println("b+d < $n：$a（$tag，两条路径一致）")
    }

    println()
    println("== 2. 全尺寸 b+d < 10^8 ==")
    val N = 100_000_000L
    val ans1 = countMethod1(N)
    val ans2 = countMethod2(N)
    check(ans1 == ans2) { "全尺寸不一致：$ans1 vs $ans2" }
    check(ans1 >= 100_000_000L && ans1 < 1_000_000_000L) { "答案量级异常：$ans1" }

    println()
    println("== 3. 计时 ==")
    val ms1 = bestOf3("路径 1（(x,y) 直方图 + Möbius 计数）", ans1) { countMethod1(N) }
    val ms2 = bestOf3("路径 2（对 C 求和 + 网点计数）", ans2) { countMethod2(N) }

    println()
    println("== 4. 结果 ==")
    println("b+d < 100 000 000 的三元组 (a,b,d) 个数 = $ans1")
    println("路径 1 ${"%.3f".format(ms1)} ms；路径 2 ${"%.3f".format(ms2)} ms（check() 全部通过）")
}
