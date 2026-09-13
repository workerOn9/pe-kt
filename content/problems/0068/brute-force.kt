/**
 * Project Euler 068 — Magic 5-gon Ring（暴力对照）
 *
 * 暴力解：把 1..10 的全排列直接铺到 10 个位置上——
 * 前 5 个当外点 outer[0..4]，后 5 个当内点 inner[0..4]，
 * 逐条检查 5 条线 (outer[k], inner[k], inner[k+1]) 的和是否相等，
 * 丢掉 10 出现在内点的 17 位串，取最大 16 位拼接串。
 *
 * 复杂度：O(10!) = 3 628 800 次排列，每次常数次比较。
 */

fun nextPermutation(a: IntArray): Boolean {
    var i = a.size - 2
    while (i >= 0 && a[i] >= a[i + 1]) i--
    if (i < 0) return false
    var j = a.size - 1
    while (a[j] <= a[i]) j--
    val t = a[i]; a[i] = a[j]; a[j] = t
    var l = i + 1
    var r = a.size - 1
    while (l < r) {
        val u = a[l]; a[l] = a[r]; a[r] = u
        l++
        r--
    }
    return true
}

fun solveBruteForce(): Long {
    val p = IntArray(10) { it + 1 }
    var best = 0L
    do {
        val s = p[0] + p[5] + p[6]
        if (p[1] + p[6] + p[7] != s) continue
        if (p[2] + p[7] + p[8] != s) continue
        if (p[3] + p[8] + p[9] != s) continue
        if (p[4] + p[9] + p[5] != s) continue
        if (p[0] != 10 && p[1] != 10 && p[2] != 10 && p[3] != 10 && p[4] != 10) continue

        var start = 0
        for (k in 1 until 5) if (p[k] < p[start]) start = k
        val sb = StringBuilder()
        for (k in 0 until 5) {
            val i = (start + k) % 5
            sb.append(p[i]).append(p[5 + i]).append(p[5 + (i + 1) % 5])
        }
        val v = sb.toString().toLong()
        if (v > best) best = v
    } while (nextPermutation(p))
    return best
}

fun main() {
    println(solveBruteForce())
}
