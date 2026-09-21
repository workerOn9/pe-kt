/**
 * PE 168 — brute force：枚举所有 d 位数，检查是否整除自己的右旋转。
 * 只适合 d <= 6（10^6 量级），用于与数论公式对拍。
 */
fun bruteForce(d: Int): List<Long> {
    val out = ArrayList<Long>()
    var n = 1L
    for (i in 1 until d) n *= 10
    val end = n * 10
    while (n < end) {
        val rot = (n % 10) * (end / 10) + n / 10
        if (rot % n == 0L) out.add(n)
        n++
    }
    return out
}

fun main() {
    for (d in 2..6) println("d=$d -> " + bruteForce(d).size + " 个: " + bruteForce(d).take(6))
}
