/**
 * Project Euler 036 — Double-base Palindromes
 *
 * 优化解：大于 1 的二进制回文数必为奇数，故只需遍历奇数；十进制回文判定作为
 * 前置过滤，命中后再用 Integer.toBinaryString 判二进制回文。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isPalStr(s: String): Boolean { return s == s.reversed() }

fun solve(): Long {
    var sum = 0L
    for (n in 1..999999 step 2) {
        if (!isPalStr(n.toString())) continue
        if (isPalStr(Integer.toBinaryString(n))) sum += n
    }
    return sum
}

fun main() {
    println(solve())
}
