/**
 * Project Euler 059 — XOR Decryption（暴力对照）
 *
 * 暴力解：老实枚举全部 26³ = 17576 个密钥，每个都整段解密成字符串，
 * 再用「常见英文单词出现次数」这种朴素的判据挑最优密钥。
 * 优化解则利用「密钥周期性」把三个字符拆成三组独立打分，只评 3 × 26 次。
 * 需从题目目录运行（读取同目录的 cipher.txt）：
 *   kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.io.File

val P059_WORDS = listOf(
    " the ", " and ", " that ", " have ", " for ", " not ", " with ", " you ",
    " this ", " but ", " his ", " from ", " they ", " will ", " one ", " all ",
    " would ", " there ", " their ", " what ", " out ", " about ", " who "
)

fun p059BruteScore(text: String): Double {
    val padded = " " + text + " "
    var score = 0.0
    for (w in P059_WORDS) {
        var idx = padded.indexOf(w)
        while (idx >= 0) { score += 10.0; idx = padded.indexOf(w, idx + 1) }
    }
    for (c in padded) if (c == ' ') score += 0.5
    return score
}

fun solveBruteForce(): Long {
    val bytes = File("cipher.txt").readText().trim().split(",").map { it.trim().toInt() }
    var bestText = ""
    var bestScore = -1.0
    for (k0 in 'a'..'z') for (k1 in 'a'..'z') for (k2 in 'a'..'z') {
        val key = intArrayOf(k0.code, k1.code, k2.code)
        val sb = StringBuilder(bytes.size)
        for (i in bytes.indices) sb.append((bytes[i] xor key[i % 3]).toChar())
        val text = sb.toString()
        val score = p059BruteScore(text)
        if (score > bestScore) { bestScore = score; bestText = text }
    }
    var sum = 0L
    for (c in bestText) sum += c.code
    return sum
}

fun main() {
    println(solveBruteForce())
}
