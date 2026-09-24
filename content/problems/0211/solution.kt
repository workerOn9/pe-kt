#!/usr/bin/env kotlin
// PE 211 — Divisor Square Sum（因数平方和）
// 思路：sigma_2 是积性函数，n = Π p_i^{e_i} 时
//       sigma_2(n) = Π (1 + p_i^2 + p_i^4 + ... + p_i^{2 e_i})。
//       朴素做法要开 size = 6.4e7 的最小质因子表（IntArray 256 MB）与 sigma_2 表
//       （LongArray 512 MB），共 768 MB，会超出执行引擎 512 MB 的工作堆。
//       改用分段筛：把 [1, 64e6) 切成 1e6 一块，每块只保留
//       rest（尚未除尽的余因子，IntArray）与 sigma（已累乘的乘积，LongArray）两个数组，
//       对每个小素数 p（p < sqrt(64e6)）在块内扫它的所有倍数并整除去 p 的幂，
//       扫完后 rest > 1 者必为素数，再乘上 (rest^2 + 1)。内存降为 12 MB。
// 复杂度：小素数表 O(sqrt(N) log log sqrt(N))，分段筛 O(N log log N)，N = 6.4e7。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

private const val LIMIT = 64_000_000
private const val BLOCK = 1_000_000

private fun solve211(): Long {
    // 小素数表：只需 p <= sqrt(LIMIT - 1) < 8000
    val root = 8_000
    val composite = BooleanArray(root + 1)
    val primes = ArrayList<Int>(1200)
    for (p in 2..root) {
        if (composite[p]) continue
        primes.add(p)
        var j = p.toLong() * p
        while (j <= root) {
            composite[j.toInt()] = true
            j += p
        }
    }

    val rest = IntArray(BLOCK)
    val sigma = LongArray(BLOCK)
    var total = 0L
    var lo = 1
    while (lo < LIMIT) {
        val hi = minOf(lo + BLOCK, LIMIT)
        val size = hi - lo
        for (i in 0 until size) {
            rest[i] = lo + i
            sigma[i] = 1L
        }

        for (p in primes) {
            var first = ((lo + p - 1) / p) * p
            var i = first - lo
            while (i < size) {
                val r = rest[i]
                if (r % p == 0) {
                    var m = r
                    var e = 0
                    while (m % p == 0) {
                        m /= p
                        e++
                    }
                    rest[i] = m
                    // F = 1 + p^2 + ... + p^(2e)，逐项累乘避免幂运算
                    val p2 = p.toLong() * p
                    var term = 1L
                    var factor = 1L
                    repeat(e) {
                        term *= p2
                        factor += term
                    }
                    sigma[i] *= factor
                }
                i += p
            }
        }

        for (i in 0 until size) {
            val value = if (rest[i] > 1) {
                // 余因子必为素数（否则它会有两个大于 sqrt(n) 的因子）
                val q = rest[i].toLong()
                sigma[i] * (q * q + 1L)
            } else {
                sigma[i]
            }
            var r = Math.sqrt(value.toDouble()).toLong()
            while (r * r > value) r--
            while ((r + 1) * (r + 1) <= value) r++
            if (r * r == value) total += lo + i
        }
        lo = hi
    }
    return total
}

fun main() {
    println(solve211())
}
