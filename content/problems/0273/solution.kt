#!/usr/bin/env kotlin
/**
 * Project Euler 273 — Sum of Squares（平方和）
 *
 * 思路
 * ────
 * N 被限制为「平方自由、且素因子全是 4k+1 型」的数：这正好是集合
 *   P = {5,13,17,29,37,41,53,61,73,89,97,101,109,113,137,149}（16 个）
 * 的任意子集的乘积，共 2^16 = 65536 个（含 N = 1；题面把它算进来也无妨，S(1) = 0）。
 *
 * 关键结构：p ≡ 1 (mod 4) 素数有唯一表示 p = x² + y²（0 < x < y），在 Z[i] 里即
 * p = (x+yi)(x−yi)。若 (a,b) 是 M 的一个表示（0 ≤ a ≤ b），把 N = M·p 的两个高斯因子
 * 分别乘上去：
 *   (a+bi)(x+yi) = (ax−by) + (ay+bx)i
 *   (a+bi)(x−yi) = (ax+by) + (ay−bx)i
 * 两个乘积各自按「实部虚部交换」归一化成 a ≤ b，就得到 N 的全部表示，且每个表示恰好一次
 * （唯一例外是 N = 1 的 (0,1)：两个乘积重合，去重一次即可）。
 * 由此 k 个素因子的 N 恰有 2^{k−1} 个表示，与 r₂(N) = 4(d₁(N) − d₃(N)) = 4·2^k 完全吻合。
 *
 * 为什么不能逐个 N 直接解：N 最大 = 5·13·…·149 ≈ 2.5×10^27，逐个开方枚举 a 要 ~10^13 次
 * 迭代，而按集合递推的总代价只有 Σ_{S} |reps(S)| ≈ 3^16/2 ≈ 2×10^7 次高斯乘法——
 * 每个素因子的加入只是把「父集合的表示集合」映射成两个新表示，指数只出现在表示个数上。
 *
 * 复杂度
 * ──────
 * 方法 A（主路径）：对 16 个素数做「选 / 不选」DFS，每个节点维护当前子集乘积的全部表示。
 *   总扩张次数 Σᵢ 3^i/2 ≈ 1.1×10^7（每次扩张 2 个高斯乘法），内存为 DFS 路径上
 *   各层表示数组之和（峰值 < 2^16 对）。
 * 方法 B（独立复核）：枚举每个子集的「符号向量」，直接乘出高斯整数 ∏(xᵢ ± yᵢi)；
 *   约定下标最大的已选素因子取 + 号（s 与 −s 是共轭、对应同一表示），叶子取 min(|Re|,|Im|)，
 *   总叶子数 2^16/2·…≈ 3^16/2，与 A 的路径完全不同。
 *
 * 验证
 * ────
 * 1. 题面锚点：S(65) = 5（65 = 5·13，表示 (1,8)、(4,7)）；
 * 2. 暴力对照：对前 8 个素数的全部 255 个子集，逐个 N 直接枚举 a 判 N−a² 是否为平方，
 *    与主路径逐子集的结果全部相等；对 16 个素数的所有不超过 2 元的子集同样逐一对拍；
 * 3. 双方法互证：方法 A 与方法 B 的总和（65536 个子集全算）一致；
 * 4. 结构自检：每个子集的表示个数都等于 2^{k−1}（k = 素因子个数）。
 *
 * 答案：2032447591196869022（本机实跑；表示计数与公开答案表一致）
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0273/solution.kt -d /tmp/kc-0273
 * java -cp /tmp/kc-0273:<kotlin-stdlib-2.1.21.jar> SolutionKt
 */

/** 4k+1 型、且 < 150 的全部素数（共 16 个）。 */
private val PRIMES = intArrayOf(5, 13, 17, 29, 37, 41, 53, 61, 73, 89, 97, 101, 109, 113, 137, 149)

/** p = x² + y² 的唯一表示（0 < x < y）；p < 150 时双精度开方完全精确。 */
private fun twoSquares(p: Int): LongArray {
    var x = 1L
    while (x * x < p) {
        val r = p - x * x
        val y = Math.round(Math.sqrt(r.toDouble()))
        if (y > x && y * y == r) return longArrayOf(x, y)
        x++
    }
    error("$p 不是 4k+1 型素数")
}

