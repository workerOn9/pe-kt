/**
 * Project Euler 124 — Ordered Radicals（根基排序）
 *
 * 思路：rad(n) 是 n 的不同质因子的乘积。把 rad 全部初始化为 1，遇到
 * rad(p)=1 的 p≥2 就发现一个质数，对它的所有倍数各乘一次 p。
 * 因 rad(n)≤n≤N，以根基为键做计数排序：前缀和记录每个根基桶的起点，
 * 再按 n 递增稳定填桶，即得到按 (rad(n),n) 字典序排列的整数。
 * 复杂度：O(N log log N) 时间、O(N) 空间；N=100000，取一基序号 10000。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun orderedRadicals(limit: Int): IntArray {
    require(limit >= 1 && limit < Int.MAX_VALUE)
    val radicals = IntArray(limit + 1) { 1 }
    for (p in 2..limit) {
        if (radicals[p] == 1) {
            for (multiple in p..limit step p) radicals[multiple] *= p
        }
    }
    val next = IntArray(limit + 1)
    for (n in 1..limit) next[radicals[n]]++
    var offset = 0
    for (r in 1..limit) {
        val count = next[r]
        next[r] = offset
        offset += count
    }
    val ordered = IntArray(limit)
    for (n in 1..limit) ordered[next[radicals[n]]++] = n
    return ordered
}

fun solve(): Long = orderedRadicals(100_000)[9_999].toLong()

fun main() { println(solve()) }
