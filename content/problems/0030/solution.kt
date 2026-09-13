/**
 * Project Euler 030 — Digit Fifth Powers
 *
 * 优化解：上界 7×9^5 = 354294（8 位数的最小值已超过 8×9^5），只需扫描到该上界；
 * 预先算好 0..9 的五次方查找表，逐位累加即可。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val p5 = IntArray(10) { i -> var v = 1; repeat(5) { v *= i }; v }
    var sum = 0L
    for (n in 2..354294) {
        var x = n; var s = 0
        while (x > 0) { s += p5[x % 10]; x /= 10 }
        if (s == n) sum += n
    }
    return sum
}

fun main() {
    println(solve())
}
