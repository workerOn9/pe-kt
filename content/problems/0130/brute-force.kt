/**
 * Project Euler 130 — 暴力解（教学对比用）
 *
 * 与 solution.kt 的路线完全不同。优化解走代数路线：由 n | R(k) ⟺ 10^k ≡ 1 (mod 9n) 得到
 * A(n) = ord_n(10)，用最小素因子筛把 n 分解成素数幂，再靠「φ 逐因子下降」求每个分量的阶并取 lcm。
 * 本解完全按定义朴素迭代：维护 R(k) mod n 的余数 r，从 r = 0、k = 1 出发反复执行
 *     r ← (10r + 1) mod n
 * 这个余数序列恰好是 R(1), R(2), … 模 n 的值；余数首次回到 0 时的 k 就是 A(n)（余数为 0
 * 当且仅当 n | R(k)）。整个过程中不做任何因式分解，也不出现素数幂、欧拉函数或 lcm。
 *
 * 复杂度：素性筛 O(L log log L)，空间 O(L)；求 A(n) 的代价是 O(A(n)) 次模乘加，
 * 累计 O(Σ A(n))（遍历所有与 30 互素的合数候选）。这正是它比优化解慢几十倍的根源：
 * 优化解每个候选只要 O(ω(φ)·log² n) 次模乘，而这里要对每个候选真跑完整个循环节。
 * 本题在 L = 5·10⁴、第 25 个值 14701 以内共扫描 2203 个候选，Σ A(n) = 2859149
 *（平均 1298 步；最贵的是 12769 = 113²，要迭代 12656 次）。
 *
 * 题面样例写成运行时断言：前五个合数 91/259/451/481/703、素数例 A(7) = 6、A(41) = 5，
 * 以及第三条判据（10^(n−1) ≡ 1 (mod 9n)）对全部 25 个合格值的复验。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 埃氏筛：isPrime[x] 表示 x 为素数。 */
fun sieve(limit: Int): BooleanArray {
    val isPrime = BooleanArray(limit + 1) { true }
    if (limit >= 0) isPrime[0] = false
    if (limit >= 1) isPrime[1] = false
    var i = 2
    while (i.toLong() * i <= limit) {
        if (isPrime[i]) {
            var j = i * i
            while (j <= limit) {
                isPrime[j] = false
                j += i
            }
        }
        i++
    }
    return isPrime
}

/** 按定义求 A(n)：r ← (10r + 1) mod n 迭代到余数为 0，返回迭代步数 k。 */
fun aOf(n: Int): Int {
    var r = 0L
    var k = 0
    do {
        r = (r * 10 + 1) % n
        k++
    } while (r != 0L)
    return k
}

fun modPow(base: Long, exp: Long, mod: Long): Long {
    var b = base % mod
    var e = exp
    var r = 1L
    while (e > 0L) {
        if (e and 1L == 1L) r = r * b % mod
        b = b * b % mod
        e = e shr 1
    }
    return r
}

/** 可选上限内前 target 个合格合数之和；不足 target 个时返回 null。 */
fun solveBruteForce(target: Int, limit: Int): Long? {
    val isPrime = sieve(limit)
    var count = 0
    var sum = 0L
    var n = 7
    while (n <= limit) {
        if (n % 2 != 0 && n % 3 != 0 && n % 5 != 0 && !isPrime[n]) {
            val a = aOf(n)
            if ((n - 1) % a == 0) {
                count++
                sum += n
                if (count == target) return sum
            }
        }
        n++
    }
    return null
}

/** 前 target 个合格合数（用于断言与展示）。 */
fun firstQualifying(target: Int, limit: Int): List<Int> {
    val isPrime = sieve(limit)
    val out = ArrayList<Int>()
    var n = 7
    while (n <= limit && out.size < target) {
        if (n % 2 != 0 && n % 3 != 0 && n % 5 != 0 && !isPrime[n] &&
            (n - 1) % aOf(n) == 0
        ) out.add(n)
        n++
    }
    return out
}

fun main() {
    // 题面样例：A(7) = 6、A(41) = 5、A(91) = 6
    check(aOf(7) == 6) { "A(7) 应为 6" }
    check(aOf(41) == 5) { "A(41) 应为 5" }
    check(aOf(91) == 6) { "A(91) 应为 6" }
    // 题面样例：前五个合数例子
    val firstFive = firstQualifying(5, 1000)
    check(firstFive == listOf(91, 259, 451, 481, 703)) { "前五个应为 91/259/451/481/703，实际 $firstFive" }
    // 3 | n 的候选永不合格：定义式给出的 A(n) 是 3 的倍数（27 | 9n ⇒ 3 | A(n)），而 n − 1 ≡ 2 (mod 3)
    for (n in 3..3000 step 3) {
        if (n % 2 == 0 || n % 5 == 0) continue
        check(aOf(n) % 3 == 0 && (n - 1) % aOf(n) != 0) { "3 | $n 竟合格" }
    }
    // 第三条判据交叉验证：A(n) | n − 1 ⟺ 10^(n−1) ≡ 1 (mod 9n)（费马式判据，不做循环迭代）
    for (n in firstQualifying(25, 50_000)) {
        check(modPow(10L, (n - 1).toLong(), 9L * n) == 1L) { "$n 未通过费马式判据" }
    }

    var limit = 50_000
    var answer: Long? = null
    while (answer == null) {
        answer = solveBruteForce(25, limit)
        if (answer == null) limit *= 2
    }
    // 诊断输出走 stderr：stdout 的契约是「只输出答案」，最后一行必须是答案
    System.err.println("first25 = ${firstQualifying(25, limit)}")

    repeat(3) { solveBruteForce(25, limit) }         // JIT 预热
    val start = System.nanoTime()
    val timed = solveBruteForce(25, limit)!!
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(timed)
}
