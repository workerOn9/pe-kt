/**
 * Project Euler 112 — 暴力解（逐整数检查）
 *
 * 思路：从 1 起逐数用除法拆位，同时记录相邻位比较中是否出现两个方向。
 * 从右向左读取只会交换上升和下降，不改变“两者都出现”的判据。
 * 累加弹跳数 B，以 100B=pn 判断比例，避免浮点相等。
 * 复杂度：O(N log N) 时间、O(1) 额外空间。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun isBouncy(value: Long): Boolean {
    var rest = value / 10
    var previous = value % 10
    var up = false
    var down = false
    while (rest > 0) {
        val digit = rest % 10
        if (digit > previous) up = true
        if (digit < previous) down = true
        if (up && down) return true
        previous = digit
        rest /= 10
    }
    return false
}

fun firstProportionBruteForce(percent: Int): Long {
    require(percent in 1..99)
    var n = 0L
    var bouncy = 0L
    do {
        n++
        if (isBouncy(n)) bouncy++
    } while (100 * bouncy != percent * n)
    return n
}

fun solveBruteForce(): Long {
    check((1L..999L).count(::isBouncy) == 525)
    check(firstProportionBruteForce(50) == 538L)
    check(firstProportionBruteForce(90) == 21780L)
    return firstProportionBruteForce(99)
}

fun main() { println(solveBruteForce()) }
