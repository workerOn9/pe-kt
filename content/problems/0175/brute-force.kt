/**
 * PE 175 — brute force：直接枚举 n，用递推算 f(n)/f(n-1)，
 * 取每个分数首次出现的 n，并把 n 的二进制做游程编码得到 SBE。
 * 用于与 Calkin–Wilf 树算法对拍（小分数、n 上限内）。
 */
private val memo = HashMap<Long, Long>()

fun f(n: Long): Long {
    if (n <= 1L) return 1L
    memo[n]?.let { return it }
    val h = n / 2
    val v = if (n % 2 == 1L) f(h) else f(h) + f(h - 1)
    memo[n] = v
    return v
}

fun sbeOf(n: Long): List<Int> {
    val bits = java.lang.Long.toBinaryString(n)
    val runs = ArrayList<Int>()
    var cur = bits[0]; var len = 1
    for (i in 1 until bits.length) {
        if (bits[i] == cur) len++ else { runs.add(len); cur = bits[i]; len = 1 }
    }
    runs.add(len)
    return runs
}

fun main() {
    // 13/17 的最小 n 应为 241，SBE = 4,3,1
    for (n in 1L..100000L) {
        val p = f(n); val q = f(n - 1)
        if (p * 17 == q * 13) { println("13/17 -> n=$n SBE=" + sbeOf(n)); break }
    }
}
