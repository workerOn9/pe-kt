/**
 * Project Euler 130 — Composites with Prime Repunit Property
 *（具有素数循环单位数性质的合数）
 *
 * 思路：R(k) = (10^k − 1)/9，于是
 *     n | R(k) ⟺ 9n | 10^k − 1 ⟺ 10^k ≡ 1 (mod 9n)，
 * 即 A(n) = ord_{9n}(10)。当 3 ∤ n 时 9 与 n 互素，由中国剩余定理
 *     ord_{9n}(10) = lcm(ord_9(10), ord_n(10)) = ord_n(10)，
 * 因为 10 ≡ 1 (mod 9) 故 ord_9(10) = 1。所以候选只需满足 gcd(n, 10) = 1、3 ∤ n，
 * 也就是 gcd(n, 30) = 1。
 *
 * 3 | n 的候选永远不合格：设 A(n) = ord_{9n}(10)，由 9 | n 得 27 | 9n，
 * 而 10^k − 1 = 9·R(k) 被 27 整除要求 3 | R(k) = (10^k − 1)/9，即 k ≡ 0 (mod 3)
 *（R(k) 的数字和为 k）。故 3 | A(n)；但 3 | n 时 n − 1 ≡ 2 (mod 3)，A(n) 不可能整除 n − 1。
 *
 * 求 ord_n(10)：把 n 分解为互素的素数幂 ∏ p_i^{e_i}，各分量取 ord_{p^e}(10) 再取 lcm。
 * 由 (Z/p^e)* 的阶为 φ(p^e) = p^{e−1}(p − 1) 知 ord_{p^e}(10) | φ(p^e)：从 d = φ(p^e)
 * 出发，对 φ(p^e) 的每个素因子 q 反复尝试 d ← d/q（若 10^{d/q} ≡ 1 (mod p^e) 则接受），
 * 试尽后得到的 d 就是最小指数。判定用模幂，全部数值 < 10^6，Long 内不必担心溢出。
 *
 * 素数 p 在这条判据下自动成立（费马小定理给出 ord_p(10) | p − 1），因此只测试合数。
 *
 * 复杂度：最小素因子筛 O(L log log L)，空间 O(L)；每个候选 n 的分解 O(log n)，
 * 求阶 O(ω(φ) · log² n)（ω 为素因子个数，模幂每步 O(log n)），合计 O(L log² L)。
 * 扫描规模按 5·10⁴ 起步、不足则翻倍；本题第 25 个值是 14701，只用到第一档。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 最小素因子筛：spf[x] = x 的最小素因子，spf[1] = 1。 */
fun smallestPrimeFactors(limit: Int): IntArray {
    val spf = IntArray(limit + 1) { it }
    var i = 2
    while (i.toLong() * i <= limit) {
        if (spf[i] == i) {
            var j = i * i
            while (j <= limit) {
                if (spf[j] == j) spf[j] = i
                j += i
            }
        }
        i++
    }
    return spf
}

/** 快速幂取模：base^exp mod mod，base、mod 均 < 2^31。 */
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

