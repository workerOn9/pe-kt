/**
 * Project Euler 145 — 暴力解（教学对比用）
 *
 * 与 solution.kt 的思路完全不同：不做任何数位配对或进位 DP，而是**按定义逐个枚举**
 * 每个候选数 n，用整数除法把它反转，再把 n + reverse(n) 的每一位拿出来看奇偶。
 * 优化解一次候选枚举都不做（只处理 19 个可能的数位对、2 个进位比特、9 个数位长度），
 * 本解要老老实实走完整个候选区间；两者唯一共享的前提只是题面语义本身
 * （末位非 0 保证 reverse(n) 无前导零），所以它给出的是对 DP 建模的独立佐证。
 *
 * 复杂度：时间 O(lim · d)（d ≤ 9 为位数，反转与判奇偶都是 O(d)），空间 O(1)。
 * 全部运算在 Int 内完成：lim ≤ 10⁹ 时 n ≤ 10⁹−1，n + reverse(n) ≤ 2 × 10⁹−2 < 2³¹−1。
 *
 * 规模说明（重要）：足规模 lim = 10⁹ 在本机实跑过一次，得到与优化解相同的 608720，
 * 耗时 114221.3212 ms——超过 60 s 的可用上限，故默认规模降一档到 10⁸，baseline 取
 * 该规模的实测值。降规模不改变输出的答案：位数长度 1…8 的候选（即 n < 10⁸）里已经
 * 包含全部 608720 个可逆数，9 位数一个都没有（见 analysis.md 的分位数表）。
 *
 * 题面样例（36 + 63 = 99、409 + 904 = 1313、一千以内 120 个）写成运行时断言。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 整数反转（不经过字符串）。 */
fun reverseOf(n: Int): Int {
    var rest = n
    var reversed = 0
    while (rest > 0) {
        reversed = reversed * 10 + rest % 10
        rest /= 10
    }
    return reversed
}

/** n 是否可逆：末位非 0（reverse(n) 无前导零），且 n + reverse(n) 的十进制各位全为奇数。 */
fun isReversible(n: Int): Boolean {
    if (n <= 0 || n % 10 == 0) return false
    var sum = n + reverseOf(n)
    while (sum > 0) {
        if (sum % 10 % 2 == 0) return false
        sum /= 10
    }
    return true
}

/** [1, limit) 中可逆数的个数（limit 为 10 的幂时正好覆盖「位数 ≤ log₁₀ limit」的全部候选）。 */
fun solveBruteForce(limit: Int = 100_000_000): Long {
    var count = 0L
    var n = 1
    while (n < limit) {
        if (isReversible(n)) count++
        n++
    }
    return count
}

fun main() {
    check(reverseOf(36) == 63 && reverseOf(409) == 904 && reverseOf(10) == 1)   // 反转本身
    check(listOf(36, 63, 409, 904).all { isReversible(it) }) { "36/63/409/904 应当都可逆" }
    check(!isReversible(10))               // 10 + 1 = 11 全奇数，但 reverse(10) = 1 带前导零 → 不算
    check(!isReversible(11))               // 11 + 11 = 22
    check(solveBruteForce(1000) == 120L) { "一千以内应为 120，实得 ${solveBruteForce(1000)}" }
    check(solveBruteForce(100_000) == 720L)   // 1…5 位共 0 + 20 + 100 + 600 + 0
    System.err.println("题面与边界断言全部通过")

    repeat(3) { solveBruteForce(10_000_000) } // JIT 预热（1/10 规模）
    val start = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
