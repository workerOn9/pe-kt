package dev.pekt.problems

/**
 * Problem 196: Prime Triplets
 *
 * 思路：分段筛 + 三角形网格的 8 邻域
 *
 * 三角形中第 y 行（从 1 起）为 T(y-1)+1 .. T(y)，其中 T(y) = y(y+1)/2；
 * 位置 (y, x) 上的数 v = T(y-1)+x。按 (行,列) 网格取邻域，每个数至多有 8 个邻居：
 * 上一行的 (y-1, x-1) (y-1, x) (y-1, x+1)，同行的 (y, x-1) (y, x+1)，
 * 下一行的 (y+1, x-1) (y+1, x) (y+1, x+1)（列号须满足 1 <= x' <= y'）。
 *
 * 「素三元组」定义为：三个素数中有一个的另外两个都是它的邻居。
 * 因此第 n 行中的素数 p 计入 S(n) 的充要条件是：
 *   p 自身至少有两个素数邻居；或者 p 的某个素数邻居 q 至少有两个素数邻居
 *   （此时三元组以 q 为中心，p 是 q 的两个素数邻居之一）。
 *
 * 实现：对目标行 n 一次筛出第 n-2 .. n+2 行范围的素性（约 5n 个数，用埃氏筛分段标记），
 * 先在第一遍中把「素数邻居数 >= 2」的素数收进 good 集合，再扫描第 n 行做上述判定。
 *
 * 自检：S(8) = 29 + 31 = 60，S(9) = 37，S(10) = 47，S(10000) = 950007619，
 * 与题面给出的数值一致（前两者由 brute-force.kt 独立复核）。
 *
 * 复杂度：每个目标行 O(n log log n) 时间、O(n) 空间（n ≈ 7.2 × 10^6）。
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 三角数 T(k) = k(k+1)/2 */
private fun tri196(k: Long): Long = k * (k + 1) / 2

/** 整数平方根（向下取整） */
private fun isqrt196(n: Long): Long {
    var x = Math.sqrt(n.toDouble()).toLong()
    while ((x + 1) * (x + 1) <= n) x++
    while (x * x > n) x--
    return x
}

/** 数值 v 所在的行号 y（第 y 行满足 T(y-1) < v <= T(y)） */
private fun rowOf196(v: Long): Long {
    var y = (isqrt196(8 * v + 1) - 1) / 2 + 1
    while (y > 1 && tri196(y - 1) >= v) y--
    while (tri196(y) < v) y++
    return y
}

/** S(n)：第 n 行中所有属于某个素三元组的素数之和 */
private fun rowSum196(n: Long): Long {
    val lo = tri196(n - 3) + 1          // 第 n-2 行的起点
    val hi = tri196(n + 2)              // 第 n+2 行的终点
    val w = (hi - lo + 1).toInt()
    val comp = BooleanArray(w)          // false 表示未被标记，即素数

    // 基础素数表（到 sqrt(hi)），用于分段筛
    val root = isqrt196(hi).toInt() + 1
    val baseComp = BooleanArray(root + 1)
    var i = 2
    while (i.toLong() * i <= root) {
        if (!baseComp[i]) {
            var j = i * i
            while (j <= root) { baseComp[j] = true; j += i }
        }
        i++
    }
    for (p in 2..root) {
        if (baseComp[p]) continue
        val pp = p.toLong() * p
        if (pp > hi) break
        var start = ((p - lo % p) % p).toInt()             // 窗口内第一个 p 的倍数
        if (lo + start < pp) {                             // 跳过 < p^2 的部分（它们自身是素数）
            val step = ((pp - lo - start + p - 1) / p).toInt()
            start += step * p
        }
        var idx = start
        while (idx < w) { comp[idx] = true; idx += p }
    }

    fun isP(v: Long): Boolean = v in lo..hi && !comp[(v - lo).toInt()]

    /** (y, x) 的 8 邻域中的素数个数 */
    fun primeNeighbours(y: Long, x: Long): Int {
        var cnt = 0
        for (dy in -1L..1L) {
            val yy = y + dy
            if (yy < 1) continue
            for (dx in -1L..1L) {
                if (dy == 0L && dx == 0L) continue
                val xx = x + dx
                if (xx < 1 || xx > yy) continue
                if (isP(tri196(yy - 1) + xx)) cnt++
            }
        }
        return cnt
    }

    // 第一遍：标记素数邻居数 >= 2 的素数
    val good = HashSet<Long>()
    for (y in (n - 1)..(n + 1)) {
        var x = 1L
        while (x <= y) {
            val v = tri196(y - 1) + x
            if (isP(v) && primeNeighbours(y, x) >= 2) good.add(v)
            x++
        }
    }

    // 第二遍：第 n 行中「自身属于 good，或某个素数邻居属于 good」的素数求和
    var sum = 0L
    var x = 1L
    while (x <= n) {
        val v = tri196(n - 1) + x
        if (isP(v)) {
            var ok = good.contains(v)
            if (!ok) {
                for (dy in -1L..1L) {
                    val yy = n + dy
                    if (yy < 1) continue
                    for (dx in -1L..1L) {
                        if (dy == 0L && dx == 0L) continue
                        val xx = x + dx
                        if (xx < 1 || xx > yy) continue
                        val u = tri196(yy - 1) + xx
                        if (isP(u) && good.contains(u)) ok = true
                    }
                }
            }
            if (ok) sum += v
        }
        x++
    }
    return sum
}

fun solve196(): Long = rowSum196(5678027L) + rowSum196(7208785L)

fun main() {
    check(rowOf196(29L) == 8L && rowOf196(36L) == 8L && rowOf196(37L) == 9L) { "行号换算错误" }
    println("S(8)     = ${rowSum196(8L)} (题面 60)")
    println("S(9)     = ${rowSum196(9L)} (题面 37)")
    println("S(10000) = ${rowSum196(10000L)} (题面 950007619)")
    println("S(5678027) = ${rowSum196(5678027L)}")
    println("S(7208785) = ${rowSum196(7208785L)}")
    println("answer   = ${solve196()}")
}
