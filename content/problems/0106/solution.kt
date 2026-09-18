/**
 * Project Euler 106 — Special Subset Sums: Meta-testing（特殊和集：元测试）
 *
 * 思路：集合严格递增且第二条规则（元素多者和更大）已成立，所以只有「等势」的真子集对
 * 还可能需要测相等。把一对等势不相交子集写成 B = {b₁<…<b_k}、C = {c₁<…<c_k}，
 * 按下标逐位比较：若 b_i < c_i 对全体 i 成立（或反向全体成立），则由元素大小关系直接
 * 得到 S(B) < S(C)，无需称重；只有逐位比较「有交叉」的对才真正需要测。
 * 于是计数化为纯组合问题：先取并集（C(n,2k) 种），再把有序的 2k 元组切成两个 k 元子集。
 * 不上升路径（b_i < c_i 恒成立）恰是 Dyson 路径，数为 Catalan 数 C_k = (1/(k+1))·C(2k,k)，
 * 故需要测的对数
 *   T(n) = Σ_{k=1}^{⌊n/2⌋} C(n,2k) · ( C(2k,k)/2 − Catalan_k )。
 * 复杂度：O(n) 项求和，每项两次组合数；n = 12 时全部中间量 < 10⁶，Long 足够。
 * 校验锚点：题面给出 n=4 → 1、n=7 → 70，以及总对数 (3ⁿ−2^{n+1}+1)/2 在 n=12 为 261625，
 * 三处都在 main 里做运行时断言。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 精确组合数 C(n, m)，逐步乘除保持整除。 */
fun binom(n: Int, m: Int): Long {
    val k = minOf(m, n - m).toLong()
    var result = 1L
    for (i in 0L until k) {
        result = result * (n - i) / (i + 1)      // 每步 result·(n−i) 是连续 (i+1) 个整数之积，整除
    }
    return result
}

/** 第 k 个 Catalan 数 C_k = (1/(k+1))·C(2k,k)。 */
fun catalan(k: Int): Long = binom(2 * k, k) / (k + 1L)

/** 从 n 元严格递增集中取出的「真正需要测相等」的等势不相交子集对数。 */
fun pairsNeedingTest(n: Int): Long =
    (1..n / 2).sumOf { k ->
        val union = binom(n, 2 * k)               // 选并集（2k 个位置）
        val splits = binom(2 * k, k) / 2          // 无序拆成两个 k 元子集
        union * (splits - catalan(k))             // 减去被大小关系直接判定的（Dyson）拆法
    }

/** 题面口径的全部非空不相交子集对数：(3ⁿ − 2^{n+1} + 1)/2。 */
fun allSubsetPairs(n: Int): Long {
    var powersOfThree = 1L
    repeat(n) { powersOfThree *= 3 }
    return (powersOfThree - (1L shl (n + 1)) + 1) / 2
}

fun solve(): Long {
    check(pairsNeedingTest(4) == 1L && pairsNeedingTest(7) == 70L) { "题面锚点不符" }
    return pairsNeedingTest(12)
}

fun main() {
    check(allSubsetPairs(4) == 25L && allSubsetPairs(7) == 966L && allSubsetPairs(12) == 261625L) {
        "子集对总数与题面不符：${allSubsetPairs(4)}, ${allSubsetPairs(7)}, ${allSubsetPairs(12)}"
    }
    for (n in 1..12) println("n = $n：共 ${allSubsetPairs(n)} 对，需测 ${pairsNeedingTest(n)} 对")
    println(solve())
}
