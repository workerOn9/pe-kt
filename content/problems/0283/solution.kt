#!/usr/bin/env kotlin
/**
 * Project Euler 283 — Integer Sided Triangles with Integral Area/perimeter Ratio
 * （整数边三角形与整数面积周长比）
 *
 * 题目
 * ────
 * 6-8-10 的面积与周长都是 24，面积/周长 = 1；13-14-15 的周长 42、面积 84，比值 = 2。
 * 求所有整数边三角形中「面积/周长为不超过 1000 的正整数」者，其周长之和。
 *
 * 建模
 * ────
 * 内切圆把三角形分成三个以 r（内切圆半径）为高的三角形，故面积 A = r·s（s = 半周长）；
 * 于是 A/P = r·s/(2s) = r/2。所以「面积/周长 = 正整数 k」⟺「内切圆半径 r = 2k 为偶数」，
 * k ≤ 1000 即 r ∈ {2, 4, …, 2000}，共 1000 个候选内切圆半径。
 *
 * 切线长参数化：令 x = s − a、y = s − b、z = s − c（顶点到切点的三段切线长），则
 *   a = y + z,  b = z + x,  c = x + y,  P = 2(x + y + z),
 * 且 (a, b, c) ↔ {x, y, z} 是「整数边三角形 ↔ 正整数三元组」的双射（x = s − a 可唯一复原）。
 * Heron 公式给出 A² = s·x·y·z；又 A = r·s，两边平方后
 *
 *   r²(x + y + z) = x·y·z.                                                    (∗)
 *
 * 于是问题化为：对每个偶数 r = 2k ≤ 2000，求 (∗) 的正整数解的无序三元组 {x ≤ y ≤ z}，
 * 答案 = Σ 2(x + y + z)。
 *
 * 关键恒等式（把 (∗) 拆成两个约数之积）
 * ────────────────────────────────
 *   (xy − r²)(xz − r²) = x²yz − r²x(y+z) + r⁴
 *                      = x·(xyz) − r²x(y+z) + r⁴
 *                      = r²x(x+y+z) − r²x(y+z) + r⁴
 *                      = r²(x² + r²).
 *
 * 记 A = xy − r²、B = xz − r²、M_x = r²(x² + r²)，则 A·B = M_x 且 xy = A + r²、xz = B + r²。
 * 反向亦然：只要 A·B = M_x 且 x | A + r²、x | B + r²，令 y = (A+r²)/x、z = (B+r²)/x 就有
 * (xy−r²)(xz−r²) = r²(x²+r²)，展开即 (∗)。于是
 *
 *   固定 x 时 (∗) 的解 ↔ M_x 的约数对 A·B = M_x，满足 A ≡ B ≡ −r² (mod x)，
 *   对应 y = (A+r²)/x、z = (B+r²)/x；顺序 x ≤ y ≤ z ⟺ A ≥ x² − r² 且 A ≤ √(M_x)。
 *
 * 界与指数约束（把枚举域压到最小）
 * ────────────────────────────
 * 1) 把 (∗) 除以 xyz 得 1 = r²(1/(yz) + 1/(xz) + 1/(xy))；x ≤ y ≤ z 使中间项最小/最大可夹出
 *    r² ≤ xy ≤ 3r²，于是 x² ≤ xy ≤ 3r² ⟹ x ≤ ⌊√3·r⌋；又 A ≤ B ⟹ A ≤ √(M_x) = r√(x²+r²) ≤ 2r²。
 * 2) p-adic 约束（a = v_p(x)，b = v_p(r²)，c = v_p(A)）：由 A ≡ −r² (mod x) 即 A + r² = xy，
 *    比较两边 p 的指数得
 *      a ≤ b：v_p(A) ≥ a 且 v_p(B) ≥ a  ⟹ c ∈ [a, v_p(M) − a]；
 *      a > b：v_p(A) = v_p(B) = b 精确（并要求 v_p(M) = 2b，否则无解）。
 *    只有 p | r² 的素数需要约束：p | x 而 p ∤ r² 时 p ∤ x² + r²，自动满足。
 *
 * 主路径 A（约数驱动枚举）
 * ─────────────────────
 * 对每个 r = 2k（k ≤ 1000）、每个 x ≤ ⌊√3·r⌋：
 *   1) 分解 M_x = r²(x² + r²)：r² 每个 r 分解一次；x² + r² ≤ 4r² ≤ 1.6×10^7。注意 n = x² + r²
 *      中 ≡ 3 (mod 4) 的素因子必同时整除 x 与 r（−1 不是 mod p 二次剩余），因此试除只需
 *      2、所有 ≡ 1 (mod 4) 的素数，以及 r 的 ≡ 3 (mod 4) 素因子——比全素数表少一半；
 *   2) 用 DFS 枚举 M_x 中 ≤ √M_x 的约数 A，同时施加 (1)(2) 的界；
 *   3) 叶子处检查 x | A + r²、x | B + r²、x ≤ y ≤ z，通过则累加周长。
 * x 是三元组的最小元素（被 x ≤ y ≤ z 唯一确定）、A 又唯一对应 y，故无重复无遗漏。
 *
 * 复杂度
 * ──────
 * (r, x) 对总数 Σ_{k≤1000} √3·2k ≈ 1.73×10^6；每对一次 ≤ 4000 的试除分解 + 一次约数 DFS。
 * 全尺度候选（DFS 节点）约 2.7×10^8 次整数运算，实测 1.1–1.2 s（JIT 预热后）。
 * 对照的「窗口暴力」直接枚举 y ∈ [max(x, ⌈r²/x⌉), (r² + r√(r²+x²))/x] 需 ≈ 1.15×10^10
 * 次整除判定（全尺度不可行，只在 k ≤ 100 上对拍）。
 *
 * 验证
 * ────
 * 1. 题面样例：6-8-10 ⟹ (x,y,z) = {2,4,6}、r = 2、k = 1；13-14-15 ⟹ {6,7,8}、r = 4、k = 2；
 * 2. 独立枚举（窗口暴力）：k ≤ 100 直接按方程解 z = r²(x+y)/(xy−r²)，与主路径的周长多重集比对；
 * 3. 定义级暴力（Heron）：枚举 a ≤ b ≤ c、周长 ≤ 2000 的整数边三角形，用 16A² = P(P−2a)(P−2b)(P−2c)
 *    判定面积与 A ≡ 0 (mod P)，与主路径限制后的周长多重集逐个比对（含样例 24、42）；
 * 4. 结构自检：k ≤ 20 的全部解逐个验方程 xyz = r²(x+y+z) 与顺序 x ≤ y ≤ z；
 * 5. 全尺寸独立复核：content/problems/0283/brute-force.kt 的窗口暴力（另写一套实现）给出同一总和；
 *    另有 Python 独立实现（同为 64.2 s）与公开答案表 28038042525570324 双重旁证。
 *
 * 答案：28038042525570324
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0283/solution.kt -d /tmp/kc-0283
 * java -Xmx4g -cp /tmp/kc-0283:<kotlin-stdlib> SolutionKt
 */

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

