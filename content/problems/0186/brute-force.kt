package dev.pekt.problems

/**
 * Problem 186: Connectedness of a Network (Brute Force / Verification Baseline)
 *
 * 朴素并查集对照实现，不带按秩合并优化（纯单链压缩并查集），用于基准性能与正确性双向对照。
 */

fun solve0186BruteForce(): Long {
    val totalUsers = 1_000_000
    val targetSize = 990_000
    val primeMinister = 524287

    val parent = IntArray(totalUsers) { it }
    val size = IntArray(totalUsers) { 1 }

    fun find(i: Int): Int {
        var r = i
        while (r != parent[r]) r = parent[r]
        var curr = i
        while (curr != r) {
            val nxt = parent[curr]
            parent[curr] = r
            curr = nxt
        }
        return r
    }

    fun union(i: Int, j: Int) {
        val rootI = find(i)
        val rootJ = find(j)
        if (rootI != rootJ) {
            parent[rootJ] = rootI
            size[rootI] += size[rootJ]
        }
    }

    val s = IntArray(6_000_000)
    for (k in 1..55) {
        val kLong = k.toLong()
        val term = (100003L - 200003L * kLong + 300007L * kLong * kLong * kLong) % totalUsers
        s[k] = ((term % totalUsers + totalUsers) % totalUsers).toInt()
    }
    for (k in 56 until s.size) {
        s[k] = (s[k - 24] + s[k - 55]) % totalUsers
    }

    var successfulCalls = 0L
    var k = 1
    while (k < s.size) {
        val u = s[k++]
        val v = s[k++]
        if (u == v) continue
        successfulCalls++
        union(u, v)
        if (size[find(primeMinister)] >= targetSize) {
            return successfulCalls
        }
    }
    return -1L
}

fun main() {
    println(solve0186BruteForce())
}
