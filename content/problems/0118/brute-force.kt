/**
 * Project Euler 118 — 暴力解 / 交叉验证（教学对比用）
 *
 * 与 solution.kt 结论相同但路径完全不同，用来互证：
 *
 * 1. **素数表**：solution.kt 用埃氏筛 + 试除，这里改成「先枚举该数字集的所有排列得到整数，
 *    再用 `BigInteger.isProbablePrime` 判素」，多位数先按末位与数字和快速排除；
 * 2. **计数**：solution.kt 是「含最小数字的块优先」的记忆化 DFS，这里改用
 *    **集合分割的穷举**——把数字 1..9 依次放入已有块或新块（受限增长串），得到 $\{1,\dots,9\}$
 *    的全部 $B_9 = 21147$ 个分割，每个分割的合法集合数 = 各块素数个数之积，再求和；
 * 3. **第三种聚合**：直接数「有序的素数序列」（DP：$H_m(R)=\sum_{s\subseteq R}\mathrm{pc}[s]\,H_{m-1}(R\setminus s)$），
 *    再用「$m$ 个互不相同的素数恰好有 $m!$ 种排列」除以 $m!$ 还原成集合数。
 *
 * 三条路径（掩码 DFS / 分割穷举 / 有序除阶乘）互相独立，若三者一致则计数模型基本无虞。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

private val SMALL_PRIMES = intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97)

private fun isPrime(value: Int): Boolean {
    if (value < 2) return false
    for (p in SMALL_PRIMES) {
        if (value == p) return true
        if (value % p == 0) return false
    }
    return BigInteger.valueOf(value.toLong()).isProbablePrime(30)
}

/** 用「枚举排列」的方式统计每个数字掩码上能拼出的素数个数。 */
fun primeCountsByPermutation(): IntArray {
    val counts = IntArray(512)
    for (mask in 1..511) {
        val digits = ArrayList<Int>()
        for (d in 1..9) if (mask shr (d - 1) and 1 == 1) digits.add(d)
        val sum = digits.sum()
        // 多位数只要数字和是 3 的倍数就必被 3 整除，可以直接跳过；单数字掩码不能用这个捷径
        // （{3} 的和是 3，但 3 本身是素数），漏掉它会让含块 {3} 的集合全部丢失。
        if (digits.size > 1 && sum % 3 == 0) continue
        val picked = BooleanArray(digits.size)
        var found = 0
        fun build(value: Int, depth: Int) {
            if (depth == digits.size) {
                if (isPrime(value)) found++
                return
            }
            for (i in digits.indices) {
                if (!picked[i]) {
                    picked[i] = true
                    build(value * 10 + digits[i], depth + 1)
                    picked[i] = false
                }
            }
        }
        build(0, 0)
        counts[mask] = found
    }
    return counts
}

/** 穷举 1..9 的全部集合分割（受限增长串），累加各块素数个数之积。 */
fun countByPartitions(counts: IntArray): Long {
    var total = 0L
    val blocks = IntArray(9)
    fun place(digit: Int, blockCount: Int) {
        if (digit == 10) {
            var product = 1L
            for (b in 0 until blockCount) {
                val choices = counts[blocks[b]]
                if (choices == 0) return
                product *= choices.toLong()
            }
            total += product
            return
        }
        val bit = 1 shl (digit - 1)
        for (b in 0 until blockCount) {
            blocks[b] = blocks[b] or bit
            place(digit + 1, blockCount)
            blocks[b] = blocks[b] and bit.inv()
        }
        blocks[blockCount] = bit
        place(digit + 1, blockCount + 1)
    }
    place(1, 0)
    return total
}

/** 有序序列计数再除以 m!：$H_m(R)=\sum_{s\subseteq R}\mathrm{pc}[s]\cdot H_{m-1}(R\setminus s)$。 */
fun countByOrderedDp(counts: IntArray, mask: Int): Long {
    val h = Array(10) { LongArray(512) }
    h[0][0] = 1L
    for (m in 1..9) {
        for (rest in 1..mask) {
            if (rest and mask != rest) continue
            var sum = 0L
            var s = rest
            while (s > 0) {
                val choices = counts[s]
                if (choices != 0) sum += choices.toLong() * h[m - 1][rest xor s]
                s = (s - 1) and rest
            }
            h[m][rest] = sum
        }
    }
    var factorial = 1L
    var sets = 0L
    for (m in 1..9) {
        factorial *= m
        sets += h[m][mask] / factorial
    }
    return sets
}

fun solveBruteForce(): Long = countByPartitions(primeCountsByPermutation())

fun main() {
    val counts = primeCountsByPermutation()

    // 小样例锚点（可纸上枚举）：{1,2,3} 得 {2,13}、{2,31} 共 2 个；
    // {1,2,3,4} 共 9 个：单块 1423/2143/2341/4231，两块 {2,431}、{3,241}、{3,421}、{23,41}，
    // 三块 {2,3,41}。
    val anchor123 = countByOrderedDp(counts, 0b000000111)
    val anchor1234 = countByOrderedDp(counts, 0b000001111)
    println("锚点：F({1,2,3}) = $anchor123（应为 2），F({1,2,3,4}) = $anchor1234（应为 9）")
    check(anchor123 == 2L && anchor1234 == 9L) { "小样例锚点失败" }

    val byPartitions = countByPartitions(counts)
    println("集合分割穷举：$byPartitions")
    val byOrdered = countByOrderedDp(counts, 511)
    println("有序序列除 m!：$byOrdered")
    check(byPartitions == byOrdered) { "两条独立路径不一致：$byPartitions vs $byOrdered" }
}
