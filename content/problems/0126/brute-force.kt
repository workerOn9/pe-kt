/**
 * Project Euler 126 — 暴力对照解（教学对比用）
 *
 * 与 solution.kt 的枚举方向相反：优化解以 (a, b, c) 为外层——每个长方体只访问一次，再逐层用
 * 「增量 = 4(a+b+c) + 8(k−1)」把第 1, 2, 3, … 层的大小一路加法推下去；本解把**层号 k 提到最外层**，
 * 对每个 (k, a, b) 把 c 从 b 起自增，每次重新按定义代入闭式
 *
 *     f(a,b,c,k) = 2(ab + ac + bc) + 4(a + b + c)(k − 1) + 4(k − 1)(k − 2)
 *
 * 求值（每次都按闭式重算、不留任何增量状态），f > limit 才停下。也就是说：优化解是「一个长方体、多层」，
 * 本解是「一层、多个长方体」，两条路径覆盖的 (长方体, 层) 对完全相同但生成顺序完全相反，没有任何共享的
 * 中间量，属于独立互证而非同一算法调参。代价是同一套 (a, b) 要按 k 重复枚举，且每个 (k,a,b,c)
 * 组合都要重算一次多项式，所以常数项明显更大。
 *
 * 复杂度：时间 O(K·A + W)，K 为层号上界（由 f(1,1,1,k) ≤ limit 定，limit = 32000 时 K = 89），
 *   A 为每个 k 下满足 f(a,b,b,k) ≤ limit 的 (a,b) 对数，W 为被记数的 (长方体, 层) 对数
 *   （与优化解同一个 W = 10050016）；空间 O(limit)。
 *
 * 题面样例（3×2×1 的 22/46/78/118、若干长方体的 22/46、C(22)=2、C(46)=4、C(78)=5、C(118)=8、
 * 最小的 C(n)=10 是 154）全部写成运行时断言。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 第 k 层立方体个数的闭式（k ≥ 1），与题面给出的 22/46/78/118 逐项对齐。 */
fun layerCount(a: Long, b: Long, c: Long, k: Long): Long =
    2L * (a * b + a * c + b * c) + 4L * (a + b + c) * (k - 1) + 4L * (k - 1) * (k - 2)

/**
 * 层号 k 在最外层；每个 (k, a, b) 下 c 从 b 起朴素自增，逐次代入闭式，f > limit 即停。
 * 计数单位是 (长方体, 层) 对：a ≤ b ≤ c 的长方体只算一次，每个 k ≥ 1 各算一次。
 */
fun countLayersByLayer(limit: Int, cnt: IntArray) {
    val lim = limit.toLong()
    var k = 1L
    while (layerCount(1, 1, 1, k) <= lim) {          // 最小长方体 (1,1,1) 的第 k 层已超限，k 到头
        var a = 1L
        while (layerCount(a, a, a, k) <= lim) {      // 固定 k 时 a = b = c 给出最小层大小
            var b = a
            while (layerCount(a, b, b, k) <= lim) {  // c = b 是 c ≥ b 中最小的层大小
                var c = b
                while (true) {
                    val f = layerCount(a, b, c, k)   // 每次重新按定义求值，不留增量状态
                    if (f > lim) break               // f 随 c 严格递增，可安全退出
                    cnt[f.toInt()]++
                    c++
                }
                b++
            }
            a++
        }
        k++
    }
}

/** 在给定上界内统计 C(n)，返回最小的满足 C(n) == target 的 n；找不到返回 -1。 */
fun leastWithCount(target: Int, limit: Int): Int {
    val cnt = IntArray(limit + 1)
    countLayersByLayer(limit, cnt)
    for (n in 1..limit) if (cnt[n] == target) return n
    return -1
}

/** 从小到大倍增上界，返回最小的满足 C(n) == target 的 n（与优化解同一套倍增口径，便于对比）。 */
fun solveBruteForce(target: Int = 1000): Long {
    var limit = 1000
    while (true) {
        val n = leastWithCount(target, limit)
        if (n > 0) return n.toLong()
        limit *= 2
    }
}

fun verifySample() {
    // 题面第一组：3×2×1 的第 1..4 层
    check(layerCount(3, 2, 1, 1) == 22L) { "3×2×1 第一层应为 22" }
    check(layerCount(3, 2, 1, 2) == 46L) { "3×2×1 第二层应为 46" }
    check(layerCount(3, 2, 1, 3) == 78L) { "3×2×1 第三层应为 78" }
    check(layerCount(3, 2, 1, 4) == 118L) { "3×2×1 第四层应为 118" }

    // 题面第二组：其它长方体的第一层
    check(layerCount(5, 1, 1, 1) == 22L) { "5×1×1 第一层应为 22" }
    check(layerCount(5, 3, 1, 1) == 46L) { "5×3×1 第一层应为 46" }
    check(layerCount(7, 2, 1, 1) == 46L) { "7×2×1 第一层应为 46" }
    check(layerCount(11, 1, 1, 1) == 46L) { "11×1×1 第一层应为 46" }

    // 题面 C(n) 的四个值，以及「最小的 C(n) = 10 是 154」
    val cnt = IntArray(2000)
    countLayersByLayer(1999, cnt)
    check(cnt[22] == 2) { "C(22) 应为 2，实测 ${cnt[22]}" }
    check(cnt[46] == 4) { "C(46) 应为 4，实测 ${cnt[46]}" }
    check(cnt[78] == 5) { "C(78) 应为 5，实测 ${cnt[78]}" }
    check(cnt[118] == 8) { "C(118) 应为 8，实测 ${cnt[118]}" }
    check(leastWithCount(10, 2000) == 154) { "最小 C(n) = 10 的 n 应为 154" }

    check(solveBruteForce(1000) < 32_000L) { "答案必须严格小于最后使用的上界" }
}

fun main() {
    verifySample()
    repeat(5) { solveBruteForce() }                  // JIT 预热
    val start = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
