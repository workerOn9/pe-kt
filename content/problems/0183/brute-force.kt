/**
 * PE 183 — brute force：对 N = 5 到 100 逐一验证连续整数 k 的枚举是否确实由 N/e 给出极值，
 * 验证求导极值点的严谨性。
 */
import kotlin.math.ln

private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

fun main() {
    for (n in 5..100) {
        var maxVal = Double.NEGATIVE_INFINITY
        var bruteK = 1
        for (k in 1..(2 * n)) {
            val v = k * (ln(n.toDouble()) - ln(k.toDouble()))
            if (v > maxVal) {
                maxVal = v
                bruteK = k
            }
        }
        val kNear = (n / Math.E).toInt()
        check(bruteK == kNear || bruteK == kNear + 1) { "Optimal k not near N/e for n=$n" }
    }
    println("Optimal k around N/e verified for all N in 5..100!")
}
