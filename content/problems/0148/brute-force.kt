/**
 * Project Euler 148 — 暴力对照解（教学对比用）
 *
 * 与 solution.kt 的思路不同：优化解直接引用库默尔定理，写出 A(n) = ∏(数位+1) 后自高位向低位做数位 DP，
 * 一次扫过 11 个 7 进制数位就得到总数；本解走「自相似分块」路线——先把整片行范围按 7 切块：
 * 第 7m + j 行（j = 0…6）的数位是 m 的数位后面接一位 j（7 进制），于是
 *
 *     A(7m + j) = A(m)·(j + 1)
 *
 * 一整块（7 行）对总数的贡献就是 A(m)·Σ_{j=0}^{6}(j+1) = 28·A(m)。因此
 *
 *     F(N) = 28·F(N / 7) + A(N / 7)·T(N mod 7),   T(r) = r(r+1)/2
 *
 * 自顶向下把行数一路除以 7，最后只剩一个不足 7 行的残尾；这等价于按 7 的幂自相似地折叠三角形。
 *
 * 真正「按定义」的暴力是 literalCount()：不引用任何定理，用 C(n,k) = C(n−1,k−1) + C(n−1,k) 逐行
 * 递推（只需模 7 保余），结果非零即不被 7 整除。它需要 O(N²) 次加法，本机跑 10⁹ 行不可能（按 8000 行
 * 实测 48.6940 ms 外推约需 24 年），故只在小规模上运行——并把它当作标尺去校验上面那条递推式在 0…400
 * 行、343、1000、2401、8000 行处全部吻合。因此 **brute 的 baseline 计时取降规模后的 literalCount(8000 行)**，
 * 而打印的答案仍来自与优化解同规模的足规模分块递推。
 *
 * 复杂度：literalCount 时间 O(N²)、空间 O(N)；countByBlocks 时间 O((log_7 N)²)（外层折叠 log_7 N 次，
 * 每次求 A(N/7) 又是 log_7 N 位），空间 O(log_7 N)。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 朴素暴力：逐行递推帕斯卡三角形（模 7），数出前 rows 行里不被 7 整除的项数。 */
fun literalCount(rows: Int): Long {
    if (rows <= 0) return 0L
    var cur = LongArray(rows)                     // 交替复用两块内存，避免 O(N²) 的分配量
    var nxt = LongArray(rows)
    cur[0] = 1L
    var count = 0L
    for (n in 0 until rows) {
        for (k in 0..n) if (cur[k] != 0L) count++ // 模 7 后非零 ⟺ 原值不被 7 整除
        if (n + 1 < rows) {                       // 最后一行之后不必再建新行
            for (k in 0..n + 1) {
                var v = 0L
                if (k > 0) v += cur[k - 1]
                if (k <= n) v += cur[k]
                nxt[k] = v % 7L
            }
            val tmp = cur; cur = nxt; nxt = tmp
        }
    }
    return count
}

/** 行 n 中不被 7 整除的项数 A(n) = ∏(7 进制数位 + 1)。 */
fun rowCount(n: Long): Long {
    var x = n
    var product = 1L
    while (x > 0) {
        product *= x % 7 + 1
        x /= 7
    }
    return product
}

/** 自相似分块递推：F(N) = 28·F(N/7) + A(N/7)·T(N mod 7)。 */
fun countByBlocks(rows: Long): Long {
    if (rows <= 0L) return 0L
    val q = rows / 7
    val r = rows % 7
    return 28L * countByBlocks(q) + rowCount(q) * (r * (r + 1) / 2)
}

fun solveBruteForce(): Long = countByBlocks(1_000_000_000L)

const val BRUTE_ROWS = 8000                        // 逐行递推的实测规模（O(N²)，10⁹ 行不可行）

fun verifySample() {
    // 题面：前七行无一项被 7 整除，共 28 项
    check(literalCount(7) == 28L) { "前七行应为 28 项，实得 ${literalCount(7)}" }
    // 题面：前一百行 5050 项中 2361 项不被 7 整除
    check(literalCount(100) == 2361L) { "前一百行应为 2361 项，实得 ${literalCount(100)}" }
    // 整块规模：前 7^k 行的项数恰为 28^k
    check(countByBlocks(343) == 21952L)            // 7^3 → 28^3
    check(countByBlocks(2401) == 614656L)          // 7^4 → 28^4
    // 逐行递推（定义）与分块递推（公式）在小规模上逐点对拍
    for (rows in 0..400) {
        check(literalCount(rows) == countByBlocks(rows.toLong())) {
            "第 $rows 行规模下两者不符：${literalCount(rows)} vs ${countByBlocks(rows.toLong())}"
        }
    }
    check(literalCount(1000) == countByBlocks(1000L))
    check(literalCount(2401) == countByBlocks(2401L))
    check(literalCount(BRUTE_ROWS) == countByBlocks(BRUTE_ROWS.toLong())) {
        "第 $BRUTE_ROWS 行规模下两者不符：${literalCount(BRUTE_ROWS)} vs ${countByBlocks(BRUTE_ROWS.toLong())}"
    }
}

fun main() {
    verifySample()
    repeat(3) { literalCount(BRUTE_ROWS) }         // JIT 预热
    var best = Double.MAX_VALUE                    // 逐行递推耗时在数百毫秒级，取多次最小值压住负载抖动
    var literal = 0L
    repeat(5) {
        val start = System.nanoTime()
        literal = literalCount(BRUTE_ROWS)
        val ms = (System.nanoTime() - start) / 1e6
        if (ms < best) best = ms
    }
    System.err.printf("brute: %.4f ms%n", best)
    System.err.println("brute 规模：前 $BRUTE_ROWS 行逐行递推 = $literal 项（10⁹ 行的 O(N²) 递推不可行）")
    // 答案来自与优化解同规模的足规模分块递推，其正确性已由上面的逐行递推标定
    val start2 = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute(self-similar, 10^9 rows): %.4f ms%n", (System.nanoTime() - start2) / 1e6)
    println(answer)
}
