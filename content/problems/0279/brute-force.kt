#!/usr/bin/env kotlin
/**
 * Project Euler 279 — Triangles with Integral Sides and an Integral Angle：暴力对照
 * （独立实现，与 solution.kt 不共享核心代码）
 *
 * 三条路径：
 *
 *   路径 1（定义级，Θ(N³)）：枚举整数边三角形 a ≤ b ≤ c，对三个角分别用精确整数等式
 *   判定 60°/90°/120°（s² = u²+v²−uv、u²+v²、u²+v²+uv）。这条路径是 meta 的
 *   bruteForceBaselineMs 口径（N = 2000）。
 *
 *   路径 2（配对扫描，O(N²) + HashSet）：对每一对边 (x, y)，三种角度方程都把第三边
 *   c 完全确定（c² = x²+y²−xy / x²+y² / x²+y²+xy），整数则得到一个合法三角形
 *   （三角形不等式自动成立：|x−y| < c < x+y 对三个方程分别由 xy > 0、x²+y² < (x+y)²、
 *   xy < 2xy 保证）。每个三角形会被它的若干边对重复找到，用排序三边的 HashSet 去重。
 *   不需要任何参数化理论，可在 N = 3000 上独立复核三类计数。
 *
 *   路径 3（浮点角度数值校验，N ≤ 400）：对每个三角形的三个角用余弦定理 + acos 求度数，
 *   验证「存在整数度的角」⟺「存在 60°/90°/120° 的角」——即 Niven 定理在可暴力规模上的
 *   数值旁证（小边长的双精度 acos 误差 ≪ 1e-9）。
 *
 * 暴力在 N = 10^8 完全不可行：路径 1 的内层判断数约 N³/48 ≈ 2×10^22，路径 2 也要
 * N²/8 ≈ 1.25×10^15 对——可行的完整规模解法见 solution.kt（三段参数化 + 缩放计数）。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0279/brute-force.kt -d /tmp/kc-0279-bf
 * java -cp /tmp/kc-0279-bf:<kotlin-stdlib-2.1.21.jar> Brute_forceKt
 * （文件名的 `-` 会被 JVM facade 规则 mangle 成 `_`：类名是 Brute_forceKt。）
 */

/** 角度类型位掩码：1 = 90°，2 = 60°，4 = 120°，0 = 无（s 为对边，u、v 为夹边）。 */
private fun angleType(s: Long, u: Long, v: Long): Int {
    val s2 = s * s
    val base = u * u + v * v
    return when (s2) {
        base -> 1
        base - u * v -> 2
        base + u * v -> 4
        else -> 0
    }
}

private fun triangleTypes(a: Long, b: Long, c: Long): Int =
    angleType(c, a, b) or angleType(b, a, c) or angleType(a, b, c)

// ─────────── 路径 1：定义级枚举（基线路径） ───────────
/** 返回 [90°, 60°, 120°, 至少一个整数角的三角形数]。 */
private fun bruteTriple(N: Long): LongArray {
    val r = LongArray(4)
    var a = 1L
    while (3 * a <= N) {
        var b = a
        while (a + 2 * b <= N) {
            var c = b
            while (a + b + c <= N) {
                if (a + b > c) {
                    val t = triangleTypes(a, b, c)
                    if (t != 0) {
                        r[3]++
                        if (t and 1 != 0) r[0]++
                        if (t and 2 != 0) r[1]++
                        if (t and 4 != 0) r[2]++
                    }
                }
                c++
            }
            b++
        }
        a++
    }
    return r
}

// ─────────── 路径 2：配对扫描 + HashSet 去重 ───────────
private fun isqrt(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return r
}

private fun pack(a: Long, b: Long, c: Long): Long {
    // 三边排序后用 12 位字段打包（要求边长 < 4096，N ≤ 8000 时成立）
    var x = a
    var y = b
    var z = c
    if (x > y) { val t = x; x = y; y = t }
    if (y > z) { val t = y; y = z; z = t }
    if (x > y) { val t = x; x = y; y = t }
    return (x shl 24) or (y shl 12) or z
}

/** 返回 [90°, 60°, 120°, 总数]；o[i] 是第 i 类的去重集合。 */
private fun brutePair(N: Long): Pair<LongArray, Array<HashSet<Long>>> {
    val sets = Array(3) { HashSet<Long>() }
    var x = 1L
    while (2 * x <= N) {                      // 任意一边 < N/2
        var y = x
        while (2 * y <= N) {
            val xy = x * y
            val base = x * x + y * y
            // 90°：c² = x² + y²
            var c = isqrt(base)
            if (c * c == base && x + y + c <= N) sets[0].add(pack(x, y, c))
            // 60°：c² = x² + y² − xy
            c = isqrt(base - xy)
            if (c * c == base - xy && x + y + c <= N) sets[1].add(pack(x, y, c))
            // 120°：c² = x² + y² + xy
            c = isqrt(base + xy)
            if (c * c == base + xy && x + y + c <= N) sets[2].add(pack(x, y, c))
            y++
        }
        x++
    }
    val r = LongArray(4)
    for (i in 0..2) r[i] = sets[i].size.toLong()
    r[3] = (sets[0] + sets[1] + sets[2]).size.toLong()
    return r to sets
}

