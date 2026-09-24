#!/usr/bin/env kotlin
// PE 212 — Combined Volume of Cuboids（长方体并集体积）暴力参照
// 思路：不做扫描线优化，把每个 z 截面的长方体并集逐体素栅格化到一张 (x,y) 位图上，
//       用位图基数（cardinality）数出截面面积，再乘 dz 累加。
//       位图规模 10400 x 10400 位 ~= 13.5 MB，只在 N=100（题面锚点 723581599）规模可用。
// 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
// 运行：java -jar brute-force.jar

private const val SPAN = 10_400  // x0 < 10^4 且 dx <= 399，故坐标上界为 10399

fun main() {
    val n = 100
    val s = IntArray(300_001)
    for (k in 1..55) s[k] = ((100003L - 200003L * k + 300007L * k * k * k) % 1_000_000L).toInt()
    for (k in 56..300_000) s[k] = (s[k - 24] + s[k - 55]) % 1_000_000

    val x0 = IntArray(n); val y0 = IntArray(n); val z0 = IntArray(n)
    val dx = IntArray(n); val dy = IntArray(n); val dz = IntArray(n)
    for (i in 0 until n) {
        val b = 6 * (i + 1)
        x0[i] = s[b - 5] % 10_000; y0[i] = s[b - 4] % 10_000; z0[i] = s[b - 3] % 10_000
        dx[i] = 1 + s[b - 2] % 399; dy[i] = 1 + s[b - 1] % 399; dz[i] = 1 + s[b] % 399
    }

    // z 事件按坐标排序；相邻事件之间活跃集合不变，截面只需栅格化一次
    val events = IntArray(2 * n)
    var e = 0
    for (i in 0 until n) {
        events[e++] = (z0[i] shl 17) or (i shl 1)
        events[e++] = ((z0[i] + dz[i]) shl 17) or (i shl 1) or 1
    }
    java.util.Arrays.sort(events)

    val plane = java.util.BitSet(SPAN * SPAN)
    val inside = BooleanArray(n)
    var volume = 0L
    var previousZ = Int.MIN_VALUE
    var index = 0
    while (index < events.size) {
        val z = events[index] ushr 17
        if (previousZ != Int.MIN_VALUE) {
            plane.clear()
            for (i in 0 until n) {
                if (!inside[i]) continue
                for (y in y0[i] until y0[i] + dy[i]) {
                    val base = y * SPAN
                    plane.set(base + x0[i], base + x0[i] + dx[i])
                }
            }
            volume += plane.cardinality().toLong() * (z - previousZ)
        }
        previousZ = z
        while (index < events.size && (events[index] ushr 17) == z) {
            val enc = events[index]
            inside[(enc shr 1) and 0xFFFF] = enc and 1 == 0
            index++
        }
    }
    println(volume)
}
