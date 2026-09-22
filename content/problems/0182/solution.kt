/**
 * PE 182 — RSA 密码体制中使未隐藏消息数最小的 e 之和。
 *
 * 原理：
 * m^e ≡ m (mod n) ⟺ m^e ≡ m (mod p) 且 m^e ≡ m (mod q)。
 * 对于质数 p：
 *   m(m^(e-1) - 1) ≡ 0 (mod p)。
 *   m = 0 是 1 个解；
 *   非零解 m ∈ Z_p^* 满足 m^(e-1) ≡ 1 (mod p)。在循环群 Z_p^* 中，
 *   解的个数恰为 gcd(e - 1, p - 1)。
 * 因此模 p 的解总数为 1 + gcd(e - 1, p - 1)。
 * 由中国剩余定理，模 n 的未隐藏消息总数即两者乘积：
 *   N_unconcealed(e) = (1 + gcd(e - 1, p - 1)) * (1 + gcd(e - 1, q - 1))。
 * 因为 gcd(e, φ) = 1，e 为奇数，e - 1 为偶数；而 p - 1, q - 1 亦为偶数，
 * 故 gcd(e - 1, p - 1) >= 2 且 gcd(e - 1, q - 1) >= 2。
 * 最小未隐藏消息数恰为 (1 + 2) * (1 + 2) = 9。
 * 遍历所有 1 < e < φ 且 gcd(e, φ) = 1 的奇数 e，
 * 累加满足 gcd(e - 1, p - 1) == 2 且 gcd(e - 1, q - 1) == 2 的 e。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

fun solve182(p: Long = 1009L, q: Long = 3643L): Long {
    val phi = (p - 1L) * (q - 1L)
    val pMinus1 = p - 1L
    val qMinus1 = q - 1L

    var sumE = 0L
    var e = 3L
    while (e < phi) {
        if (gcd(e, phi) == 1L) {
            val em1 = e - 1L
            if (gcd(em1, pMinus1) == 2L && gcd(em1, qMinus1) == 2L) {
                sumE += e
            }
        }
        e += 2L
    }
    return sumE
}

fun main() {
    // 题面样例：p = 19, q = 37, phi = 648
    // e = 181 时，gcd(180, 18) = 18, gcd(180, 36) = 36 -> (1+18)*(1+36) = 19*37 = 703 全部未隐藏
    val eTest = 181L
    val unTest = (1L + gcd(eTest - 1L, 18L)) * (1L + gcd(eTest - 1L, 36L))
    check(unTest == 703L) { "Sample e=181 should give 703 unconcealed" }

    val answer = solve182()
    println(answer)
}
