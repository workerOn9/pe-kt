package dev.pekt.engine

import java.math.BigDecimal
import java.math.MathContext

// Solvers for Problems 186 to 190

internal fun solve186Impl(): Long {
    val totalUsers = 1_000_000
    val targetSize = 990_000
    val primeMinister = 524287

    val parent = IntArray(totalUsers) { it }
    val size = IntArray(totalUsers) { 1 }

    fun find(i: Int): Int {
        var root = i
        while (root != parent[root]) {
            root = parent[root]
        }
        var curr = i
        while (curr != root) {
            val nxt = parent[curr]
            parent[curr] = root
            curr = nxt
        }
        return root
    }

    fun union(i: Int, j: Int) {
        val rootI = find(i)
        val rootJ = find(j)
        if (rootI != rootJ) {
            if (size[rootI] < size[rootJ]) {
                parent[rootI] = rootJ
                size[rootJ] += size[rootI]
            } else {
                parent[rootJ] = rootI
                size[rootI] += size[rootJ]
            }
        }
    }

    val lfg = IntArray(55)
    for (k in 1..55) {
        val kLong = k.toLong()
        val term = (100003L - 200003L * kLong + 300007L * kLong * kLong * kLong) % totalUsers
        val s = ((term % totalUsers + totalUsers) % totalUsers).toInt()
        lfg[k - 1] = s
    }

    var k = 55
    fun nextS(): Int {
        val idx55 = (k - 55) % 55
        val idx24 = (k - 24) % 55
        val s = (lfg[idx24] + lfg[idx55]) % totalUsers
        lfg[idx55] = s
        k++
        return s
    }

    var ptr = 0
    fun getS(): Int {
        return if (ptr < 55) lfg[ptr++] else nextS()
    }

    var successfulCalls = 0L
    while (true) {
        val u = getS()
        val v = getS()
        if (u == v) continue
        successfulCalls++
        union(u, v)
        if (size[find(primeMinister)] >= targetSize) {
            return successfulCalls
        }
    }
}

internal fun solve187Impl(): Long {
    val limit = 100_000_000
    val maxP = limit / 2

    val half = maxP / 2
    val isComposite = java.util.BitSet(half + 1)
    val sqrtHalf = (Math.sqrt(maxP.toDouble()).toInt() - 1) / 2

    for (i in 1..sqrtHalf) {
        if (!isComposite.get(i)) {
            val p = 2 * i + 1
            var j = 2 * i * (i + 1)
            while (j <= half) {
                isComposite.set(j)
                j += p
            }
        }
    }

    var primeCount = 1
    for (i in 1..half) {
        if (!isComposite.get(i)) primeCount++
    }

    val primes = IntArray(primeCount)
    primes[0] = 2
    var idx = 1
    for (i in 1..half) {
        if (!isComposite.get(i)) {
            primes[idx++] = 2 * i + 1
        }
    }

    var totalSemiprimes = 0L
    for (i in 0 until primeCount) {
        val p = primes[i].toLong()
        if (p * p >= limit) break
        val maxQ = ((limit - 1) / p).toInt()

        var low = i
        var high = primeCount - 1
        var best = i
        while (low <= high) {
            val mid = (low + high) ushr 1
            if (primes[mid] <= maxQ) {
                best = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        totalSemiprimes += (best - i + 1)
    }

    return totalSemiprimes
}

internal fun solve188Impl(): Long {
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

internal fun solve189Impl(): Long {
    val pow3 = IntArray(10) { 1 }
    for (i in 1..9) pow3[i] = pow3[i - 1] * 3

    var dp = LongArray(3) { 1L }

    for (row in 2..8) {
        val prevLen = row - 1
        val curLen = row
        val newDp = LongArray(pow3[curLen])

        fun getColor(state: Int, idx: Int): Int = (state / pow3[idx]) % 3

        val curColors = IntArray(curLen)

        fun dfs(col: Int, prevColors: IntArray, ways: Long) {
            if (col == curLen) {
                var state = 0
                for (i in 0 until curLen) {
                    state += curColors[i] * pow3[i]
                }
                newDp[state] += ways
                return
            }

            for (c in 0..2) {
                curColors[col] = c
                var factor = 1L
                if (col > 0) {
                    val p = prevColors[col - 1]
                    val uLeft = curColors[col - 1]
                    val uRight = c
                    val distinct = booleanArrayOf(false, false, false)
                    distinct[p] = true
                    distinct[uLeft] = true
                    distinct[uRight] = true
                    var count = 0
                    if (distinct[0]) count++
                    if (distinct[1]) count++
                    if (distinct[2]) count++
                    val validChoices = 3 - count
                    if (validChoices <= 0) continue
                    factor = validChoices.toLong()
                }
                dfs(col + 1, prevColors, ways * factor)
            }
        }

        val prevColors = IntArray(prevLen)
        for (prevState in 0 until pow3[prevLen]) {
            val ways = dp[prevState]
            if (ways == 0L) continue
            for (i in 0 until prevLen) {
                prevColors[i] = getColor(prevState, i)
            }
            dfs(0, prevColors, ways)
        }

        dp = newDp
    }

    return dp.sum()
}

internal fun solve190Impl(): Long {
    val mc = MathContext(60)

    fun computePmFloor(m: Int): Long {
        val mDec = BigDecimal(m)
        val denom = BigDecimal(m + 1)
        var prod = BigDecimal.ONE

        for (i in 1..m) {
            val num = BigDecimal(2 * i)
            val base = num.divide(denom, mc)
            val term = base.pow(i, mc)
            prod = prod.multiply(term, mc)
        }

        return prod.toBigInteger().toLong()
    }

    var totalSum = 0L
    for (m in 2..15) {
        totalSum += computePmFloor(m)
    }

    return totalSum
}
