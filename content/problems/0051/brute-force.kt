/**
 * Project Euler 051 — Prime Digit Replacements（暴力解，教学对比用）
 *
 * 从 2 开始升序扫描素数；对每个素数枚举全部位置掩码，现场把掩码位的数字
 * 依次换成 0…9，用试除法判定生成的数是否为素数，统计家族里的素数个数。
 * 不做 k ≡ 0 (mod 3) 剪枝、不用筛法、不按长度分组，直接返回第一个合格素数。
 *
 * 复杂度：O(π(n) · 2^d · 10 · √n)（d 为 n 的位数），本机实测秒级。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun isPrimeByTrial(n: Int): Boolean {
    if (n < 2) return false
    if (n % 2 == 0) return n == 2
    var d = 3
    while (d.toLong() * d <= n) {
        if (n % d == 0) return false
        d += 2
    }
    return true
}

fun digitCountOf(n: Int): Int {
    var d = 1
    var t = n
    while (t >= 10) { t /= 10; d++ }
    return d
}

/** 掩码位上的数字是否全都相同——只有相同才算「替换同一数字」得到的家族成员。 */
fun maskIsUniform(n: Int, mask: Int): Boolean {
    var t = n
    var m = mask
    var seen = -1
    while (t > 0) {
        val d = t % 10
        if (m and 1 == 1) {
            if (seen < 0) seen = d else if (seen != d) return false
        }
        m = m shr 1
        t /= 10
    }
    return true
}

/** 把掩码位的数字统一换成 digit。 */
fun withReplacedDigits(n: Int, mask: Int, digit: Int): Int {
    var out = 0
    var pow = 1
    var t = n
    var m = mask
    while (t > 0) {
        val d = t % 10
        out += (if (m and 1 == 1) digit else d) * pow
        m = m shr 1
        pow *= 10
        t /= 10
    }
    return out
}

fun solveBruteForce(): Long {
    var n = 2
    while (true) {
        if (isPrimeByTrial(n)) {
            val digits = digitCountOf(n)
            for (mask in 1 until (1 shl digits)) {
                if (!maskIsUniform(n, mask)) continue
                // 最高位被替换时不能取 0（否则位数变少）
                val from = if ((mask shr (digits - 1)) and 1 == 1) 1 else 0
                var primeCount = 0
                for (digit in from..9) {
                    if (isPrimeByTrial(withReplacedDigits(n, mask, digit))) primeCount++
                }
                if (primeCount >= 8) return n.toLong()
            }
        }
        n++
    }
}

fun main() {
    println(solveBruteForce())
}