private const val K_MAX = 1000               // 面积/周长 k ≤ 1000
private const val SMALL_K = 100              // 窗口暴力对拍的规模
private const val SMALL_P = 2000L            // 定义级 Heron 暴力的周长上界（对拍用）

// ─────────────────────────────── 基础工具 ───────────────────────────────

/** 埃氏筛，返回 ≤ limit 的素数（升序）。 */
private fun primesUpTo(limit: Int): IntArray {
    val composite = BooleanArray(limit + 1)
    val out = ArrayList<Int>()
    for (i in 2..limit) {
        if (!composite[i]) {
            out.add(i)
            var j = i.toLong() * i
            while (j <= limit) {
                composite[j.toInt()] = true
                j += i
            }
        }
    }
    return out.toIntArray()
}

/** 整数平方根（floor）。M ≤ 4r⁴ ≤ 6.4×10^13，double 起步 + 修正即可。 */
private fun isqrt(n: Long): Long {
    var s = sqrt(n.toDouble()).toLong()
    while (s > 0 && s * s > n) s--
    while ((s + 1) * (s + 1) <= n) s++
    return s
}

/**
 * 把 n 的质因数分解合并进 (ps, es) 的前 cnt 项（相同素数累加指数），返回新的项数。
 * n ≤ 4r² ≤ 1.6×10^7：只需试除 [tp] 中前 [tcount] 个素数，试除后剩余 > 1 的部分必为素数。
 */
