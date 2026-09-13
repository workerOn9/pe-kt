/**
 * Project Euler 091 — Right Triangles with Integer Coordinates（暴力解）
 *
 * 暴力思路：不加任何几何化简，枚举格点上所有无序点对 {P, Q}（皆非原点），
 * 对三处夹角各做一次点积判定：P·Q（直角在 O）、(O−P)·(Q−P)（直角在 P）、
 * (O−Q)·(P−Q)（直角在 Q）。任一为零即是直角三角形；非退化三角形直角顶点唯一，不会重复计。
 *
 * 复杂度：O(N^4)，N=50 时约 3.4×10^6 个点对，实测几十毫秒。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d bf.jar && java -jar bf.jar
 */

private const val b091N = 50

fun solveBruteForce(): Long {
    val n = b091N
    val pts = ArrayList<IntArray>()
    for (x in 0..n) for (y in 0..n) if (x != 0 || y != 0) pts.add(intArrayOf(x, y))
    var count = 0L
    for (i in pts.indices) {
        val p = pts[i]
        for (j in i + 1 until pts.size) {
            val q = pts[j]
            val atO = p[0] * q[0] + p[1] * q[1]
            val atP = p[0] * (q[0] - p[0]) + p[1] * (q[1] - p[1])
            val atQ = q[0] * (p[0] - q[0]) + q[1] * (p[1] - q[1])
            if (atO == 0 || atP == 0 || atQ == 0) count++
        }
    }
    return count
}

fun main() {
    println(solveBruteForce())
}