/**
 * 方法 A（主路径）：按素数顺序逐个决定「选 / 不选」，dfs 维护当前乘积的全部表示 (a,b)。
 * 若传入 [perSubset]（长度 ≥ 2^primes.size），则同时把每个子集的 S(N) 写进该表；
 * [perCount] 可记录每个子集的表示个数（用于结构自检：k 个素因子恰有 2^{k−1} 个表示）。
 */
private fun solveDfs(primes: IntArray, perSubset: LongArray? = null, perCount: IntArray? = null): Long {
    val xy = Array(primes.size) { twoSquares(primes[it]) }
    var total = 0L

    fun rec(i: Int, mask: Int, a: LongArray, b: LongArray, n: Int) {
        if (i == primes.size) {
            var s = 0L
            for (k in 0 until n) s += a[k]
            total += s
            perSubset?.set(mask, s)
            perCount?.set(mask, n)
            return
        }
        rec(i + 1, mask, a, b, n) // 不选 p_i
        val x = xy[i][0]
        val y = xy[i][1]
        val na = LongArray(2 * n)
        val nb = LongArray(2 * n)
        var m = 0
        for (k in 0 until n) {
            val u = a[k]
            val v = b[k]
            // 候选 1：(|uy − vx|, ux + vy)
            var c = u * y - v * x
            if (c < 0) c = -c
            val d = u * x + v * y
            val c1: Long
            val d1: Long
            if (c <= d) { c1 = c; d1 = d } else { c1 = d; d1 = c }
            // 候选 2：(uy + vx, |ux − vy|)
            val e = u * y + v * x
            var f = u * x - v * y
            if (f < 0) f = -f
            val c2: Long
            val d2: Long
            if (e <= f) { c2 = e; d2 = f } else { c2 = f; d2 = e }
            na[m] = c1
            nb[m] = d1
            m++
            if (c2 != c1 || d2 != d1) { // 仅 N = 1（u=0,v=1）时两候选重合
                na[m] = c2
                nb[m] = d2
                m++
            }
        }
        rec(i + 1, mask or (1 shl i), na, nb, m)
    }

    rec(0, 0, longArrayOf(0), longArrayOf(1), 1)
    return total
}

/**
 * 方法 B（独立复核）：对每个子集枚举「符号向量」s ∈ {±1}^k，直接乘出 ∏(xᵢ + sᵢyᵢi)。
 * 规范化约定：下标最大的已选素因子（即 DFS 中第一个被选的）取 + 号；这样 s 与 −s（共轭对）
 * 只保留一个代表，叶子取 min(|Re|,|Im|) 即该表示中的 a。总叶子数约为 3^16/2。
 * 若传入 [perSubset] 则逐子集记录 S(N)，用于与主路径逐子集对拍。
 */
private fun solveSigns(primes: IntArray, perSubset: LongArray? = null): Long {
    val xy = Array(primes.size) { twoSquares(primes[it]) }
    var total = 0L

    // 从高位素数往低位走：seen = 已有更高位的素因子被选中（此时符号可任取）
    fun rec(i: Int, re: Long, im: Long, seen: Boolean, mask: Int) {
        if (i < 0) {
            if (!seen) return // 空子集：S(1) = 0
            val ar = if (re < 0) -re else re
            val ai = if (im < 0) -im else im
            val s = if (ar < ai) ar else ai
            total += s
            perSubset?.let { it[mask] = it[mask] + s } // 同一子集有多个符号向量，累加
            return
        }
        rec(i - 1, re, im, seen, mask) // 不选 p_i
        val x = xy[i][0]
        val y = xy[i][1]
        rec(i - 1, re * x - im * y, re * y + im * x, true, mask or (1 shl i)) // 选，符号 +
        if (seen) rec(i - 1, re * x + im * y, im * x - re * y, true, mask or (1 shl i)) // 选，符号 −
    }

    rec(primes.size - 1, 1L, 0L, false, 0)
    return total
}

/** 直接暴力：枚举 0 ≤ a ≤ b，a² + b² = N，返回 Σa。允许 N ≤ 2^52 的精确开方。 */
private fun sumOfSquares(n: Long): Long {
    var total = 0L
    var a = 0L
    while (2 * a * a <= n) {
        val r = n - a * a
        val b = Math.round(Math.sqrt(r.toDouble()))
        var c = b - 1
        while (c <= b + 1) {
            if (c * c == r && c >= a) { total += a; break }
            c++
        }
        a++
    }
    return total
}

