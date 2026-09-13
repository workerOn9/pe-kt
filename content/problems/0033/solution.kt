/**
 * Project Euler 033 — Digit Cancelling Fractions
 *
 * 优化解：只枚举两位数分子分母，检查是否存在「分子某位 == 分母某位」且删去该位后
 * 分数值不变的情形；满足条件的分数累乘后约分取分母。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun gcdL(a: Long, b: Long): Long {
    var x = kotlin.math.abs(a); var y = kotlin.math.abs(b)
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

fun solve(): Long {
    var num = 1; var den = 1
    for (d in 10..99) for (n in 10 until d) {
        if (n % 10 == 0 && d % 10 == 0) continue
        val n1 = n / 10; val n2 = n % 10; val d1 = d / 10; val d2 = d % 10
        val a: Int; val b: Int
        if (n1 == d1) { a = n2; b = d2 } else if (n1 == d2) { a = n2; b = d1 }
        else if (n2 == d1) { a = n1; b = d2 } else if (n2 == d2) { a = n1; b = d1 }
        else continue
        if (b == 0) continue
        if (n.toLong() * b != d.toLong() * a) continue
        num *= n; den *= d
    }
    return (den / gcdL(num.toLong(), den.toLong())).toLong()
}

fun main() {
    println(solve())
}
