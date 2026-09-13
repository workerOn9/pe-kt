/**
 * Project Euler 030 — 暴力解（教学对比用）
 *
 * 不预存查找表，每一位都调用 Math.pow 现算五次方（浮点运算），并同样扫描到上界。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    var sum = 0L
    for (n in 2..354294) {
        var x = n; var s = 0.0
        while (x > 0) { s += Math.pow((x % 10).toDouble(), 5.0); x /= 10 }
        if (s == n.toDouble()) sum += n
    }
    return sum
}

fun main() {
    println(solveBruteForce())
}
