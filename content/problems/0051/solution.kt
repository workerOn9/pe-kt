/**
 * Project Euler 051 — Prime Digit Replacements
 *
 * 优化解：升序扫描素数，对每个素数枚举「被替换的位置集合」mask，数出家族里有多少素数，
 * 第一个凑齐 8 个素数的素数即为答案。两处关键剪枝：
 *   1. 被替换位数 k 必须满足 k ≡ 0 (mod 3)：家族成员的数位和相差 d·k（d 为替换数字），
 *      若 3∤k，则 d 取 0…9 时有 4 个、最高位被替换（d 只能取 1…9）时有 3 个被 3 整除，
 *      剩下的候选不足 8 个；
 *   2. 被替换的数位在原数中必须全相同（原数本身就是家族的一员），否则该 mask 与它无关。
 * 素性判定用 10^6 的埃氏筛做 O(1) 查表，替代暴力解的试除法。
 *
 * 复杂度：筛法 O(10^6 log log 10^6)；扫描 O(π(N)·C(d,3)·10)，d = 6 时每个素数只有 20 个 mask。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

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

fun digitCount(n: Int): Int {
    var d = 1
    var t = n
    while (t >= 10) { t /= 10; d++ }
    return d
}

/** 家族基元：掩码位清零；掩码位数字不全相同则返回 −1（该素数不属于此家族）。 */
fun familyBase(n: Int, mask: Int): Int {
    var base = 0
    var pow = 1
    var t = n
    var m = mask
    var seen = -1
    var uniform = true
    while (t > 0) {
        val d = t % 10
        if (m and 1 == 1) {
            if (seen < 0) seen = d else if (seen != d) uniform = false
        } else {
            base += d * pow
        }
        m = m shr 1
        pow *= 10
        t /= 10
    }
    return if (uniform) base else -1
}

/** 掩码位的权重和 Σ10^pos：把基元加上 d·weight 就等于把掩码位全换成数字 d。 */
fun maskWeight(mask: Int): Int {
    var weight = 0
    var pow = 1
    var m = mask
    while (m > 0) {
        if (m and 1 == 1) weight += pow
        pow *= 10
        m = m shr 1
    }
    return weight
}

fun solve(): Long {
    val limit = 999_999
    val isP = sieveBool(limit)
    for (n in 2..limit) {
        if (!isP[n]) continue
        val digits = digitCount(n)
        val leadingBit = 1 shl (digits - 1)
        for (mask in 1 until (1 shl digits)) {
            val k = Integer.bitCount(mask)
            if (k % 3 != 0 || k == digits) continue
            val base = familyBase(n, mask)
            if (base < 0) continue
            val weight = maskWeight(mask)
            // 最高位被替换时替换数字不能取 0，否则位数缩水
            val from = if (mask and leadingBit != 0) 1 else 0
            var primeCount = 0
            for (d in from..9) if (isP[base + d * weight]) primeCount++
            if (primeCount >= 8) return n.toLong()
        }
    }
    return 0L
}

fun main() {
    println(solve())
}
