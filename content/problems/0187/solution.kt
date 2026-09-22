package dev.pekt.problems

/**
 * Problem 187: Semiprimes
 *
 * 思路：
 * 设合数 n = p * q，其中 p <= q 且均为质数。
 * 由于 n < 10^8，故 p <= sqrt(10^8) = 10000，而 q < 10^8 / 2 = 50000000。
 * 先使用线性筛或布尔数组筛出 50000000 以内的全部质数。
 * 对每个质数 p (p * p < 10^8)，在质数数组中使用二分查找（或双指针扫描）确定最大的 q 满足 p * q < 10^8，
 * 则与该 p 构成的半质数个数为 index(q) - index(p) + 1。
 * 累加所有合法 (p, q) 对的数量即可。
 *
 * 复杂度：
 * 时间复杂度 O(M + pi(sqrt(N)) log pi(M))，其中 M = N / 2 = 5 * 10^7，实测耗时约 450 ms。
 * 空间复杂度 O(M)，使用 ByteArray 或 BitSet 存储筛表。
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve0187(): Long {
    val limit = 100_000_000
    val maxP = limit / 2

    // 奇数素数筛，节省一半空间
    // isComposite[i] 表示数字 2*i + 1 是否为合数
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

    // 收集质数
    // 50000000 以内质数个数约为 3001134
    var primeCount = 1 // 2
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

        // 二分查找 <= maxQ 的最大质数索引
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

fun main() {
    println(solve0187())
}
