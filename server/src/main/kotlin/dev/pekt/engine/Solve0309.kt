package dev.pekt.engine

/**
 * PE 309 — Integer Ladders（整数梯子）：巷宽 w，两架梯子长 x、y（0 < x < y），交叉点离地 h；
 * 求 0 < x < y < 1,000,000 中使 w 为整数的 (x, y, h) 组数。
 *
 * 推导（详见 content/problems/0309/solution.kt 头部与 0309/analysis.md）：
 *   令 a = √(x² − w²)、b = √(y² − w²) 为两根梯子各自在墙上的端点高度，相似性给出
 *       1/h = 1/a + 1/b ⟺ h = ab/(a+b) ⟺ (a−h)(b−h) = h²，
 *   且 h 是 a、b 的调和平均，恒有 h < min(a, b)。
 *   题面只要求 x、y、h、w 为整数，但 a、b 必为整数：令 A = a²、B = b² ∈ ℤ、s = a+b，
 *       A+B = s² − 2hs、AB = h²s² ⟹ √(AB) = [AB − h²(A+B)]/(2h²) ∈ ℚ ⟹ ab ∈ ℤ，
 *   于是 A、B 的平方自由部分相同：A = d·u²、B = d·v² 给出 h = ab/(a+b) = uv√d/(u+v) ∈ ℚ，
 *   迫使 d = 1。
 *   故 (a, w, x)、(b, w, y) 是两个共享直角边 w 的整数勾股数（x < y ⟺ a < b），条件退化为 (a+b) | ab。
 *
 * 做法：欧几里得参数化（m > n ≥ 1、gcd(m,n) = 1、奇偶不同）枚举 c = m²+n² < 10⁶ 的本原勾股数，
 * 再乘倍数 s（c·s < 10⁶），两条直角边分别尝试当巷宽 w 与竖边 a，约 3.96×10⁶ 条记录按 w 分桶；
 * 桶内所有无序对 a < b 判 (a+b) | ab，成立即一组答案（h = ab/(a+b) < b < y，无需另判）。
 *
 * 最终答案：210139 组（0 < x < y < 1,000,000）。
 */
internal fun solve0309Impl(): Long {
    fun gcd(a: Int, b: Int): Int {
        var x = a
        var y = b
        while (y != 0) { val t = x % y; x = y; y = t }
        return x
    }

    fun isqrt(v: Long): Long {
        if (v <= 0L) return 0L
        var r = Math.sqrt(v.toDouble()).toLong()
        while (r * r > v) r--
        while ((r + 1) * (r + 1) <= v) r++
        return r
    }

    val limit = 1_000_000
    // 按巷宽 w 分桶的勾股数表：head/next 是链式前向星，av[n] 竖边 a，cv[n] 斜边 c
    val head = IntArray(limit + 1) { -1 }
    var av = IntArray(1 shl 16)
    var cv = IntArray(1 shl 16)
    var nx = IntArray(1 shl 16)
    var n = 0
    fun push(w: Int, a: Int, c: Int) {
        if (n == av.size) { av = av.copyOf(n * 2); cv = cv.copyOf(n * 2); nx = nx.copyOf(n * 2) }
        av[n] = a; cv[n] = c; nx[n] = head[w]; head[w] = n; n++
    }

    for (m in 2..isqrt(limit.toLong()).toInt()) {
        val m2 = m * m
        for (nn in 1 until m) {
            if (gcd(m, nn) != 1) continue
            if (((m + nn) and 1) == 0) continue
            val cc = m2 + nn * nn
            if (cc >= limit) continue
            val p = m2 - nn * nn          // p、q 是两条直角边，谁当巷宽都行
            val q = 2 * m * nn
            var s = 1
            while (cc * s < limit) {
                push(p * s, q * s, cc * s)
                push(q * s, p * s, cc * s)
                s++
            }
        }
    }

    var count = 0L
    var ba = IntArray(1024)
    var bc = IntArray(1024)
    for (w in 1..limit) {
        var k = 0
        var i = head[w]
        while (i != -1) {
            if (k == ba.size) { ba = ba.copyOf(k * 2); bc = bc.copyOf(k * 2) }
            ba[k] = av[i]; bc[k] = cv[i]; k++; i = nx[i]
        }
        if (k < 2) continue
        val ord = IntArray(k) { it }.sortedBy { ba[it] }   // a 升序即斜边升序，保证 a < b ⟺ x < y
        for (p in 0 until k) {
            val a1 = ba[ord[p]].toLong()
            for (q in p + 1 until k) {
                val b1 = ba[ord[q]].toLong()
                if (a1 * b1 % (a1 + b1) == 0L) count++
            }
        }
    }
    return count
}

fun main() {
    val t0 = System.nanoTime()
    val answer = solve0309Impl()
    println("solve0309Impl() = " + answer + "  (%.3f ms)".format((System.nanoTime() - t0) / 1_000_000.0))
}
