/**
 * PE 182 — brute force：在极小规模 (p=19, q=37) 下直接暴力枚举明文 m 逐一验算 m^e mod n，
 * 验证 (1 + gcd(e-1, p-1)) * (1 + gcd(e-1, q-1)) 公式的正确性。
 */
private fun powerMod(base: Long, exp: Long, mod: Long): Long {
    var res = 1L
    var b = base % mod
    var e = exp
    while (e > 0L) {
        if (e % 2L == 1L) res = (res * b) % mod
        b = (b * b) % mod
        e /= 2L
    }
    return res
}

private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

fun main() {
    val p = 19L
    val q = 37L
    val n = p * q
    val phi = (p - 1L) * (q - 1L)

    // 对前几个合法的 e，比对暴力模拟计数与数论公式
    for (e in 3L until 50L step 2L) {
        if (gcd(e, phi) == 1L) {
            var bruteCount = 0L
            for (m in 0L until n) {
                if (powerMod(m, e, n) == m) {
                    bruteCount++
                }
            }
            val formulaCount = (1L + gcd(e - 1L, p - 1L)) * (1L + gcd(e - 1L, q - 1L))
            check(bruteCount == formulaCount) { "Mismatch at e=$e: brute=$bruteCount formula=$formulaCount" }
        }
    }
    println("Small prime brute verification passed successfully!")
}
