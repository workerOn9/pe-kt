package dev.pekt.engine

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

/**
 * PE 314 — Landgrab（月球上的老鼠）
 *
 * 格点凸多边形最大化 面积/周长。0-1 分式规划 + 凸链 DP。
 * 最终答案：132.52756426（×10⁸ = 13252756426）。
 */
internal fun solve0314Impl(): String {
    val SIDE = 500
    val B = 170
    val D = 15

    val dirs: Array<IntArray> = run {
        val tmp = ArrayList<IntArray>()
        for (di in 0..D) for (dj in 0..D) {
            if (di == 0 && dj == 0) continue
            tmp.add(intArrayOf(di, dj))
        }
        tmp.sortWith(compareByDescending { Math.atan2(it[1].toDouble(), it[0].toDouble()) })
        tmp.toTypedArray()
    }
    val ndir = dirs.size
    val np = (B + 1) * (B + 1)
    fun idx(i: Int, j: Int) = i * (B + 1) + j

    val M = DoubleArray(np)
    val bestDir = IntArray(np)

    fun solveLambda(lambda: Double): Double {
        java.util.Arrays.fill(M, Double.NEGATIVE_INFINITY)
        java.util.Arrays.fill(bestDir, -1)
        for (j in 0..B) M[idx(0, j)] = (250.0 - lambda) * (j + (SIDE - B))
        for (k in 0 until ndir) {
            val di = dirs[k][0]
            val dj = dirs[k][1]
            val len = hypot(di.toDouble(), dj.toDouble())
            for (i in di..B) {
                val x1 = (SIDE - i + di).toDouble()
                for (j in dj..B) {
                    val pi = i - di
                    val pj = j - dj
                    val cur = M[idx(pi, pj)]
                    if (cur == Double.NEGATIVE_INFINITY) continue
                    val y1 = (pj + (SIDE - B)).toDouble()
                    val nv = cur + (x1 * dj + di * y1) / 2.0 - lambda * len
                    val dst = idx(i, j)
                    if (nv > M[dst]) { M[dst] = nv; bestDir[dst] = k }
                }
            }
        }
        var best = Double.NEGATIVE_INFINITY
        for (i in 0..B) {
            val v = M[idx(i, B)]
            if (v == Double.NEGATIVE_INFINITY) continue
            val tot = v + (250.0 - lambda) * (SIDE - i) + 500.0 * lambda - 187500.0
            if (tot > best) best = tot
        }
        return best
    }

    var lo = 100.0
    var hi = 200.0
    repeat(45) {
        val mid = (lo + hi) / 2
        if (solveLambda(mid) > 0) lo = mid else hi = mid
    }
    val lambda = (lo + hi) / 2
    return String.format(java.util.Locale.US, "%.8f", lambda)
}