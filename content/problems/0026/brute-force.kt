/**
 * Project Euler 026 — 暴力解（教学对比用）
 *
 * 对每个分母 d 用长除法模拟小数展开：余数首次重复的位置差即循环节长度。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun cycleLen(d: Int): Int {
    val first = IntArray(d) { -1 }
    var rem = 1 % d; var pos = 0
    while (rem != 0 && first[rem] == -1) { first[rem] = pos; rem = rem * 10 % d; pos++ }
    return if (rem == 0) 0 else pos - first[rem]
}

fun solveBruteForce(): Long {
    var best = 0; var bestLen = 0
    for (d in 2 until 1000) { val l = cycleLen(d); if (l > bestLen) { bestLen = l; best = d } }
    return best.toLong()
}

fun main() {
    println(solveBruteForce())
}
