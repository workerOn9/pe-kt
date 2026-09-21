/**
 * PE 167 — 直接模拟 Ulam 序列：维护「每个值的表示数」计数数组，
 * 每次加入新项时把它与所有先前项的和计数 +1，然后从上一项之后找计数恰为 1 的最小值。
 * 只适合模拟前几万项（O(N^2)），用于观察差分序列的周期性。
 */
fun ulamTerms(a: Long, b: Long, n: Int): LongArray {
    val maxVal = 60L * n + 1000
    val cnt = IntArray(maxVal.toInt() + 1)
    val terms = LongArray(n)
    terms[0] = a
    if (n > 1) { terms[1] = b; cnt[(a + b).toInt()]++ }
    var k = 2
    var last = b
    while (k < n) {
        var t = last + 1
        while (t <= maxVal && cnt[t.toInt()] != 1) t++
        terms[k] = t
        last = t
        for (i in 0 until k) {
            val s = terms[i] + t
            if (s <= maxVal && cnt[s.toInt()] < 3) cnt[s.toInt()]++
        }
        k++
    }
    return terms
}

fun main() {
    val t = ulamTerms(1, 2, 7)
    println("U(1,2) 前 7 项 = " + t.joinToString(",") + "   (题面: 1,2,3,4,6,8,11)")
    val u25 = ulamTerms(2, 5, 4000)
    val d = LongArray(u25.size - 1) { u25[it + 1] - u25[it] }
    println("U(2,5) 第 4000 项 = " + u25.last())
    // 观察尾部 32 项差分
    println("末 32 个差分之和 = " + (u25.size - 32 until d.size).sumOf { d[it] })
}