private fun factorMerge(n0: Long, tp: IntArray, tcount: Int, ps: IntArray, es: IntArray, cnt0: Int): Int {
    var n = n0
    var cnt = cnt0
    for (i in 0 until tcount) {
        val p = tp[i]
        if (p.toLong() * p > n) break
        if (n % p == 0L) {
            var e = 0
            while (n % p == 0L) {
                n /= p
                e++
            }
            var idx = -1
            for (j in 0 until cnt) if (ps[j] == p) { idx = j; break }
            if (idx >= 0) es[idx] += e else { ps[cnt] = p; es[cnt] = e; cnt++ }
        }
    }
    if (n > 1) {
        val p = n.toInt()
        var idx = -1
        for (j in 0 until cnt) if (ps[j] == p) { idx = j; break }
        if (idx >= 0) es[idx] += 1 else { ps[cnt] = p; es[cnt] = 1; cnt++ }
    }
    return cnt
}

/** 一次枚举的汇总：周长之和、解的个数、可选的周长多重集（对拍用）。 */
private class Result(val total: Long, val count: Long, private val list: LongArray?) {
    fun sortedList(): LongArray = list!!.copyOf().also { it.sort() }
}

// ───────────────────── 主路径 A：约数驱动枚举（全尺寸） ─────────────────────

/**
 * 单个 (r, x) 的约数 DFS。per-(r,x) 状态放字段里，递归只传 (idx, cur)；
 * 最后两层（占比最大的部分）直接展开成双重循环、叶子测试内联，避免函数调用开销。
 */
private class Walker(wantList: Boolean) {
    val ps = IntArray(32)                       // M_x 的不同素因子
    val hi = IntArray(32)                       // 指数上界
    val loX = IntArray(32)                      // 指数下界
    var cnt = 0
    var cap = 0L                                // A ≤ cap = √M_x
    var lo = 1L                                 // A ≥ lo = max(1, x² − r²)
    var m = 0L
    var r2i = 0
    var xi = 1
    var total = 0L
    var count = 0L
    var pMax = Long.MAX_VALUE                      // 周长上界（对拍用；主路径为 Long.MAX_VALUE）
    private var buf: LongArray? = if (wantList) LongArray(1 shl 12) else null
    private var size = 0

    private fun pow(p: Int, e: Int): Long {
        var v = 1L
        repeat(e) { v *= p }
        return v
    }

    /** 叶子：A = a；检查两个同余与顺序 x ≤ y ≤ z，通过则累加周长。 */
    fun leaf(a: Long) {
        val ai = a.toInt()
        if ((ai + r2i) % xi != 0) return
        val b = m / a
        if ((b + r2i) % xi != 0L) return
        val y = (a + r2i) / xi
        val z = (b + r2i) / xi
        if (y < xi || z < y) return
        val perimeter = 2 * (xi + y + z)
        if (perimeter > pMax) return
        total += perimeter
        count++
        val bb = buf ?: return
        if (size == bb.size) {                  // 缓冲按需扩容（对拍用）
            val bigger = LongArray(bb.size * 2)
            System.arraycopy(bb, 0, bigger, 0, bb.size)
            buf = bigger
            bigger[size++] = perimeter
        } else {
            bb[size++] = perimeter
        }
    }

