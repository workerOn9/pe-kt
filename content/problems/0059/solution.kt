/**
 * Project Euler 059 — XOR Decryption
 *
 * 优化解：密钥只有三个小写字母，共 26³ = 17576 种。把密文按位置模 3 分成三组，
 * 每组各自用 128 项英文频率表给候选字节打分（空格权重最高），三组独立取最优，
 * 把 26³ 的全量组合搜索降成 3 × 26 次打分；最后用最优密钥解密并求和。
 * 中间结果用 IntArray 存字节，避免 String 拼接带来的分配开销。
 * 需从题目目录运行（读取同目录的 cipher.txt）：
 *   kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.io.File

val P059_FREQ: DoubleArray = DoubleArray(128) { -1000.0 }.also { f ->
    val letters = "etaoinshrdlcumwfgypbvkjxqz"
    val weights = doubleArrayOf(
        12.7, 9.1, 8.2, 7.5, 7.0, 6.7, 6.3, 6.1, 6.0, 4.3, 4.0, 2.8, 2.8,
        2.4, 2.4, 2.2, 2.0, 2.0, 1.9, 1.5, 1.0, 0.8, 0.15, 0.15, 0.1, 0.07
    )
    for (i in letters.indices) f[letters[i].code] = weights[i]
    f[' '.code] = 15.0
    f['\n'.code] = 0.0
    for (c in ",.;:!?'\"-()") f[c.code] = 0.5
    for (c in 32..126) if (c < 128) f[c] = maxOf(f[c], 0.0)
}

fun p059LoadCipher(path: String): IntArray =
    File(path).readText().trim().split(",").map { it.trim().toInt() }.toIntArray()

fun p059Score(bytes: IntArray): Double {
    var s = 0.0
    for (b in bytes) s += if (b in 0..127) P059_FREQ[b] else -1000.0
    return s
}

/** 按位置模 3 分组，每组独立挑最优密钥字符 */
fun p059BestKey(bytes: IntArray): String {
    val sb = StringBuilder()
    for (r in 0 until 3) {
        var bestScore = -1e18
        var bestCh = 'a'
        for (c in 'a'..'z') {
            var s = 0.0
            var i = r
            while (i < bytes.size) { s += P059_FREQ[bytes[i] xor c.code]; i += 3 }
            if (s > bestScore) { bestScore = s; bestCh = c }
        }
        sb.append(bestCh)
    }
    return sb.toString()
}

fun p059Decrypt(key: String, bytes: IntArray): String {
    val out = CharArray(bytes.size)
    for (i in bytes.indices) out[i] = (bytes[i] xor key[i % 3].code).toChar()
    return String(out)
}

fun solve(path: String = "cipher.txt"): Long {
    val bytes = p059LoadCipher(path)
    val text = p059Decrypt(p059BestKey(bytes), bytes)
    var sum = 0L
    for (c in text) sum += c.code
    return sum
}

fun main() {
    println(solve())
}
