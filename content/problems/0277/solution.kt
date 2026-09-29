#!/usr/bin/env kotlin
/**
 * Project Euler 277 — A Modified Collatz Sequence（修改版 Collatz 序列）
 *
 * 思路
 * ────
 * 记三种步为 D：a ↦ a/3（a ≡ 0 mod 3）、U：a ↦ (4a+2)/3（a ≡ 1）、d：a ↦ (2a−1)/3（a ≡ 2）。
 * 关键事实：每一步的归属只由当前项 mod 3 决定，而 a_n 是 a_1 的分母为 3^{n−1} 的线性式
 *
 *   a_n = (A_n·a_1 + B_n)/3^{n−1},   A_{n+1} = c_n A_n,  B_{n+1} = c_n B_n + d_n,
 *
 * 其中 (c,d) 按第 n 步的类型取 (1,0)/(4,2)/(2,−1)；c ∈ {1,2,4} 模 3 可逆。于是
 *
 *   「第 n 步是 s_n」 ⟺ a_n ≡ s_n (mod 3) ⟺ A_n a_1 + B_n ≡ 3^{n−1} s_n (mod 3^n),
 *
 * 是一条对 a_1 的同余式（模 3^n）。满足前 n 个字符的 a_1 恰好构成**一个模 3^n 的剩余类**：
 * 把模 3^n 的类提升到模 3^{n+1}，三个候选 r, r+3^n, r+2·3^n 给出的 a_{n+1} 模 3 互不相同
 * （a_1 每加 3^n，a_{n+1} 就加 A_{n+1}·3^n/3^n = A_{n+1} ≡ ±1 (mod 3)），恰有一个命中 s_{n+1}。
 * 所以「最小解」= 最终剩余类 mod 3^{|s|} 中严格大于 10^15 的最小元素（再逐项验证非退化）。
 *
 * 一个容易踩的坑：a_1 = 1 时序列**立即终止**（不产生任何步字符），所以「剩余类的规范代表元」
 * 可能是退化值；判定某个提升候选是否命中时要把同类元素换一个代表元再模拟（本文件在类内多试
 * 几个 m·3^{n} 偏移，见 residueByLifting）。
 *
 * 两条完全独立的路径：
 *   方法 A（主路径，纯 Long）：逐字符提升——已知前 i 位匹配的类 r mod 3^i，对三个提升候选各试
 *   若干个同类代表元，直接模拟前 i+1 步比对字符；留下来的类就是 r mod 3^{i+1}。
 *   方法 B（BigInteger 精确仿射跟踪）：按 (A_n, B_n) 递推精确维护线性式，每一步解同余
 *   A_n a_1 ≡ 3^{n−1} s_n − B_n (mod 3^n)，并与上一层剩余类断言相容。
 *
 * 复杂度
 * ──────
 * k = |s| = 30：方法 A 每个字符 3 个候选、每个候选 O(i) 步模拟（外加少数类内偏移），约 O(k³/2)
 * 量级的最坏尝试、实际常数极小；方法 B O(k) 次 BigInteger 运算。实测都在毫秒级。
 * 对照：直接扫描 a_1 的密度是 3^{−30}，平均要走过 ~10^14 个候选（见 brute-force.kt 实测速率与外推）。
 *
 * 验证
 * ────
 * 1. 题面锚点：231 的步串 = "DdDddUUdDD"；1004064 的步串以 "DdDddUUdDD" 开头且是 > 10^6 的最小解
 *    （扫描确认）；1004064 的完整步串 = "DdDddUUdDDDdUDUUUdDdUUDDDUdDD"；
 * 2. 方法 A 与方法 B 对目标串的全部 30 个前缀给出的剩余类、以及「> 10^15 的最小解」逐一一致；
 * 3. 最终答案回代模拟：前 30 步恰为目标串；答案 − 3^30 = 920086261029661 ≤ 10^15（类中前一个元素
 *    不满足下界），故它是「> 10^15 的最小解」；
 * 4. brute-force.kt 独立做：在 [1, 10^7] 穷举，验证「前缀匹配（前 12 位）⟺ 落在剩余类 mod 3^12」；
 *    并实测全串扫描速率，外推直接暴力需 ~10^14 个候选（不可行）。
 *
 * 答案
 * ────
 * 1125977393124310（本机实跑，方法 A、方法 B 一致）
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0277/solution.kt -d /tmp/kc-0277
 * java -cp /tmp/kc-0277:<kotlin-stdlib> SolutionKt
 */

import java.math.BigInteger

private const val TARGET = "UDDDUdddDDUDDddDdDddDDUDDdUUDd"
private const val LOWER_BOUND = 1_000_000_000_000_000L   // 10^15：答案要求严格大于它