    fun dfs(idx: Int, cur: Long) {
        val ps = ps
        val hi = hi
        val loX = loX
        if (idx == cnt - 2) {                   // 展开最后两层
            val p1 = ps[idx]
            val p2 = ps[idx + 1]
            var f1 = loX[idx]
            var v1 = if (f1 > 0) cur * pow(p1, f1) else cur
            while (f1 <= hi[idx]) {
                if (v1 > cap) return
                var f2 = loX[idx + 1]
                var v2 = if (f2 > 0) v1 * pow(p2, f2) else v1
                while (f2 <= hi[idx + 1]) {
                    if (v2 > cap) break
                    if (v2 >= lo) leaf(v2)
                    v2 *= p2
                    f2++
                }
                v1 *= p1
                f1++
            }
            return
        }
        if (idx == cnt - 3) {                   // 展开倒数三层
            val p1 = ps[idx]
            val p2 = ps[idx + 1]
            val p3 = ps[idx + 2]
            var f1 = loX[idx]
            var v1 = if (f1 > 0) cur * pow(p1, f1) else cur
            while (f1 <= hi[idx]) {
                if (v1 > cap) return
                var f2 = loX[idx + 1]
                var v2 = if (f2 > 0) v1 * pow(p2, f2) else v1
                while (f2 <= hi[idx + 1]) {
                    if (v2 > cap) break
                    var f3 = loX[idx + 2]
                    var v3 = if (f3 > 0) v2 * pow(p3, f3) else v2
                    while (f3 <= hi[idx + 2]) {
                        if (v3 > cap) break
                        if (v3 >= lo) leaf(v3)
                        v3 *= p3
                        f3++
                    }
                    v2 *= p2
                    f2++
                }
                v1 *= p1
                f1++
            }
            return
        }
        val p = ps[idx]
        var f = loX[idx]
        var v = if (f > 0) cur * pow(p, f) else cur
        while (f <= hi[idx]) {
            if (v > cap) return
            dfs(idx + 1, v)
            v *= p
            f++
        }
    }

    fun result(): Result = Result(total, count, if (buf == null) null else buf!!.copyOf(size))
}

/**
 * 主路径：k = 1 … kMax（r = 2k）枚举 (∗) 的全部解。
 * [pMax] 限制周长（与定义级暴力对拍）；[wantList] 收集周长多重集。
 */
