/**
 * Project Euler 124 — 暴力解（逐数试除与比较排序，教学对比用）
 *
 * 思路：对每个 n 独立试除，发现质因子后只乘一次并除尽其全部幂次。
 * 然后按 (rad(n),n) 双键比较排序，不复用筛表或计数桶。
 * 复杂度：试除上界 O(N√N)，排序 O(N log N)，空间 O(N)。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun radicalByTrialDivision(n: Int): Int {
    require(n >= 1)
    var remaining = n
    var radical = 1
    var divisor = 2
    while (divisor <= remaining / divisor) {
        if (remaining % divisor == 0) {
            radical *= divisor
            do { remaining /= divisor } while (remaining % divisor == 0)
        }
        divisor++
    }
    if (remaining > 1) radical *= remaining
    return radical
}

fun orderedRadicalsByTrialDivision(limit: Int): List<Int> {
    require(limit >= 1 && limit < Int.MAX_VALUE)
    val radicals = IntArray(limit + 1)
    for (n in 1..limit) radicals[n] = radicalByTrialDivision(n)
    return (1..limit).sortedWith(compareBy<Int> { radicals[it] }.thenBy { it })
}

fun solveBruteForce(): Long = orderedRadicalsByTrialDivision(100_000)[9_999].toLong()

fun main() {
    check(radicalByTrialDivision(504) == 42)
    check(orderedRadicalsByTrialDivision(10) == listOf(1, 2, 4, 8, 3, 9, 5, 6, 7, 10))
    println(solveBruteForce())
}