/** 走一步：返回 (步字符, 下一项)。 */
private fun step(a: Long): Pair<Char, Long> = when (a % 3L) {
    0L -> 'D' to a / 3
    1L -> 'U' to (4 * a + 2) / 3
    else -> 'd' to (2 * a - 1) / 3
}

/** 从 a 出发产生的前 len 个步字符；序列在 1 处终止时返回的串会更短。 */
private fun stepString(a: Long, len: Int): String {
    val sb = StringBuilder()
    var x = a
    while (sb.length < len && x != 1L) {
        val (c, y) = step(x)
        sb.append(c)
        x = y
    }
    return sb.toString()
}

/** 序列是否以 s 开头（不建串、逐字符早退）。注意 a = 1 时序列立即终止，不算匹配。 */
private fun beginsWith(a: Long, s: String): Boolean {
    var x = a
    for (i in s.indices) {
        if (x == 1L) return false
        val r = x % 3L
        val c = if (r == 0L) 'D' else if (r == 1L) 'U' else 'd'
        if (c != s[i]) return false
        x = when (c) {
            'D' -> x / 3
            'U' -> (4 * x + 2) / 3
            else -> (2 * x - 1) / 3
        }
    }
    return true
}

/**
 * 方法 A：逐字符提升。已知「前 i 位匹配」的类为 r mod 3^i，枚举三个提升候选
 * r + t·3^i (t = 0,1,2)。判定候选所属的**类**是否命中时，对该类的若干代表元
 * cand + m·3^{i+1}（m 从小到大）做模拟：规范代表元可能是退化值（如 a_1 = 1 直接终止），
 * 换一个代表元即可；而错的类在任何非退化代表元上都会在第 i+1 位给出不同字符。
 * 类内退化代表元至多 i+1 个（每个 n ≤ i+1 至多一个 a_1 使 a_n = 1），故试 m = 0…i+2 足够。
 */
private fun residueByLifting(s: String): Long {
    var r = 0L
    var mod = 1L                  // 3^i
    for (i in s.indices) {
        val lower = mod            // 3^i
        mod *= 3                   // 3^{i+1}
        val want = s.substring(0, i + 1)
        var found = -1L
        for (t in 0..2) {
            val base = r + t * lower
            var hits = false
            for (m in 0..(i + 2)) {
                val cand = base + m * mod
                if (cand >= 1L && beginsWith(cand, want)) { hits = true; break }
            }
            if (hits) {
                check(found < 0L) { "第 ${i + 1} 位（字符 ${s[i]}）有多个提升候选命中" }
                found = base
            }
        }
        check(found >= 0L) { "第 ${i + 1} 位（字符 ${s[i]}）没有提升候选命中" }
        r = found
    }
    return r
}

/** 方法 B：BigInteger 精确维护 a_n = (A·a_1 + B)/3^{n−1}，逐层解同余。 */
private fun residueByAffine(s: String): Long {
    val three = BigInteger.valueOf(3L)
    var a = BigInteger.ONE
    var b = BigInteger.ZERO
    var pow = BigInteger.ONE             // 3^{n−1}，n 从 1 起
    var r = BigInteger.ZERO
    for (ch in s) {
        val (c, d, target) = when (ch) {
            'D' -> Triple(1L, 0L, 0L)
            'U' -> Triple(4L, 2L, 1L)
            else -> Triple(2L, -1L, 2L)
        }
        val mod = pow.multiply(three)    // 3^n
        // 第 n 步属于该分支 ⟺ A_n·a_1 + B_n ≡ 3^{n−1}·s_n (mod 3^n)
        val rhs = pow.multiply(BigInteger.valueOf(target)).subtract(b).mod(mod)
        val cand = rhs.multiply(a.modInverse(mod)).mod(mod)
        check(cand.mod(pow) == r.mod(pow)) { "第 n 层同余与前一层不兼容" }
        r = cand
        // 更新到 n+1：a_{n+1} = (c·a_n + d)/3 = (c·a_1·A_n + c·B_n + 3^{n−1}·d)/3^n
        a = a.multiply(BigInteger.valueOf(c))
        b = b.multiply(BigInteger.valueOf(c)).add(pow.multiply(BigInteger.valueOf(d)))
        pow = mod
    }
    return r.toLong()
}