private fun scanDivisors(kMax: Int, pMax: Long = Long.MAX_VALUE, wantList: Boolean = false): Result {
    val primes = primesUpTo(4000)               // x² + r² ≤ 4r² ≤ 1.6×10^7 ⟹ 试除到 4000
    // 试除素数表 = 2 + 所有 ≡ 1 (mod 4) 的素数（3 (mod 4) 的素数按 r 动态补入）；
    // base 是「干净副本」，每个 r 都要从它重新拷贝（排序会打乱 testPrimes 的内容）
    val p1mod4 = ArrayList<Int>()
    for (p in primes) if (p % 4 == 1) p1mod4.add(p)
    val base = IntArray(p1mod4.size + 1)
    base[0] = 2
    for (i in p1mod4.indices) base[i + 1] = p1mod4[i]
    val testPrimes = IntArray(base.size + 8)
    val baseCount = base.size

    val w = Walker(wantList)
    val psR = IntArray(16)
    val esR = IntArray(16)

    for (k in 1..kMax) {
        val r = 2L * k
        val r2 = r * r
        val r2i = r2.toInt()
        val cR = factorMerge(r2, primes, primes.size, psR, esR, 0)
        System.arraycopy(base, 0, testPrimes, 0, baseCount)
        var tc = baseCount
        for (i in 0 until cR) if (psR[i] % 4 == 3) testPrimes[tc++] = psR[i]
        java.util.Arrays.sort(testPrimes, 0, tc)

        var x = 1L
        while (x * x <= 3 * r2) {               // x² ≤ xy ≤ 3r²
            val n = r2 + x * x
            val ps = w.ps
            val hi = w.hi
            val loX = w.loX
            System.arraycopy(psR, 0, ps, 0, cR)
            System.arraycopy(esR, 0, hi, 0, cR)
            val cnt = factorMerge(n, testPrimes, tc, ps, hi, cR)

            // p-adic 指数约束
            var ok = true
            for (i in 0 until cnt) {
                if (i >= cR) { loX[i] = 0; continue }   // n 的素因子与 x 互素 ⇒ 无约束
                val p = psR[i]
                if (x % p != 0L) { loX[i] = 0; continue }
                var t = x
                var a = 0
                while (t % p == 0L) {
                    t /= p
                    a++
                }
                val b = esR[i]
                if (a <= b) {
                    loX[i] = a
                    val newHi = hi[i] - a
                    if (newHi < a) { ok = false; break }
                    hi[i] = newHi
                } else {
                    if (hi[i] != 2 * b) { ok = false; break }
                    loX[i] = b
                    hi[i] = b
                }
            }
            if (ok) {
                w.pMax = pMax
                w.cnt = cnt
                w.cap = isqrt(r2 * n)
                w.lo = if (x > r) x * x - r2 else 1L
                w.m = r2 * n
                w.r2i = r2i
                w.xi = x.toInt()
                when {
                    cnt == 1 -> {                   // 单个素因子：直接一重循环
                        val p0 = ps[0]
                        var f = loX[0]
                        var v = 1L
                        repeat(f) { v *= p0 }
                        while (f <= hi[0]) {
                            if (v > w.cap) break
                            if (v >= w.lo) w.leaf(v)
                            v *= p0
                            f++
                        }
                    }
                    else -> w.dfs(0, 1L)
                }
            }
            x++
        }
    }
    return w.result()
}

// ───────────── 对拍路径 B：窗口暴力（方程 z = r²(x+y)/(xy−r²)，小规模） ─────────────

/**
 * 直接照方程 (∗) 解出 z：固定 r、x 后 y 的窗口为 [max(x, ⌈r²/x⌉), (r² + r√(r²+x²))/x]
 * （下界来自 xy ≥ r²，上界来自 z ≥ y），逐个 y 检查整除。用于与主路径对拍。
 */
private fun scanNaive(kMax: Int, wantList: Boolean = false): Result {
    val buf = if (wantList) ArrayList<Long>() else null
    var total = 0L
    var count = 0L
    for (k in 1..kMax) {
        val r = 2L * k
        val r2 = r * r
        var x = 1L
        while (x * x <= 3 * r2) {
            var y = max(x, (r2 + x - 1) / x)
            val yMax = (r2 + isqrt(r2 * (r2 + x * x))) / x + 1     // +1 兜住取整边界
            while (y <= yMax) {
                val a = x * y - r2
                if (a > 0) {
                    val num = r2 * (x + y)
                    if (num % a == 0L) {
                        val z = num / a
                        if (z >= y) {
                            total += 2 * (x + y + z)
                            count++
                            buf?.add(2 * (x + y + z))
                        }
                    }
                }
                y++
            }
            x++
        }
    }
    return Result(total, count, buf?.toLongArray())
}

/** 以回调方式列出 k 对应的全部解（窗口暴力，供结构自检逐解验方程）。 */
private inline fun forEachSolutionNaive(k: Int, action: (Long, Long, Long) -> Unit) {
    val r = 2L * k
    val r2 = r * r
    var x = 1L
    while (x * x <= 3 * r2) {
        var y = max(x, (r2 + x - 1) / x)
        val yMax = (r2 + isqrt(r2 * (r2 + x * x))) / x + 1
        while (y <= yMax) {
            val a = x * y - r2
            if (a > 0) {
                val num = r2 * (x + y)
                if (num % a == 0L) {
                    val z = num / a
                    if (z >= y) action(x, y, z)
                }
            }
            y++
        }
        x++
    }
}

