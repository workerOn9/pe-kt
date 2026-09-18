/**
 * Project Euler 138 — Special Isosceles Triangles（特殊的等腰三角形）
 *
 * 思路：设底边 b、腰 L、高 h，由等腰三角形的对称性 h² + (b/2)² = L²。
 *
 * ① 底边必为偶数。若 b 为奇数，则 h = b ± 1 为偶数，两边乘 4 后取模 4：
 *    4L² = 4h² + b²，左端 ≡ 0，右端 ≡ 0 + 1 ≡ 1 (mod 4)，矛盾。故设 b = 2x（x ≥ 1）。
 *
 * ② 把 h = 2x ± 1 代入勾股关系，得到单变量方程
 *      5x² ± 4x + 1 = L²。
 *    两边乘 5 并配方：(5x ± 2)² = 5L² − 1。令 u = 5x ± 2，即得 Pell 方程
 *      u² − 5L² = −1。
 *
 * ③ 反向也成立：任何正解都有 u² ≡ −1 ≡ 4 (mod 5)，故 u ≡ ±2 (mod 5)，
 *    x = (u ∓ 2)/5 必为整数。u ≡ 2 时对应 u = 5x + 2、h = 2x + 1 = b + 1；
 *    u ≡ 3 时对应 u = 5x − 2、h = 2x − 1 = b − 1。于是「h = b ± 1 的等腰三角形」
 *    与「u² − 5L² = −1 的正解」一一对应。
 *
 * ④ u² − 5L² = −1 的全部正解由 u + L√5 = (2 + √5)^(2k+1)（k ≥ 0）给出。相邻两个
 *    奇次幂相差因子 (2 + √5)² = 9 + 4√5，展开即得整数递推
 *      u' = 9u + 20L,   L' = 4u + 9L。
 *    从最小正解 (u, L) = (2, 1) 出发迭代，L（以及 b）严格递增，
 *    所以前 count 个解就是前 count 小的三角形。
 *
 * ⑤ (2, 1) 给出 x = 0，底边退化为 0，不是正整数三角形，跳过；此后每个解恰好
 *    给出一个三角形，且 h 在 b − 1 与 b + 1 之间交替。
 *
 * 复杂度：时间 O(count)，每次迭代 O(1) 次 Long 乘加，与题目规模无关；空间 O(count)。
 * 步长 (2 + √5)² ≈ 17.944，第 12 个 L ≈ 1.056×10^15，中间量最大约 2.4×10^15，
 * Long 足够（和 ≈ 1.118×10^15）。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 一个满足 h = b ± 1 的等腰三角形：底边 base、腰 leg、高 height。 */
data class Triangle(val base: Long, val leg: Long, val height: Long)

/**
 * 按底边（等价地按腰长）从小到大列出前 count 个满足 h = b ± 1 的等腰三角形。
 * 解由 Pell 方程 u² − 5L² = −1 的基本单位幂 (2 + √5)^(2k+1) 给出，
 * 用整数递推 u' = 9u + 20L、L' = 4u + 9L 逐项生成。
 */
fun specialTriangles(count: Int): List<Triangle> {
    require(count >= 1)
    val result = ArrayList<Triangle>(count)
    var u = 2L              // u + L√5 = (2 + √5)^1：最小的正解（4 − 5 = −1）
    var leg = 1L
    while (result.size < count) {
        val nextU = 9 * u + 20 * leg
        val nextLeg = 4 * u + 9 * leg
        u = nextU
        leg = nextLeg
        // u mod 5 ∈ {2, 3}；=2 给 h = b + 1，=3 给 h = b − 1
        val plusOne = u % 5 == 2L
        val halfBase = if (plusOne) (u - 2) / 5 else (u + 2) / 5
        if (halfBase == 0L) continue            // (2, 1) 的退化情形：b = 0
        val base = 2 * halfBase
        result.add(Triangle(base, leg, if (plusOne) base + 1 else base - 1))
    }
    return result
}

/** 前 count 个三角形的腰长之和。 */
fun solve(count: Int = 12): Long = specialTriangles(count).sumOf { it.leg }

fun verifySample() {
    val triangles = specialTriangles(12)
    check(triangles.size == 12)

    // 题面给出的两个样例
    val first = triangles[0]
    check(first.base == 16L && first.leg == 17L && first.height == 15L) { "第一个三角形应为 (16, 17, 15)" }
    val second = triangles[1]
    check(second.base == 272L && second.leg == 305L && second.height == 273L) { "第二个三角形应为 (272, 305, 273)" }

    for ((index, t) in triangles.withIndex()) {
        check(t.base > 0 && t.leg > 0 && t.height > 0) { "边长必须为正：$t" }
        check(t.base % 2 == 0L) { "底边必须为偶数：$t" }
        val half = t.base / 2
        check(t.leg * t.leg == half * half + t.height * t.height) { "勾股关系不成立：$t" }
        check(t.leg > t.height) { "腰必须是最长边：$t" }
        check(t.height == t.base + 1 || t.height == t.base - 1) { "高必须与底边差 1：$t" }
        // h 在 b − 1（奇数位）与 b + 1（偶数位）之间交替
        check(if (index % 2 == 0) t.height == t.base - 1 else t.height == t.base + 1) { "符号未交替：$t" }
        if (index > 0) check(t.base > triangles[index - 1].base) { "底边必须严格递增：$t" }
    }

    // 与题面「第二个最小的三角形」一致：不存在更小的第三个解被漏掉
    check(solve(2) == 17L + 305L)

    // 解链的横向不变量：L_k 是 (2 + √5)^(2k+1) 的线性组合，故满足二阶线性递推
    // L_{k+1} = 18L_k − L_{k−1}（18 = 迹 (9 + 4√5)）。它只用相邻三项，
    // 与逐次乘 (2 + √5)² 的写法相互制约。
    for (index in 2 until triangles.size) {
        check(triangles[index].leg == 18 * triangles[index - 1].leg - triangles[index - 2].leg) {
            "腰长应满足 L_{k+1} = 18L_k − L_{k−1}：$triangles"
        }
    }
}

fun main() {
    verifySample()
    repeat(5) { solve() }                        // JIT 预热
    // solve() 只有 12 次迭代、耗时在微秒级，单次计时被调度噪声淹没：
    // 每次计时跑 1000 遍取单遍均值，再取 5 次独立计时的最小值（并发机器上最稳的估计）。
    var bestMs = Double.MAX_VALUE
    repeat(5) {
        val start = System.nanoTime()
        repeat(1000) { solve() }
        bestMs = minOf(bestMs, (System.nanoTime() - start) / 1e6 / 1000.0)
    }
    System.err.printf("optimized: %.4f ms%n", bestMs)
    println(solve())
}