fun gcdLong(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

/** x 的所有互异素因子（x ≥ 1，需 spf 覆盖到 x）。 */
fun distinctPrimeFactors(x: Int, spf: IntArray): List<Int> {
    val out = ArrayList<Int>()
    var y = x
    while (y > 1) {
        val p = spf[y]
        out.add(p)
        while (y % p == 0) y /= p
    }
    return out
}

/**
 * ord_{p^e}(10)，要求 gcd(10, p) = 1。pe = p^e 由调用方给出以免重复求幂。
 * 算法：d ← φ(p^e)，对 φ(p^e) 的每个素因子 q 反复做「能整除就把 d 缩小」的下降。
 */
fun orderModPrimePower(p: Long, e: Int, pe: Long, spf: IntArray): Long {
    var d = pe / p * (p - 1)                       // φ(p^e) = p^(e−1)·(p − 1)
    for (q in distinctPrimeFactors(d.toInt(), spf)) {
        while (d % q == 0L && modPow(10L, d / q, pe) == 1L) d /= q
    }
    return d
}

/** ord_m(10)，要求 gcd(m, 10) = 1；m 分解为素数幂后各分量取 lcm。 */
fun orderOf10(m: Int, spf: IntArray): Long {
    var x = m
    var ord = 1L
    while (x > 1) {
        val p = spf[x].toLong()
        var pe = 1L
        var e = 0
        while (x % p.toInt() == 0) {               // 剥出整个素数幂 p^e
            x /= p.toInt()
            pe *= p
            e++
        }
        val part = orderModPrimePower(p, e, pe, spf)
        ord = ord / gcdLong(ord, part) * part
    }
    return ord
}

/**
 * 扫描 [2, limit]，返回前 target 个「gcd(n, 10) = 1、n 为合数、A(n) | n − 1」的 n 之和；
 * 不足 target 个时返回 null（由调用方放大 limit）。
 */
fun scan(limit: Int, target: Int): Long? {
    val spf = smallestPrimeFactors(limit)
    var count = 0
    var sum = 0L
    var n = 7                                       // 最小候选：合数且与 30 互素
    while (n <= limit) {
        if (n % 2 != 0 && n % 3 != 0 && n % 5 != 0 && spf[n] != n) {
            val a = orderOf10(n, spf)
            if ((n - 1) % a == 0L) {
                count++
                sum += n
                if (count == target) return sum
            }
        }
        n++
    }
    return null
}

/** 规模倍增：先扫 5·10⁴，不够就翻倍，直到凑满 target 个候选。 */
fun solve(target: Int = 25): Long {
    var limit = 50_000
    while (true) {
        val sum = scan(limit, target)
        if (sum != null) return sum
        limit *= 2
    }
}

/** 与 30 互素的合数中满足 A(n) | n − 1 的 n 的升序列表（仅用于校验）。 */
fun candidateSet(limit: Int, spf: IntArray): List<Int> {
    val out = ArrayList<Int>()
    for (n in 7..limit) {
        if (n % 2 != 0 && n % 3 != 0 && n % 5 != 0 && spf[n] != n &&
            (n - 1) % orderOf10(n, spf) == 0L
        ) out.add(n)
    }
    return out
}

/** 第三条判据：A(n) | n − 1 ⟺ 10^(n−1) ≡ 1 (mod 9n)，只用一次模幂（仅用于校验）。 */
fun fermatSet(limit: Int, spf: IntArray): List<Int> {
    val out = ArrayList<Int>()
    for (n in 7..limit) {
        if (n % 2 != 0 && n % 3 != 0 && n % 5 != 0 && spf[n] != n &&
            modPow(10L, (n - 1).toLong(), 9L * n) == 1L
        ) out.add(n)
    }
    return out
}

/** m 是否无平方因子（m ≥ 2）。 */
fun squareFree(m: Int, spf: IntArray): Boolean {
    var x = m
    while (x > 1) {
        val p = spf[x]
        var e = 0
        while (x % p == 0) { x /= p; e++ }
        if (e > 1) return false
    }
    return true
}

/** 按定义直接算 A(n)：r ← (10r + 1) mod n 迭代到 0，返回最小步数（仅用于校验）。 */
fun aByDefinition(n: Int): Int {
    var r = 0L
    var k = 0
    do {
        r = (r * 10 + 1) % n
        k++
    } while (r != 0L)
    return k
}

fun verifySample() {
    val spf = smallestPrimeFactors(100_000)
    // 题面样例：A(7) = 6、A(41) = 5
    check(orderOf10(7, spf) == 6L) { "A(7) 应为 6" }
    check(orderOf10(41, spf) == 5L) { "A(41) 应为 5" }
    check(aByDefinition(7) == 6 && aByDefinition(41) == 5)
    // 定义式与求阶公式在 7..2000 上必须一致（两条路径的交叉校验）
    for (m in 7..2000) if (m % 2 != 0 && m % 5 != 0 && m % 3 != 0) {
        check(aByDefinition(m).toLong() == orderOf10(m, spf)) { "A($m) 两条路径不一致" }
    }
    // 题面样例：素数 p > 5 满足 A(p) | p − 1
    for (m in 7..2000) if (spf[m] == m && m > 5) {
        check((m - 1) % orderOf10(m, spf) == 0L) { "素数 $m 违反 A(p) | p − 1" }
    }
    // 前五个合数例子
    check(scan(1000, 5) == 91L + 259 + 451 + 481 + 703) { "前五个例子应为 91/259/451/481/703" }
    check(scan(1000, 5) == 1985L)
    // 平方因子 p² | n 会强制 p | A(n)：p 非 base-10 Wieferich 时 ord_{p²}(10) = p·ord_p(10)，
    // 而 ord_p(10) | p − 1 故 p ∤ ord_p(10)；于是 A(n) | n − 1 会推出 p | n − 1，与 p | n 矛盾。
    // 因此合格值必无平方因子（最小的非平凡 base-10 Wieferich 素数是 487，487² 远大于本题扫描范围）。
    check(orderOf10(113 * 113, spf) == 113L * 112L) { "A(113²) 应为 113·112" }
    val qualifying = candidateSet(30_000, spf)
    check(qualifying.all { squareFree(it, spf) }) { "合格集内出现含平方因子的数" }
    check(qualifying.subList(0, 5) == listOf(91, 259, 451, 481, 703))
    // 第三条独立路径：费马式判据（一次模幂）给出的合格集必须与求阶判据逐项一致
    check(fermatSet(30_000, spf) == qualifying) { "两种判据的合格集不一致" }
    // 第 25 个合格值恰好是 14701：上限 14700 只能凑到 24 个，抬到 14701 才凑满
    check(scan(14_700, 25) == null) { "14700 以内不该凑满 25 个" }
    check(scan(14_701, 25) == 149253L) { "14701 以内前 25 个之和应为 149253" }
    // 3 | n 永远不合格：此时 A(n) = ord_{9n}(10) 是 3 的倍数，而 n − 1 ≡ 2 (mod 3)
    val spfBig = smallestPrimeFactors(27_000)
    for (n in 3..3000 step 3) {
        if (n % 2 == 0 || n % 5 == 0) continue
        val a = orderOf10(9 * n, spfBig)            // 直接对 9n 求阶，与 n 互素性无关
        check(a % 3 == 0L && (n - 1) % a != 0L) { "3 | $n 竟满足条件" }
    }
    // 循环单位数末位恒为 1，既不被 2 也不被 5 整除，故 gcd(n, 10) = 1 是前提
    var rep = 0L
    var ten = 1L
    for (k in 1..12) {
        rep = rep * 10 + 1
        ten *= 10
        check(rep == (ten - 1) / 9) { "R($k) 与 (10^k − 1)/9 不符" }
        check(rep % 2 == 1L && rep % 5 == 1L)
    }
}

fun main() {
    verifySample()
    repeat(5) { solve() }                           // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
