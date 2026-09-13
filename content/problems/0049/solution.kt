/**
 * Project Euler 049 — Prime Permutations
 *
 * 优化解：先按「数位排序后的字符串」把四位数素数分桶，同桶内任意两项都是数字排列；
 * 只需在桶内两两配对、检查第三项 2b−a 是否同桶素数，搜索量从 10^6 降到千级。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun sortedDigits(n: Int): String { return n.toString().toCharArray().sorted().joinToString("") }

fun sieveBool(limit: Int): BooleanArray {
    val composite = BooleanArray(limit + 1)
    val result = BooleanArray(limit + 1) { it >= 2 }
    if (limit < 2) return result
    var p = 2
    while (p.toLong() * p <= limit) {
        if (!composite[p]) {
            var m = p * p
            while (m <= limit) { composite[m] = true; m += p }
        }
        p++
    }
    for (i in 2..limit) result[i] = !composite[i]
    return result
}

fun solve(): Long {
    val isP = sieveBool(9999)
    val buckets = HashMap<String, MutableList<Int>>()
    for (n in 1000..9999) if (isP[n]) buckets.getOrPut(sortedDigits(n)) { ArrayList() }.add(n)
    var best = 0L
    for ((key, list) in buckets) {
        for (i in list.indices) for (j in i + 1 until list.size) {
            val c = 2 * list[j] - list[i]
            if (c > 9999 || !isP[c] || sortedDigits(c) != key) continue
            val s = "" + list[i] + list[j] + c
            if (s == "148748178147") continue
            val v = s.toLong()
            if (v > best) best = v
        }
    }
    return best
}

fun main() {
    println(solve())
}