// ─────────── 路径 3：浮点角度数值校验（Niven 数值旁证） ───────────
/** 返回不一致的条目列表（空 = 通过）。 */
private fun numericAngleCheck(N: Long): List<String> {
    val bad = ArrayList<String>()
    var a = 1L
    while (3 * a <= N) {
        var b = a
        while (a + 2 * b <= N) {
            var c = b
            while (a + b + c <= N) {
                if (a + b > c) {
                    val exact = triangleTypes(a, b, c)
                    var numInteger = 0
                    var allInSet = true
                    for ((s, u, v) in listOf(Triple(c, a, b), Triple(b, a, c), Triple(a, b, c))) {
                        val cos = (u * u + v * v - s * s).toDouble() / (2.0 * u * v)
                        val deg = Math.toDegrees(Math.acos(cos.coerceIn(-1.0, 1.0)))
                        val nearest = Math.round(deg).toDouble()
                        if (Math.abs(deg - nearest) < 1e-9) {          // 数值上为整数度
                            numInteger++
                            if (nearest != 60.0 && nearest != 90.0 && nearest != 120.0) allInSet = false
                        }
                    }
                    val exactHas = exact != 0
                    if ((numInteger > 0) != exactHas || (exactHas && !allInSet)) {
                        bad.add("($a,$b,$c) 精确=$exact 数值整数角数=$numInteger")
                    }
                }
                c++
            }
            b++
        }
        a++
    }
    return bad
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 路径 1 / 路径 2 在小规模逐值一致 ----------
    val ns = listOf(50L, 100L, 200L, 500L, 1000L, 2000L, 3000L)
    for (n in ns) {
        val t = bruteTriple(n)
        val (p, _) = brutePair(n)
        check(t[0] == p[0] && t[1] == p[1] && t[2] == p[2] && t[3] == p[3]) {
            "N=$n：定义级 ${t.toList()} ≠ 配对扫描 ${p.toList()}"
        }
        println("N=$n：90° ${t[0]}，60° ${t[1]}，120° ${t[2]}，合计 ${t[3]}（两条暴力一致）")
    }

    // ---------- 路径 3：数值角度校验 ----------
    val bad = numericAngleCheck(400L)
    check(bad.isEmpty()) { "数值校验失败：${bad.take(5)}" }
    println("数值角度校验（N=400，双精度 acos）：所有整数度的角都落在 {60°, 90°, 120°}，")
    println("  且「存在整数度角」与精确判定完全一致（Niven 定理的数值旁证）")

    // ---------- 与参数化公式对拍（参数化计数写在本文件内，独立于 solution.kt） ----------
    println("与参数化三项计数对拍（同一公式，独立代码）：")
    for (n in listOf(1000L, 2000L, 3000L)) {
        val t = bruteTriple(n)
        val got = longArrayOf(param90(n), param60(n), param120(n))
        check(t[0] == got[0] && t[1] == got[1] && t[2] == got[2]) {
            "N=$n：暴力 ${t.toList()} ≠ 参数化 ${got.toList()}"
        }
        println("  N=$n：90° ${got[0]}，60° ${got[1]}，120° ${got[2]}，合计 ${got[0] + got[1] + got[2]}")
    }

    // ---------- 计时 ----------
    bruteTriple(1000L); brutePair(1500L)
    val ms1 = bestOf3("路径 1：定义级枚举（N=2000，基线路径）", bruteTriple(2000L)[3]) { bruteTriple(2000L)[3] }
    val ms2 = bestOf3("路径 2：配对扫描 + 去重（N=3000）", brutePair(3000L).first[3]) { brutePair(3000L).first[3] }

    println()
    println("汇总：路径 1 N=2000 ${"%.1f".format(ms1)} ms；路径 2 N=3000 ${"%.1f".format(ms2)} ms")
    println("外推：路径 1 内层判断 ~N³/48，N=10^8 需 ~2×10^22 次；路径 2 需 ~N²/8 = 1.25×10^15 对。")
    println("暴力只能钉到 N=3000 的规模；完整规模用 solution.kt 的三段参数化求和（O(N)，本机约 96 ms）")
    println("check() 全部通过")
}

// ─────────── 参数化三项计数（供对拍用；独立重写，与 solution.kt 无关） ───────────
private fun gcd2(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

private fun param90(N: Long): Long {
    var total = 0L
    var m = 2L
    while (2 * m * (m + 1) <= N) {
        for (n in (m % 2 + 1) until m step 2) {
            val p = 2 * m * (m + n)
            if (p > N) break
            if (gcd2(m, n) == 1L) total += N / p
        }
        m++
    }
    return total
}

private fun param60(N: Long): Long {
    var total = 0L
    var m = 2L
    while (2 * m * m <= 3 * N) {
        for (n in ((m + 1) / 2) until m) {
            if (gcd2(m, n) != 1L) continue
            val div = if ((m + n) % 3L == 0L) 3L else 1L
            val p = (2 * m * m + m * n - n * n) / div
            if (p <= N) total += N / p
        }
        m++
    }
    return total
}

private fun param120(N: Long): Long {
    var total = 0L
    var m = 2L
    while (2 * m * m < N) {
        for (n in 1L until m) {
            val p = 2 * m * m + 3 * m * n + n * n
            if (p > N) break
            if ((m - n) % 3L != 0L && gcd2(m, n) == 1L) total += N / p
        }
        m++
    }
    return total
}
