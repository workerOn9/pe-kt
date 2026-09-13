/**
 * Project Euler 004 — Largest Palindrome Product
 *
 * 优化解：降序搜索 + 两条结构性剪枝。
 * 1) 从大到小枚举，乘积不超过当前最优即可提前 break；
 * 2) 六位回文数 abccba = 11 × (9091a + 910b + 100c)，必含因子 11，
 *    所以两个三位数因子至少一个被 11 整除，据此按步长跳枚举。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isPalindrome(n: Long): Boolean {
    val s = n.toString()
    return s == s.reversed()
}

fun solve(): Long {
    var best = 0L
    for (a in 999 downTo 100) {
        if (a.toLong() * a <= best) break          // 之后任何乘积都不可能更大
        val step: Int
        var b: Int
        if (a % 11 == 0) {                          // a 已含因子 11，b 任意
            step = 1
            b = a
        } else {                                    // b 必须被 11 整除
            step = 11
            b = a - a % 11
        }
        while (b >= 100) {
            val product = a.toLong() * b
            if (product <= best) break
            if (isPalindrome(product)) best = product
            b -= step
        }
    }
    return best
}

fun main() {
    println(solve())
}
