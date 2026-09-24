#!/usr/bin/env kotlin
// PE 217 — Balanced Numbers（平衡数）
// 思路：k 位数的「前 ⌈k/2⌉ 位」与「后 ⌈k/2⌉ 位」在 k 为奇数时共享中间位 d_m：
//       k = 2m 时前段是高 m 位（首位非零）、后段是低 m 位，约束为两段数字和相等；
//       k = 2m+1 时 d_m 同时落在两段里、左右各贡献一次因而抵消，约束退化为
//       「高 m 位数字和 = 低 m 位数字和」，中间位 d_m ∈ {0..9} 自由（题面 T(1) = 45 即此）。
//       于是对每个数字和 s 先做数字 DP，求长度 m 的数字串（允许/不允许前导零两种）
//       按 s 分组的「个数 cnt(s)」与「数值和 val(s)」，再按 s 配对合成 k 位数的和：
//         k = 2m  ：Σ_s [10^m·valUp(s)·cntLo(s) + valLo(s)·cntUp(s)]
//         k = 2m+1：Σ_s [10^{m+1}·valUp(s)·10·cntLo(s) + 10^m·45·cntUp(s)·cntLo(s) + valLo(s)·10·cntUp(s)]
//       其中 45 = Σ_{d=0..9} d 为中间位取值和，10 为中间位可选个数。
//       所有项只含加/乘，故全程对 3^15 取模即可；m = 23 时 cnt 可达 10^23 量级，
//       原始值装不进 Long，取模同时也是防溢出的必要手段。
// 复杂度：数字和上限 9·23 = 207，单个长度的 DP 为 O(m·207·10)；k = 1..47 合计
//       不到 10^6 次运算，JIT 预热后不足 1 ms。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

private const val MOD = 14_348_907L      // 3^15
private const val MAXSUM = 207           // 9 × 23（k ≤ 47 时半段最长 23 位）

/**
 * 长度 [len] 的数字串按数字和分组的 (个数, 数值和)，两项均 mod 3^15。
 * [lead] = true 表示最高位不能为 0（整个数的最高半段），false 允许前导零。
 */
private fun half(len: Int, lead: Boolean): Pair<LongArray, LongArray> {
    var cnt = LongArray(MAXSUM + 1)
    var sum = LongArray(MAXSUM + 1)
    cnt[0] = 1L
    var weight = 1L
    for (pos in 0 until len) {
        val nc = LongArray(MAXSUM + 1)
        val ns = LongArray(MAXSUM + 1)
        val lo = if (pos == len - 1 && lead) 1 else 0
        for (s in 0..MAXSUM) {
            val c = cnt[s]
            if (c == 0L) continue
            val base = sum[s]
            for (d in lo..9) {
                val t = s + d
                if (t > MAXSUM) continue
                nc[t] = (nc[t] + c) % MOD
                ns[t] = (ns[t] + base + c * d % MOD * weight) % MOD
            }
        }
        cnt = nc
        sum = ns
        weight = weight * 10 % MOD
    }
    return cnt to sum
}

private fun pow10Mod(e: Int): Long {
    var r = 1L
    repeat(e) { r = r * 10 % MOD }
    return r
}

private fun solve217(nMax: Int = 47): Long {
    var total = 0L
    for (k in 1..nMax) {
        val m = k / 2
        val uc = LongArray(MAXSUM + 1)
        val uv = LongArray(MAXSUM + 1)
        if (m == 0) {
            uc[0] = 1L                     // 空串：1 个，数值和 0
        } else {
            val h = half(m, true)
            h.first.copyInto(uc)
            h.second.copyInto(uv)
        }
        val (lc, lv) = half(m, false)
        val odd = k % 2 == 1
        val p10 = pow10Mod(if (odd) m + 1 else m)
        val mid = pow10Mod(m)
        val mc = if (odd) 10L else 1L      // 中间位的可选个数：只有奇数长度才有中间位
        for (s in 0..MAXSUM) {
            if (uc[s] == 0L || lc[s] == 0L) continue
            total = (total + p10 * uv[s] % MOD * mc % MOD * lc[s]) % MOD
            if (odd) total = (total + mid * 45 % MOD * uc[s] % MOD * lc[s]) % MOD
            total = (total + lv[s] * mc % MOD * uc[s]) % MOD
        }
    }
    return total
}

fun main() {
    println("T(1) = ${solve217(1)}（题面锚点 45）")
    println("T(2) = ${solve217(2)}（题面锚点 540）")
    println("T(5) mod 3^15 = ${solve217(5)}（题面 334795890 取模后 ${334795890L % MOD}）")
    repeat(3) { solve217() }               // JIT 预热
    val t0 = System.nanoTime()
    val ans = solve217()
    val ms = (System.nanoTime() - t0) / 1_000_000
    println(ans)
    System.err.println("solve217 wall = $ms ms")
}
