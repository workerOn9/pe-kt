/**
 * Project Euler 023 — Non-Abundant Sums
 *
 * 优化解：三步打表。
 * 1) 筛法求 1..28123 的真因子和，标记盈数；
 * 2) 枚举盈数对（a ≤ b，和 ≤ 28123 即 break），用布尔数组标记可表示的数；
 * 3) 把未被标记的数求和。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

const val LIMIT = 28123                     // 数学上界：大于它的整数都能写成两个盈数之和

fun solve(): Long {
    // 1) 筛法求真因子和
    val d = IntArray(LIMIT + 1)
    for (f in 1..LIMIT / 2) {
        var m = 2 * f
        while (m <= LIMIT) {
            d[m] += f
            m += f
        }
    }
    val abundants = (12..LIMIT).filter { d[it] > it }

    // 2) 标记所有两个盈数之和
    val expressible = BooleanArray(LIMIT + 1)
    for (i in abundants.indices) {
        for (j in i until abundants.size) {
            val s = abundants[i] + abundants[j]
            if (s > LIMIT) break
            expressible[s] = true
        }
    }

    // 3) 求不能表示者的和
    var sum = 0L
    for (n in 1..LIMIT) {
        if (!expressible[n]) sum += n
    }
    return sum
}

fun main() {
    println(solve())
}
