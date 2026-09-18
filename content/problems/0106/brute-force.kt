/**
 * Project Euler 106 — 暴力解（教学对比用）
 *
 * 与 solution.kt 同一个判据（等势且逐位大小关系有交叉的对才需要测），但完全不走组合公式：
 * 用 3ⁿ 进制的三态掩码枚举每一对非空不相交子集（每位 = 不属于 B/C、属于 B、属于 C），
 * 把两个子集的元素按升序取出来做逐位比较，直接数出「有交叉」的对数，再除以 2 去重
 * （每对无序对被两种赋值顺序各数一次）。没有任何组合恒等式、没有 Catalan 数、
 * 也不用二项式系数——只有枚举与比较，因此与 solution.kt 是两条独立的数值路径。
 * n = 12 时 3¹² = 531441 次枚举，仍在暴力可承受范围。
 *
 * 顺带把题面给出的两个锚点（n = 4 → 1、n = 7 → 70）与总数 261625 当运行时断言，
 * 用来兜住「三态编码错位」「逐位比较方向搞反」这两类典型失误。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 三态编码下第 i 位是否为 B/C 成员；返回该子集的元素升序列表。 */
private fun members(mask: Int, n: Int, digit: Int): List<Int> =
    (0 until n).filter { (mask / pow3(it)) % 3 == digit }

private fun pow3(exponent: Int): Int {
    var result = 1
    repeat(exponent) { result *= 3 }
    return result
}

/** 逐位比较：B 的每个元素都小于 C 的对应元素（或反向），则大小关系已判定，无需测相等。 */
private fun dominated(b: List<Int>, c: List<Int>): Boolean =
    b.indices.all { b[it] < c[it] } || b.indices.all { b[it] > c[it] }

/** 直接枚举所有非空不相交子集对，数出需要测相等的无序对数。 */
fun countByEnumeration(n: Int): Long {
    var ordered = 0L
    val total = pow3(n)
    for (mask in 0 until total) {
        val b = members(mask, n, 1)
        if (b.isEmpty()) continue
        val c = members(mask, n, 2)
        if (c.size != b.size) continue              // 势不同由第二条规则直接判定
        if (!dominated(b, c)) ordered++
    }
    return ordered / 2                              // 每个无序对被 (B,C) 与 (C,B) 各数一次
}

/** 全部非空不相交子集对（不分势），题面口径：(3ⁿ − 2^{n+1} + 1)/2。 */
fun countAllPairs(n: Int): Long {
    var ordered = 0L
    for (mask in 0 until pow3(n)) {
        val hasB = (0 until n).any { (mask / pow3(it)) % 3 == 1 }
        val hasC = (0 until n).any { (mask / pow3(it)) % 3 == 2 }
        if (hasB && hasC) ordered++
    }
    return ordered / 2
}

fun solveBruteForce(): Long = countByEnumeration(12)

fun main() {
    check(countAllPairs(4) == 25L && countAllPairs(7) == 966L && countAllPairs(12) == 261625L) {
        "总数锚点不符：${countAllPairs(4)}, ${countAllPairs(7)}, ${countAllPairs(12)}"
    }
    check(countByEnumeration(4) == 1L && countByEnumeration(7) == 70L) {
        "题面锚点不符：n=4 → ${countByEnumeration(4)}，n=7 → ${countByEnumeration(7)}"
    }
    for (n in 1..12) println("n = $n：共 ${countAllPairs(n)} 对，需测 ${countByEnumeration(n)} 对")
    println(solveBruteForce())
}
