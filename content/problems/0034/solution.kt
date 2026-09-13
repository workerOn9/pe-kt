/**
 * Project Euler 034 — Digit Factorials
 *
 * 优化解：上界 7×9! = 2540160（8 位数的最小值已超过 8×9!），预存 0..9 的阶乘表，
 * 逐位累加后与原数比较；从 3 开始扫描（1!、2! 是自身而非「和」）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun factorialInt(n: Int): Int { var r = 1; for (i in 2..n) r *= i; return r }

fun solve(): Long {
    val f = IntArray(10) { factorialInt(it) }
    var sum = 0L
    for (n in 3..2540160) {
        var x = n; var s = 0
        while (x > 0) { s += f[x % 10]; x /= 10 }
        if (s == n) sum += n
    }
    return sum
}

fun main() {
    println(solve())
}
