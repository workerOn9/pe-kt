/**
 * PE 165 — brute force O(N²) intersection check. Feasible at N=5000.
 */
fun bruteForce(): Long {
    val T = 5000
    val mod = 50515093
    var s = 290797L
    val seq = LongArray(4 * T)
    for (i in 0 until 4 * T) { s = (s * s) % mod; seq[i] = s }
    val segs = Array(T) { i ->
        doubleArrayOf(
            seq[4 * i].toDouble() / mod, seq[4 * i + 1].toDouble() / mod,
            seq[4 * i + 2].toDouble() / mod, seq[4 * i + 3].toDouble() / mod
        )
    }
    val eps = 1e-12
    var count = 0
    for (i in 0 until T) {
        val a = segs[i]
        for (j in (i + 1) until T) {
            val b = segs[j]
            val denom = (b[2] - b[0]) * (a[3] - a[1]) - (b[3] - b[1]) * (a[2] - a[0])
            if (Math.abs(denom) < eps) continue
            val t = ((b[3] - b[1]) * (b[0] - a[0]) - (b[2] - b[0]) * (b[1] - a[1])) / denom
            val u = -((a[3] - a[1]) * (b[0] - a[0]) - (a[2] - a[0]) * (b[1] - a[1])) / denom
            if (t > eps && t < 1 - eps && u > eps && u < 1 - eps) count++
        }
    }
    return count.toLong()
}

fun main() {
    println(bruteForce())
}