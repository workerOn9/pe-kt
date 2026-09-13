/**
 * Project Euler 057 — Square Root Convergents
 *
 * 优化解：设第 k 个展开为 p_k / q_k，由 x_{k+1} = 1 + 1/(1 + x_k) 得递推
 * (p, q) -> (p + 2q, p + q)（首项 3/2）。
 * 分子分母只用「二进制大整数」之外的十进制数位数组表示：递推里只有加倍与加法，
 * 不需要乘法，于是「数位个数」天然等于数组长度——直接比较长度即可，
 * 完全绕开把大整数转成字符串再数字位的开销。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 十进制大整数，低位在前，最高位非零（0 表示为 [0]） */
fun p057Add(a: IntArray, b: IntArray): IntArray {
    val n = maxOf(a.size, b.size)
    val r = IntArray(n + 1)
    var carry = 0
    for (i in 0 until n) {
        val s = (if (i < a.size) a[i] else 0) + (if (i < b.size) b[i] else 0) + carry
        r[i] = s % 10
        carry = s / 10
    }
    r[n] = carry
    var len = n + 1
    while (len > 1 && r[len - 1] == 0) len--
    return if (len == n + 1) r else r.copyOf(len)
}

fun p057Double(a: IntArray): IntArray {
    val r = IntArray(a.size + 1)
    var carry = 0
    for (i in a.indices) {
        val s = 2 * a[i] + carry
        r[i] = s % 10
        carry = s / 10
    }
    r[a.size] = carry
    var len = a.size + 1
    while (len > 1 && r[len - 1] == 0) len--
    return if (len == a.size + 1) r else r.copyOf(len)
}

fun solve(): Long {
    var num = intArrayOf(3)
    var den = intArrayOf(2)
    var count = 0L
    for (k in 1..1000) {
        if (k > 1) {
            val nextNum = p057Add(num, p057Double(den))
            val nextDen = p057Add(num, den)
            num = nextNum
            den = nextDen
        }
        if (num.size > den.size) count++
    }
    return count
}

fun main() {
    println(solve())
}
