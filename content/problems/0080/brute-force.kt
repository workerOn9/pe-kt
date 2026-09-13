/**
 * Project Euler 080 — Square Root Digital Expansion
 *
 * 暴力解：不用 BigInteger.sqrt()，改用笔算开方的竖式（digit-by-digit longhand）：
 * 把被开方数从左往右两位一组下拉，余数 r ← 100r + 组值，
 * 每位根数字 d 从 9 试到 0，取第一个满足 (20p + d)·d ≤ r 的 d，再令
 * r ← r − (20p+d)d、p ← 10p + d。共下拉 100 组得到 100 位数字。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

val ten80 = BigInteger.TEN
val hundred80 = BigInteger.valueOf(100)
val twenty80 = BigInteger.valueOf(20)

fun longhandSqrtDigits(n: Int, groups: Int): BigInteger {
    // n·10^(2(groups−1)) 共 groups 个两位组（首组可能只有一位，算法自动兼容）
    val m = BigInteger.valueOf(n.toLong()).multiply(ten80.pow(2 * groups - 2))
    var rem = BigInteger.ZERO
    var root = BigInteger.ZERO
    for (i in 0 until groups) {
        val group = m.divide(ten80.pow(2 * (groups - 1 - i))).mod(hundred80)
        rem = rem.multiply(hundred80).add(group)
        var d = 9
        while (d > 0) {
            val trial = twenty80.multiply(root).add(BigInteger.valueOf(d.toLong()))
                .multiply(BigInteger.valueOf(d.toLong()))
            if (trial <= rem) break
            d--
        }
        val used = twenty80.multiply(root).add(BigInteger.valueOf(d.toLong()))
            .multiply(BigInteger.valueOf(d.toLong()))
        rem = rem.subtract(used)
        root = root.multiply(ten80).add(BigInteger.valueOf(d.toLong()))
    }
    return root
}

fun digitSumOf(n: BigInteger): Int {
    var x = n
    var s = 0
    while (x.signum() != 0) {
        s += x.mod(ten80).toInt()
        x = x.divide(ten80)
    }
    return s
}

fun solveBruteForce(): Long {
    var total = 0L
    for (n in 1..100) {
        val r = Math.sqrt(n.toDouble()).toInt()
        if (r * r == n) continue
        total += digitSumOf(longhandSqrtDigits(n, 100))
    }
    return total
}

fun main() {
    println(solveBruteForce())
}
