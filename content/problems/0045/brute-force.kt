/**
 * Project Euler 045 — 暴力解（教学对比用）
 *
 * 分别生成三角形数、五边形数、六边形数三个集合，再从小到大找公共元素。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    val tris = HashSet<Long>(); val pents = HashSet<Long>(); val hexes = HashSet<Long>()
    for (i in 1..100_000) { tris.add(i.toLong() * (i + 1) / 2); pents.add(i.toLong() * (3L * i - 1) / 2); hexes.add(i.toLong() * (2L * i - 1)) }
    for (i in 1..100_000) { val h = i.toLong() * (2L * i - 1); if (h > 40755 && pents.contains(h) && tris.contains(h)) return h }
    return 0
}

fun main() {
    println(solveBruteForce())
}
