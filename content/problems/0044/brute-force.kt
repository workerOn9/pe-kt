/**
 * Project Euler 044 — 暴力解（教学对比用）
 *
 * 预生成前 3000 个五边形数存入集合，双重循环遍历所有数对取最小差值。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun pentagonal(n: Int): Long { return n.toLong() * (3L * n - 1) / 2 }

fun solveBruteForce(): Long {
    val set = HashSet<Long>()
    val list = ArrayList<Long>()
    var n = 1
    while (list.size < 3000) { val p = pentagonal(n); list.add(p); set.add(p); n++ }
    var best = Long.MAX_VALUE
    for (j in list.indices) for (k in j + 1 until list.size) {
        val s = list[j] + list[k]; val dd = list[k] - list[j]
        if (dd >= best) break
        if (set.contains(s) && set.contains(dd)) best = dd
    }
    return best
}

fun main() {
    println(solveBruteForce())
}
