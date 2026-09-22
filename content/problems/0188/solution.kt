package dev.pekt.problems

/**
 * Problem 188: Hyperexponentiation
 *
 * 思路：
 * 超乘方 a ^^ b mod m 的求法建立在扩展欧拉定理之上：
 * 对于任意正整数 a, b, m，若 b >= phi(m)，则：
 * a^b = a^( (b mod phi(m)) + phi(m) ) (mod m)
 * 模数 m = 10^8 = 2^8 * 5^8，phi(10^8) = 10^7 * 4 = 40_000_000。
 * 指数塔每上一层，模数取欧拉函数 phi(m)，由于 phi(m) 迅速衰减至 1，递归深度最多不过数十层。
 * 当模数衰减为 1 时，任何数 mod 1 均为 0。
 * 借助快速幂结合递归降模即可在 1 毫秒内求得准确值。
 *
 * 复杂度：
 * 时间复杂度 O(log^2 M)，实测耗时 < 1 ms。
 * 空间复杂度 O(log M) 递归调用栈。
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve0188(): Long {
    fun phi(n: Long): Long {
        var result = n
        var p = 2L
        var temp = n
        while (p * p <= temp) {
            if (temp % p == 0L) {
                while (temp % p == 0L) temp /= p
                result -= result / p
            }
            p++
        }
        if (temp > 1L) {
            result -= result / temp
        }
        return result
    }

    fun modPow(base: Long, exp: Long, mod: Long): Long {
        var res = 1L
        var b = base % mod
        var e = exp
        while (e > 0L) {
            if ((e and 1L) == 1L) res = (res * b) % mod
            b = (b * b) % mod
            e = e ushr 1
        }
        return res
    }

    fun tetrationMod(a: Long, b: Long, m: Long): Long {
        if (m == 1L) return 0L
        if (b == 1L) return a % m
        val phiM = phi(m)
        val exp = tetrationMod(a, b - 1L, phiM)
        return modPow(a, exp + phiM, m)
    }

    return tetrationMod(1777L, 1855L, 100_000_000L)
}

fun main() {
    println(solve0188())
}
