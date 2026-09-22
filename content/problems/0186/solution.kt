package dev.pekt.problems

/**
 * Problem 186: Connectedness of a Network
 *
 * 思路：
 * 使用并查集（Disjoint Set Union, DSU）维护百万用户的连通块大小。
 * 序列生成使用固定大小为 55 的循环数组模拟滞后斐波那契生成器（LFG: k-24, k-55），避免巨量内存分配。
 * 对每对 (u, v)，若 u == v 则为误拨，忽略；否则成功通话数 +1 并合并 u 和 v 所在的集合。
 * 当首相（524287）所在连通块大小达到 990000 时，返回当前累计的成功通话数。
 *
 * 复杂度：
 * 时间复杂度 O(N * alpha(V))，其中 N ≈ 2.3 * 10^6 次通话，V = 10^6。实测耗时约 200 ms。
 * 空间复杂度 O(V)，仅需两个百万长度的 IntArray 记录 parent 和 size。
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve0186(): Long {
    val totalUsers = 1_000_000
    val targetSize = 990_000
    val primeMinister = 524287

    val parent = IntArray(totalUsers) { it }
    val size = IntArray(totalUsers) { 1 }

    fun find(i: Int): Int {
        var root = i
        while (root != parent[root]) {
            root = parent[root]
        }
        var curr = i
        while (curr != root) {
            val nxt = parent[curr]
            parent[curr] = root
            curr = nxt
        }
        return root
    }

    fun union(i: Int, j: Int) {
        val rootI = find(i)
        val rootJ = find(j)
        if (rootI != rootJ) {
            if (size[rootI] < size[rootJ]) {
                parent[rootI] = rootJ
                size[rootJ] += size[rootI]
            } else {
                parent[rootJ] = rootI
                size[rootI] += size[rootJ]
            }
        }
    }

    // 环形缓冲区维护 Lagged Fibonacci
    val lfg = IntArray(55)
    for (k in 1..55) {
        val kLong = k.toLong()
        val term = (100003L - 200003L * kLong + 300007L * kLong * kLong * kLong) % totalUsers
        val s = ((term % totalUsers + totalUsers) % totalUsers).toInt()
        lfg[k - 1] = s
    }

    var k = 55
    fun nextS(): Int {
        if (k < 55) {
            return lfg[k++]
        }
        // S_k = S_{k-24} + S_{k-55}
        val idx55 = (k - 55) % 55
        val idx24 = (k - 24) % 55
        val s = (lfg[idx24] + lfg[idx55]) % totalUsers
        lfg[idx55] = s
        k++
        return s
    }

    var ptr = 0
    fun getS(): Int {
        return if (ptr < 55) lfg[ptr++] else nextS()
    }

    var successfulCalls = 0L
    while (true) {
        val u = getS()
        val v = getS()
        if (u == v) continue
        successfulCalls++
        union(u, v)
        if (size[find(primeMinister)] >= targetSize) {
            return successfulCalls
        }
    }
}

fun main() {
    println(solve0186())
}