// ──────────── 对拍路径 C：定义级 Heron 暴力（枚举 a ≤ b ≤ c，周长 ≤ pMax） ────────────

/**
 * 完全按定义枚举：a ≤ b ≤ c、周长 P ≤ pMax，16A² = P(P−2a)(P−2b)(P−2c)，再按
 * A ≡ 0 (mod P) 判定面积/周长为整数（k = A/P ∈ [1, 1000]）。只走 P 为偶数的分支。
 */
private fun scanHeron(pMax: Long, wantList: Boolean = false): Result {
    val buf = if (wantList) ArrayList<Long>() else null
    var total = 0L
    var count = 0L
    var a = 1L
    while (3 * a <= pMax) {
        var b = a
        while (a + 2 * b <= pMax) {
            val cMax = min(pMax - a - b, a + b - 1)
            var c = b
            while (c <= cMax) {
                val p = a + b + c
                if (p % 2 == 0L) {
                    val s = p / 2
                    val q = s * (s - a) * (s - b) * (s - c)         // = A²
                    var area = sqrt(q.toDouble()).toLong()
                    while (area > 0 && area * area > q) area--
                    while ((area + 1) * (area + 1) <= q) area++
                    if (area * area == q && area % p == 0L) {
                        val kk = area / p
                        if (kk in 1..K_MAX.toLong()) {
                            total += p
                            count++
                            buf?.add(p)
                        }
                    }
                }
                c++
            }
            b++
        }
        a++
    }
    return Result(total, count, buf?.toLongArray())
}

// ─────────────────────────────── 计时与输出 ───────────────────────────────

