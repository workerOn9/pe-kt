/**
 * Project Euler 073 — Counting Fractions in a Range（暴力对照解）
 *
 * 暴力解：对每个分母 d 扫描全部 n = 1…d−1，用交叉相乘判断是否落在 (1/3, 1/2) 内，
 * 再做一次 gcd 判定是否既约。不去利用区间的代数等价形式，纯粹按定义枚举。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 */

fun solveBruteForce(): Long {
    val n = 12000
    var count = 0L
    for (d in 2..n) {
        for (x in 1 until d) {
            if (3L * x <= d) continue
            if (2L * x >= d) continue
            if (gcdNat(x, d) == 1) count++
        }
    }
    return count
}

fun gcdNat(a: Int, b: Int): Int {
    var x = a
    var y = b
    while (y != 0) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

fun main() {
    println(solveBruteForce())
}
