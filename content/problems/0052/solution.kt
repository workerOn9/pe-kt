/**
 * Project Euler 052 — Permuted Multiples
 *
 * 优化解：
 *  1. 「数字相同」等价于「0–9 各数字出现次数相同」：把十个计数各占 4 bit 压进一个 Long，
 *     逐位累加 1L shl (digit*4) 即得指纹，省掉字符串排序与拼接；
 *  2. 由 x 与 2x 的数位和同余（mod 9）得 x ≡ 2x (mod 9)，即 9 | x，
 *     于是只需检查 9 的倍数，搜索量直接除以 9。
 *
 * 复杂度：O(X/9 · 6 · D)，X 为答案、D 为位数。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun digitSignature(n: Int): Long {
    var sig = 0L
    var t = n
    while (t > 0) {
        sig += 1L shl ((t % 10) * 4)
        t /= 10
    }
    return sig
}

fun solve(): Long {
    var x = 9
    while (true) {
        val sig = digitSignature(x)
        var ok = true
        for (k in 2..6) {
            if (digitSignature(k * x) != sig) { ok = false; break }
        }
        if (ok) return x.toLong()
        x += 9
    }
}

fun main() {
    println(solve())
}