private fun bestOf3(tag: String, expectedTotal: Long, f: () -> Result): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out.total == expectedTotal) { "$tag 第 ${round + 1} 轮结果漂移：${out.total} ≠ $expectedTotal" }
        if (ms < best) best = ms
    }
    println("  $tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

private fun sameMultiset(a: Result, b: Result): Boolean = a.sortedList().contentEquals(b.sortedList())

fun main() {
    // ---------- 1. 题面样例 ----------
    run {
        val x = 2L; val y = 4L; val z = 6L                                   // 切线长 {2,4,6}
        val sides = longArrayOf(y + z, z + x, x + y).also { it.sort() }
        check(sides.contentEquals(longArrayOf(6, 8, 10))) { "6-8-10 的切线长应为 {2,4,6}" }
        check(x * y * z == 4 * (x + y + z)) { "切线长 {2,4,6} 应满足 r = 2 的方程 (∗)" }
        println("样例 6-8-10：切线长 {2,4,6}，周长 ${2 * (x + y + z)}，面积/周长 = r/2 = 1 ✓")
    }
    run {
        val x = 6L; val y = 7L; val z = 8L                                   // 切线长 {6,7,8}
        val sides = longArrayOf(y + z, z + x, x + y).also { it.sort() }
        check(sides.contentEquals(longArrayOf(13, 14, 15))) { "13-14-15 的切线长应为 {6,7,8}" }
        check(x * y * z == 16 * (x + y + z) && (x + y + z) * x * y * z == 84L * 84L) {
            "切线长 {6,7,8} 应满足 r = 4 的方程且 Heron 面积为 84"
        }
        println("样例 13-14-15：切线长 {6,7,8}，周长 ${2 * (x + y + z)}，面积 84，面积/周长 = 2 ✓")
    }

    // ---------- 2. 主路径（全尺寸） ----------
    val t0 = System.nanoTime()
    val main = scanDivisors(K_MAX)
    val mainMs = (System.nanoTime() - t0) / 1e6
    println("主路径（约数驱动枚举，k ≤ $K_MAX）：解数 ${main.count}，周长之和 ${main.total}（首次运行 ${"%.0f".format(mainMs)} ms）")

    // ---------- 3. 主路径计时（放在对拍之前！wantList=true 的同代码调用会改变 JIT 编译形态，
    //                使热循环慢 ~40%，见 analysis.md「关键教训」） ----------
    scanDivisors(K_MAX)
    val msMain = bestOf3("主路径 约数驱动枚举（全尺寸 k ≤ $K_MAX）", main.total) { scanDivisors(K_MAX) }

    // ---------- 4. 对拍 1：窗口暴力（k ≤ SMALL_K） ----------
    val naiveSmall = scanNaive(SMALL_K, wantList = true)
    val divSmall = scanDivisors(SMALL_K, wantList = true)
    check(sameMultiset(naiveSmall, divSmall)) {
        "窗口暴力与主路径在 k ≤ $SMALL_K 上不一致：${naiveSmall.count} vs ${divSmall.count} 个解"
    }
    println("对拍 1（k ≤ $SMALL_K，窗口暴力 vs 主路径）：${naiveSmall.count} 个解的周长多重集完全一致 ✓")

    // ---------- 5. 对拍 2：定义级 Heron 暴力（P ≤ SMALL_P） ----------
    val heron = scanHeron(SMALL_P, wantList = true)
    val divP = scanDivisors(K_MAX, pMax = SMALL_P, wantList = true)
    check(sameMultiset(heron, divP)) {
        "Heron 暴力与主路径在 P ≤ $SMALL_P 上不一致：${heron.count} vs ${divP.count} 个解"
    }
    val heronList = heron.sortedList()
    check(heronList.contains(24L) && heronList.contains(42L)) { "Heron 暴力应包含样例周长 24 与 42" }
    println("对拍 2（P ≤ $SMALL_P，Heron 定义级暴力 vs 主路径）：${heron.count} 个解的周长多重集完全一致（含 24、42）✓")

    // ---------- 6. 结构自检：k ≤ 20 的每个解逐个验方程与顺序 ----------
    run {
        var checked = 0L
        for (k in 1..20) {
            val r2 = (2L * k) * (2L * k)
            forEachSolutionNaive(k) { x, y, z ->
                check(x * y * z == r2 * (x + y + z)) { "方程不成立：k=$k ($x,$y,$z)" }
                check(x <= y && y <= z) { "顺序错误：k=$k ($x,$y,$z)" }
                checked++
            }
        }
        val twin = scanNaive(20)
        check(checked == twin.count) { "k ≤ 20 解数不一致：$checked vs ${twin.count}" }
        println("结构自检：k ≤ 20 的 $checked 个解逐个验方程 xyz = r²(x+y+z) 与顺序 x ≤ y ≤ z ✓")
    }

    // ---------- 7. 另两条路径的计时（与主路径不共享代码，不受上面 profile 影响） ----------
    scanNaive(SMALL_K)
    val msNaive = bestOf3("窗口暴力（k ≤ $SMALL_K，对拍口径）", naiveSmall.total) { scanNaive(SMALL_K) }
    scanHeron(SMALL_P)
    val msHeron = bestOf3("Heron 定义级暴力（P ≤ $SMALL_P）", heron.total) { scanHeron(SMALL_P) }

    // ---------- 8. 输出 ----------
    println()
    println("答案 = ${main.total}（整数边三角形中面积/周长为 ≤ $K_MAX 的正整数者，周长之和）")
    println("汇总：主路径 ${"%.1f".format(msMain)} ms；窗口暴力 ${"%.1f".format(msNaive)} ms（k ≤ $SMALL_K）；" +
        "Heron 暴力 ${"%.1f".format(msHeron)} ms（P ≤ $SMALL_P）；解数 ${main.count}")
    println("check() 全部通过")
}
