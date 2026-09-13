/**
 * Project Euler 036 — 暴力解（教学对比用）
 *
 * 遍历 1..999999 的全部整数，用除 2 取余手工构造二进制串再判回文。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun isPalStr(s: String): Boolean { return s == s.reversed() }

fun solveBruteForce(): Long {
    var sum = 0L
    for (n in 1..999999) {
        if (!isPalStr(n.toString())) continue
        var x = n; val b = StringBuilder()
        while (x > 0) { b.append(x % 2); x /= 2 }
        if (isPalStr(b.toString())) sum += n
    }
    return sum
}

fun main() {
    println(solveBruteForce())
}
