/**
 * Project Euler 123 — Prime Square Remainders（素数平方剩余）
 *
 * 思路：与 120 是同一套二项式塌缩，只是底数换成第 n 个素数 p_n。展开后含 p² 的项
 * 在 mod p² 下全灭，只剩
 *     n 为偶数：r ≡ 2        (mod p_n²)
 *     n 为奇数：r ≡ 2n·p_n   (mod p_n²)
 * 偶数下标的余数恒等于 2，永远够不到 10¹⁰；扫描按 n 递增，偶数下标只有一次分支判断，
 * 奇数下标才算一次 2n·p_n mod p_n²。注意必须真的取模：题面样例 n = 3 就是
 * 2·3·5 = 30 > 25、余数为 5 的情形（不取模会算成 30，直接与题面矛盾）；n 稍大后
 * p_n > 2n 使 2n·p_n < p_n²，取模成为恒等操作，但保留取模才能覆盖全部下标。
 *
 * 复杂度：埃氏筛 O(P log log P) 建表 + O(N) 扫描（N = 答案 ≈ 2.1×10⁴，P = 3×10⁵）。
 * 数值范围：p_n² ≤ 9×10¹⁰、2n·p_n ≤ 1.6×10¹⁰，全程 64 位整数，无需 BigInteger。
 * main 里把题面两条锚点写成断言：n = 3 时余数为 5；超过 10⁹ 的最小 n 为 7037。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 埃拉托斯特尼筛：limit 以内的全部素数，升序。 */
fun sievePrimes(limit: Int): IntArray {
    val composite = BooleanArray(limit + 1)
    val primes = ArrayList<Int>()
    for (candidate in 2..limit) {
        if (composite[candidate]) continue
        primes.add(candidate)
        if (candidate.toLong() * candidate > limit) continue
        var multiple = candidate * candidate
        while (multiple <= limit) {
            composite[multiple] = true
            multiple += candidate
        }
    }
    return primes.toIntArray()
}

/** 第 index 个素数为 prime 时的余数 r（index 从 1 开始）。 */
fun remainderAt(index: Int, prime: Long): Long =
    if (index % 2 == 0) 2L else 2L * index * prime % (prime * prime)

/** 使余数首次超过 threshold 的最小下标；素数表不够长时抛错而不是给出错误答案。 */
fun firstIndexExceeding(threshold: Long): Long {
    val primes = sievePrimes(300_000)
    for (index in 1..primes.size) {
        val prime = primes[index - 1].toLong()
        if (remainderAt(index, prime) > threshold) return index.toLong()
    }
    error("素数筛上界不足，无法覆盖阈值 $threshold")
}

fun solve(): Long = firstIndexExceeding(10_000_000_000L)

fun main() {
    check(remainderAt(3, 5L) == 5L) { "题面样例 n = 3 应得余数 5，实际 ${remainderAt(3, 5L)}" }
    check(firstIndexExceeding(1_000_000_000L) == 7037L) { "题面锚点：超过 10⁹ 的最小 n 应为 7037" }
    println(solve())
}
