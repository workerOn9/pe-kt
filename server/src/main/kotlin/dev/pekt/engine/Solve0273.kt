package dev.pekt.engine

/**
 * PE 273 — Sum of Squares（平方和）：求 Σ S(N)，其中 N 取遍「全部素因子都是 4k+1 形状、且平方自由」
 * 的数（等价于 16 个素因子 {5,13,…,149} 的任意子集之积），S(N) 是 a² + b² = N（0 ≤ a ≤ b）的
 * 全部解中 a 的和。
 *
 * 推导（详见 content/problems/0273/solution.kt 头部与 0273/analysis.md）：
 *   每个 p ≡ 1 (mod 4) 素数有唯一表示 p = x² + y²（0 < x < y），即 Z[i] 中 p = (x+yi)(x−yi)。
 *   若 (a,b) 是 M 的表示，则 N = M·p 的两个表示为（各自按 a ≤ b 归一化）
 *     (|ay − bx|, ax + by) 与 (ay + bx, |ax − by|)，
 *   二者除 N = 1 的 (0,1) 外互不重合，且这样递推得到的正是 N 的全部表示；k 个素因子的 N 恰有
 *   2^{k−1} 个表示（与 r₂(N) = 4(d₁ − d₃) = 4·2^k 吻合）。
 *
 * 实现：对 16 个素数做「选 / 不选」DFS，每个节点携带当前子集乘积的全部表示（两个 LongArray），
 * 每层把表示集合按上式映射成两倍大小的新集合；总扩张次数 Σᵢ 3^i/2 ≈ 1.1×10^7，实测约 60 ms。
 * 逐个 N 直接开方枚举需要 ~10^13 次迭代（N 最大 ≈ 2.5×10^27），不可行。
 *
 * 校验：题面锚点 S(65) = 5 复现；与前 8 个素数全部 255 个子集的直接枚举逐子集一致；
 *   与方法 B（符号向量枚举高斯整数）在全部 65536 个子集上总和一致。逻辑与
 *   content/problems/0273/solution.kt 的主路径（方法 A）一致。
 */
internal fun solve0273Impl(): Long {
    val primes = intArrayOf(5, 13, 17, 29, 37, 41, 53, 61, 73, 89, 97, 101, 109, 113, 137, 149)
    val xy = Array(primes.size) { rep0273(primes[it]) }
    var total = 0L

    fun rec(i: Int, a: LongArray, b: LongArray, n: Int) {
        if (i == primes.size) {
            var s = 0L
            for (k in 0 until n) s += a[k]
            total += s
            return
        }
        rec(i + 1, a, b, n) // 不选 p_i
        val x = xy[i][0]
        val y = xy[i][1]
        val na = LongArray(2 * n)
        val nb = LongArray(2 * n)
        var m = 0
        for (k in 0 until n) {
            val u = a[k]
            val v = b[k]
            var c = u * y - v * x
            if (c < 0) c = -c
            val d = u * x + v * y
            val c1: Long
            val d1: Long
            if (c <= d) { c1 = c; d1 = d } else { c1 = d; d1 = c }
            val e = u * y + v * x
            var f = u * x - v * y
            if (f < 0) f = -f
            val c2: Long
            val d2: Long
            if (e <= f) { c2 = e; d2 = f } else { c2 = f; d2 = e }
            na[m] = c1
            nb[m] = d1
            m++
            if (c2 != c1 || d2 != d1) { // 仅 N = 1（u=0,v=1）时两个候选重合
                na[m] = c2
                nb[m] = d2
                m++
            }
        }
        rec(i + 1, na, nb, m)
    }

    rec(0, longArrayOf(0L), longArrayOf(1L), 1)
    return total
}

/** p = x² + y² 的唯一表示（0 < x < y）；p < 150 时双精度开方完全精确。 */
private fun rep0273(p: Int): LongArray {
    var x = 1L
    while (x * x < p) {
        val r = p - x * x
        val y = Math.round(Math.sqrt(r.toDouble()))
        if (y > x && y * y == r) return longArrayOf(x, y)
        x++
    }
    error("$p 不是 4k+1 型素数")
}