/** 直接暴力：把 [mask]（相对 [primes]）对应子集的乘积算出来再逐 a 枚举。 */
private fun bruteSubset(primes: IntArray, mask: Int): Long {
    var n = 1L
    for (i in primes.indices) if ((mask shr i) and 1 == 1) n *= primes[i]
    return sumOfSquares(n)
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
    val perSubsetA = LongArray(1 shl PRIMES.size)
    val perCountA = IntArray(1 shl PRIMES.size)
    val totalA = solveDfs(PRIMES, perSubsetA, perCountA)

    // ---------- 1. 题面锚点 ----------
    check(perSubsetA[0b11] == 5L) { "S(65) = ${perSubsetA[0b11]} ≠ 5" }
    println("题面锚点：S(65) = 5（65 = 5·13，表示 (1,8)、(4,7)）")

    // ---------- 2. 暴力对照 ----------
    // 前 8 个素数的全部 255 个子集：逐子集直接枚举 a
    var checked = 0
    for (mask in 1 until (1 shl 8)) {
        val b = bruteSubset(PRIMES.copyOfRange(0, 8), mask)
        val m2 = mask // 前 8 位相同，高位恒 0
        check(b == perSubsetA[m2]) { "mask=$mask：暴力 $b ≠ 递推 ${perSubsetA[m2]}" }
        checked++
    }
    println("暴力对照 1：前 8 个素数的 $checked 个子集，逐子集 S(N) 与直接枚举一致")
    // 全部 16 个素数的所有 1 元、2 元子集
    var checked2 = 0
    for (i in PRIMES.indices) {
        check(bruteSubset(PRIMES, 1 shl i) == perSubsetA[1 shl i]) { "单元素子集 $i 不一致" }
        checked2++
        for (j in i + 1 until PRIMES.size) {
            val m = (1 shl i) or (1 shl j)
            check(bruteSubset(PRIMES, m) == perSubsetA[m]) { "子集 ($i,$j) 不一致" }
            checked2++
        }
    }
    println("暴力对照 2：全部 16 个素数的 $checked2 个 ≤2 元子集，逐子集 S(N) 直接枚举一致")

    // ---------- 3. 结构自检：表示个数 = 2^{k-1} ----------
    var checkedCount = 0
    for (mask in 1 until (1 shl PRIMES.size)) {
        val k = Integer.bitCount(mask)
        check(perCountA[mask] == (1 shl (k - 1))) {
            "mask=$mask：表示个数 ${perCountA[mask]} ≠ ${1 shl (k - 1)}"
        }
        checkedCount++
    }
    println("结构自检：$checkedCount 个非空子集的表示个数均为 2^(k−1)（与 r₂(N) = 4·2^k 吻合）")

    // ---------- 4. 双方法互证 ----------
    val perSubsetB = LongArray(1 shl PRIMES.size)
    val totalB = solveSigns(PRIMES, perSubsetB)
    check(totalA == totalB) { "方法 A $totalA ≠ 方法 B $totalB" }
    var sameBySubset = 0
    for (m in perSubsetB.indices) {
        check(perSubsetA[m] == perSubsetB[m]) { "mask=$m：方法 A ${perSubsetA[m]} ≠ 方法 B ${perSubsetB[m]}" }
        sameBySubset++
    }
    println("方法 A（集合递推）与方法 B（符号向量）在全部 $sameBySubset 个子集上逐一一致（总和 $totalA）")

    // 与前 8/9/10 个素数配套的小规模总和控制值（供 brute-force.kt 对拍）：只取前 L 个素数
    for (l in 8..10) {
        val sub = solveDfs(PRIMES.copyOfRange(0, l))
        println("前 $l 个素数（共 ${1 shl l} 个子集）的总和 = $sub")
    }

    // ---------- 5. 计时 ----------
    solveDfs(PRIMES); solveSigns(PRIMES)
    val msA = bestOf3("方法 A（集合递推 DFS，16 素数）", totalA) { solveDfs(PRIMES) }
    val msB = bestOf3("方法 B（符号向量枚举，16 素数）", totalB) { solveSigns(PRIMES) }

    println()
    println("答案 = $totalA")
    println("汇总：方法 A ${"%.3f".format(msA)} ms；方法 B ${"%.3f".format(msB)} ms")
    println("check() 全部通过")
}
