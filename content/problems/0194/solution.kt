package dev.pekt.problems

import java.math.BigInteger

/**
 * Problem 194: Coloured Configurations
 *
 * 思路：图染色的 Chromatic Polynomial（色多项式）
 *
 * 两种单元 A 和 B 各自对应的图有固定色多项式：
 *   S_A(c) = c^5 - 9c^4 + 34c^3 - 69c^2 + 77c - 38
 *   S_B(c) = c^5 - 8c^4 + 27c^3 - 50c^2 + 52c - 24
 *
 * 当构建 a 个 A 和 b 个 B 的链时，首尾两个端点可任选颜色，其余 a+b 个连接处共享颜色：
 *   N(a, b, c) = C(a+b, a) * c * (c-1) * [S_A(c)]^a * [S_B(c)]^b
 *
 * 验证样例：
 *   N(1,0,3) = C(1,1) * 3*2 * SA(3)^1 * SB(3)^0 = 6 * 4 = 24 ✓
 *   N(0,2,4) = C(2,0) * 4*3 * SB(4)^2 = 12 * 88^2 = 92928 ✓
 *   N(2,2,3) = C(4,2) * 3*2 * SA(3)^2 * SB(3)^2 = 6*6*16*36 = 20736 ✓
 *
 * 复杂度：O((a+b)·log(mod))，只需做乘方运算。
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private const val MOD = 100_000_000L

fun SA(c: Long): Long {
    val c2 = (c * c) % MOD
    val c3 = (c2 * c) % MOD
    val c4 = (c3 * c) % MOD
    val c5 = (c4 * c) % MOD
    var r = c5 - 9 * c4
    if (r < 0) r += MOD
    r = (r + 34 * c3) % MOD
    val sub1 = (69 * c2) % MOD
    r = (r - sub1 + MOD) % MOD
    r = (r + 77 * c) % MOD
    r = (r - 38 + MOD) % MOD
    return r
}

fun SB(c: Long): Long {
    val c2 = (c * c) % MOD
    val c3 = (c2 * c) % MOD
    val c4 = (c3 * c) % MOD
    val c5 = (c4 * c) % MOD
    var r = c5 - 8 * c4
    if (r < 0) r += MOD
    r = (r + 27 * c3) % MOD
    val sub1 = (50 * c2) % MOD
    r = (r - sub1 + MOD) % MOD
    r = (r + 52 * c) % MOD
    r = (r - 24 + MOD) % MOD
    return r
}

private fun binomial(n: Int, k: Int): BigInteger {
    if (k < 0 || k > n) return BigInteger.ZERO
    if (k == 0 || k == n) return BigInteger.ONE
    var kk = k
    if (kk > n - kk) kk = n - kk
    var res = BigInteger.ONE
    for (i in 0 until kk) {
        res = res.multiply(BigInteger.valueOf(n.toLong() - i))
        res = res.divide(BigInteger.valueOf(i.toLong() + 1))
    }
    return res
}

fun solve194(): Long {
    val a = 25L
    val b = 75L
    val c = 1984L
    val n = a + b
    
    // Use BigInteger throughout to avoid overflow
    val bigC = c.toBigInteger()
    val bigMOD = MOD.toBigInteger()
    
    val bigSA = SA(c).toBigInteger()
    val bigSB = SB(c).toBigInteger()
    val bigComb = binomial(n.toInt(), a.toInt())
    
    val result = bigComb * bigC * (bigC - BigInteger.ONE) * 
                 bigSA.pow(a.toInt()) * bigSB.pow(b.toInt())
    
    val modResult = result.mod(bigMOD)
    return modResult.toLong()
}

private fun modPow(base: Long, exp: Long): Long {
    var b = (base % MOD + MOD) % MOD
    var e = exp
    var result = 1L
    while (e > 0) {
        if (e and 1L == 1L) result = result * b % MOD
        b = b * b % MOD
        e = e ushr 1
    }
    return result
}

fun main() {
    println("SA(3) = ${SA(3)}")
    println("SB(3) = ${SB(3)}")
    println("SA(4) = ${SA(4)}")
    println("SB(4) = ${SB(4)}")
    println("N(1,0,3) = ${binomial(1,1).multiply(BigInteger.valueOf(3)).multiply(BigInteger.valueOf(2)).multiply(SA(3).toBigInteger()) % MOD.toBigInteger()}")
    println("N(0,2,4) = ${binomial(2,0).multiply(BigInteger.valueOf(4)).multiply(BigInteger.valueOf(3)).multiply(SB(4).toBigInteger().pow(2)) % MOD.toBigInteger()}")
    println("N(2,2,3) = ${binomial(4,2).multiply(BigInteger.valueOf(3)).multiply(BigInteger.valueOf(2)).multiply(SA(3).toBigInteger().pow(2)).multiply(SB(3).toBigInteger().pow(2)) % MOD.toBigInteger()}")
    println("Result: ${solve194()}")
}
