/**
 * Project Euler 062 — 暴力解（教学对比用）
 *
 * 不用哈希装箱：把 1..9999 的立方及其十进制数位直方图全部算出来存成数组，
 * 再按 n 递增、对每个立方体线性扫描同位数区间内的所有更大立方体，两两比较数位直方图，
 * 第一个「连同自己在内恰有 5 个数字排列立方体」的立方体就是答案。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

private fun bfSig(n: Long): IntArray {
    val c = IntArray(10)
    var x = n
    while (x > 0L) { c[(x % 10L).toInt()]++; x /= 10L }
    return c
}

fun solveBruteForce(): Long {
    val count = 9999
    val cubes = LongArray(count)
    val sigs = Array(count) { IntArray(10) }
    val digits = IntArray(count)
    for (i in 0 until count) {
        val n = (i + 1).toLong()
        val c = n * n * n
        cubes[i] = c
        sigs[i] = bfSig(c)
        digits[i] = bfDigitCount(c)
    }

    for (i in 0 until count) {
        var total = 1
        var j = i + 1
        while (j < count && digits[j] == digits[i]) {
            if (sigs[j].contentEquals(sigs[i])) total++
            j++
        }
        if (total == 5) return cubes[i]
    }
    return -1L
}

private fun bfDigitCount(n: Long): Int {
    var x = n
    var d = 0
    while (x > 0L) { d++; x /= 10L }
    return d
}

fun main() {
    println(solveBruteForce())
}