/** 由剩余类 r mod 3^{|s|} 给出类中严格大于 lowerBound 的最小元素，并逐项模拟确认非退化。 */
private fun smallestAbove(s: String, r: Long, lowerBound: Long): Long {
    var mod = 1L
    repeat(s.length) { mod *= 3 }
    var a = r
    if (a <= lowerBound) a += ((lowerBound - a) / mod + 1) * mod
    var guard = 0
    while (!beginsWith(a, s)) {
        a += mod
        check(++guard <= 64) { "类中连续出现过多退化代表元" }
    }
    return a
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.3f".format(ms)} ms")
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 1. 题面锚点 ----------
    val anchor = "DdDddUUdDD"
    check(stepString(231L, 10) == anchor) { "231 的步串不符：${stepString(231L, 10)}" }
    check(stepString(1004064L, 30) == "DdDddUUdDDDdUDUUUdDdUUDDDUdDD") { "1004064 的步串不符" }
    var scan = 1_000_001L
    while (!beginsWith(scan, anchor)) scan++
    check(scan == 1004064L) { "> 10^6 的最小解应为 1004064，扫描得到 $scan" }
    println("题面锚点：231 → ${stepString(231L, 10)}；1004064 → 以 DdDddUUdDD 开头且为 >10^6 最小解；" +
        "1004064 的完整步串 = ${stepString(1004064L, 30)}")

    // ---------- 2. 两条方法在所有前缀上互证 ----------
    for (len in 1..TARGET.length) {
        val s = TARGET.substring(0, len)
        val rA = residueByLifting(s)
        val rB = residueByAffine(s)
        check(rA == rB) { "前缀长度 $len：方法 A 剩余类 $rA ≠ 方法 B 剩余类 $rB" }
        check(smallestAbove(s, rA, LOWER_BOUND) == smallestAbove(s, rB, LOWER_BOUND)) {
            "前缀长度 $len：两条路径的最小解不同"
        }
    }
    println("方法 A（逐字符提升 + 模拟）与方法 B（BigInteger 仿射同余）在目标串的全部 30 个前缀上" +
        "给出的剩余类与「>10^15 最小解」逐一一致")

    // ---------- 3. 最终答案与回代验证 ----------
    val rA = residueByLifting(TARGET)
    val rB = residueByAffine(TARGET)
    val answer = smallestAbove(TARGET, rA, LOWER_BOUND)
    check(stepString(answer, TARGET.length) == TARGET) { "答案回代模拟失败" }
    var mod30 = 1L
    repeat(TARGET.length) { mod30 *= 3 }
    check(answer > LOWER_BOUND && answer - mod30 <= LOWER_BOUND) { "答案不是类中 > 10^15 的最小元素" }
    println("剩余类：r = $rA（方法 A、B 一致），mod 3^30 = $mod30；类中前一个元素 ${answer - mod30} ≤ 10^15")
    println("答案回代：从 $answer 出发的前 30 步 = ${stepString(answer, TARGET.length)}")

    // ---------- 4. 计时 ----------
    residueByLifting(TARGET); residueByAffine(TARGET)
    val msA = bestOf3("方法 A：逐字符提升（目标串 30 位，含答案定位）", answer) {
        smallestAbove(TARGET, residueByLifting(TARGET), LOWER_BOUND)
    }
    val msB = bestOf3("方法 B：BigInteger 仿射同余", answer) {
        smallestAbove(TARGET, residueByAffine(TARGET), LOWER_BOUND)
    }
    val msAnchor = bestOf3("题面锚点扫描 [10^6+1, 1004064]", 1004064L) {
        var x = 1_000_001L
        while (!beginsWith(x, anchor)) x++
        x
    }
    val rate = run {
        val t0 = System.nanoTime()
        var cnt = 0
        var x = 1L
        while (x <= 100_000_000L) {
            if (beginsWith(x, TARGET)) cnt++
            x++
        }
        val ms = (System.nanoTime() - t0) / 1e6
        val perSec = 100_000_000.0 / ms * 1000.0
        println("对照：在 [1,10^8] 上按全串匹配扫描（命中 $cnt 个）用时 ${"%.1f".format(ms)} ms" +
            "（≈ ${"%.1f".format(perSec / 1e6)} 百万候选/秒）")
        perSec
    }

    println()
    println("答案 = $answer")
    println("汇总：方法 A ${"%.3f".format(msA)} ms；方法 B ${"%.3f".format(msB)} ms；" +
        "锚点扫描 ${"%.3f".format(msAnchor)} ms")
    println("暴力外推：类中最小正元素 r = $rA 就是「最小的匹配 a_1」，它到答案还隔着 5 个 3^30；" +
        "按本机扫描速率 ${"%.2e".format(rate)} 候选/秒，从 1 扫到 r 需约 " +
        "${"%.1f".format(rA / rate / 86400)} 天，扫到 >10^15 的答案需约 " +
        "${"%.1f".format(answer / rate / 86400)} 天 → 直接暴力不可行，必须做剩余类提升")
    println("check() 全部通过")
}
