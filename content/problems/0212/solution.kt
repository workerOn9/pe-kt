#!/usr/bin/env kotlin
// PE 212 — Combined Volume of Cuboids（长方体并集体积）
// 思路：沿 z 轴做事件扫描 + 二维矩形并集面积。
//       每个长方体在 z0 处加入、在 z0+dz 处移除；相邻 z 事件之间截面不变，
//       体积 = Σ A(z)·Δz。截面 A 用「y 坐标压缩 + 线段树（覆盖计数 / 覆盖长度）」
//       在 O(k log k) 内求出，k 为该时刻活跃长方体数（约 1000）。
//       长方体的连续体积即 dx·dy·dz；题面前 100 个合计 723581599 与此约定一致。
// 复杂度：事件排序 O(N log N)；每个截面 O(k log k)，事件数 O(N)（z 端点 ≤ 10400 个）。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

private const val SHIFT_X = 17

/** 覆盖计数 / 覆盖长度线段树，坐标已压缩为 yLen 的条带长度。 */
private class CoverTree(private val strips: Int, yLen: LongArray) {
    private val cover = IntArray(4 * strips)
    private val covered = LongArray(4 * strips)
    private val yPrefix = LongArray(strips + 1).also { p ->
        for (i in 0 until strips) p[i + 1] = p[i] + yLen[i]
    }

    fun reset() {
        java.util.Arrays.fill(cover, 0)
        java.util.Arrays.fill(covered, 0L)
    }

    /** 区间 [ql, qr) 覆盖计数变化 delta。 */
    fun update(node: Int, lo: Int, hi: Int, ql: Int, qr: Int, delta: Int) {
        if (qr <= lo || hi <= ql) return
        if (ql <= lo && hi <= qr) {
            cover[node] += delta
        } else {
            val mid = (lo + hi) ushr 1
            if (ql < mid) update(node * 2, lo, mid, ql, qr, delta)
            if (mid < qr) update(node * 2 + 1, mid, hi, ql, qr, delta)
        }
        covered[node] = when {
            cover[node] > 0 -> yPrefix[hi] - yPrefix[lo]
            hi - lo == 1 -> 0L
            else -> covered[node * 2] + covered[node * 2 + 1]
        }
    }

    fun total(): Long = covered[1]
}

private fun unionVolume(cuboidCount: Int): Long {
    // Lagged Fibonacci 生成器：k ≤ 55 时 S_k = [100003 - 200003k + 300007k³] mod 10⁶，
    // 其后 S_k = [S_{k-24} + S_{k-55}] mod 10⁶。
    val s = IntArray(300_001)
    for (k in 1..55) s[k] = ((100003L - 200003L * k + 300007L * k * k * k) % 1_000_000L).toInt()
    for (k in 56..300_000) s[k] = (s[k - 24] + s[k - 55]) % 1_000_000

    val n = cuboidCount
    val x0 = IntArray(n); val y0 = IntArray(n); val z0 = IntArray(n)
    val dx = IntArray(n); val dy = IntArray(n); val dz = IntArray(n)
    for (i in 0 until n) {
        val b = 6 * (i + 1)
        x0[i] = s[b - 5] % 10_000
        y0[i] = s[b - 4] % 10_000
        z0[i] = s[b - 3] % 10_000
        dx[i] = 1 + s[b - 2] % 399
        dy[i] = 1 + s[b - 1] % 399
        dz[i] = 1 + s[b] % 399
    }

    // y 方向坐标压缩
    val yEnds = IntArray(2 * n)
    for (i in 0 until n) {
        yEnds[2 * i] = y0[i]
        yEnds[2 * i + 1] = y0[i] + dy[i]
    }
    java.util.Arrays.sort(yEnds)
    val yUnique = IntArray(2 * n)
    var yCount = 0
    for (v in yEnds) if (yCount == 0 || yUnique[yCount - 1] != v) yUnique[yCount++] = v
    val yIndex = HashMap<Int, Int>(yCount * 2)
    for (i in 0 until yCount) yIndex[yUnique[i]] = i
    val strips = yCount - 1
    val yLen = LongArray(strips) { (yUnique[it + 1] - yUnique[it]).toLong() }

    // z 事件编码：z << 17 | 长方体号 << 1 | (0 加入 / 1 移除)
    val events = IntArray(2 * n)
    var e = 0
    for (i in 0 until n) {
        events[e++] = (z0[i] shl 17) or (i shl 1)
        events[e++] = ((z0[i] + dz[i]) shl 17) or (i shl 1) or 1
    }
    java.util.Arrays.sort(events)

    val tree = CoverTree(strips, yLen)
    val active = IntArray(n)
    val activeY1 = IntArray(n)
    val activeY2 = IntArray(n)
    val slabEvents = IntArray(2 * n)
    var activeCount = 0

    var volume = 0L
    var previousZ = Int.MIN_VALUE
    var index = 0
    while (index < events.size) {
        val z = events[index] ushr 17
        if (activeCount > 0 && previousZ != Int.MIN_VALUE) {
            // 截面：对活跃长方体的 x 端点扫描，线段树维护 y 方向覆盖长度
            var cnt = 0
            for (a in 0 until activeCount) {
                val i = active[a]
                slabEvents[cnt++] = (x0[i] shl SHIFT_X) or (a shl 1)
                slabEvents[cnt++] = ((x0[i] + dx[i]) shl SHIFT_X) or (a shl 1) or 1
            }
            java.util.Arrays.sort(slabEvents, 0, cnt)
            tree.reset()
            var area = 0L
            var previousX = Int.MIN_VALUE
            for (j in 0 until cnt) {
                val enc = slabEvents[j]
                val x = enc ushr SHIFT_X
                if (previousX != Int.MIN_VALUE) area += tree.total() * (x - previousX).toLong()
                previousX = x
                val a = (enc shr 1) and 0x7FFF
                val delta = if (enc and 1 == 0) 1 else -1
                tree.update(1, 0, strips, activeY1[a], activeY2[a], delta)
            }
            volume += area * (z - previousZ).toLong()
        }
        previousZ = z
        while (index < events.size && (events[index] ushr 17) == z) {
            val enc = events[index]
            val i = (enc shr 1) and 0xFFFF
            if (enc and 1 == 0) {
                active[activeCount] = i
                activeY1[activeCount] = yIndex[y0[i]]!!
                activeY2[activeCount] = yIndex[y0[i] + dy[i]]!!
                activeCount++
            } else {
                for (a in 0 until activeCount) {
                    if (active[a] == i) {
                        activeCount--
                        active[a] = active[activeCount]
                        activeY1[a] = activeY1[activeCount]
                        activeY2[a] = activeY2[activeCount]
                        break
                    }
                }
            }
            index++
        }
    }
    return volume
}

private fun solve212(): Long = unionVolume(50_000)

fun main() {
    println(solve212())
}
