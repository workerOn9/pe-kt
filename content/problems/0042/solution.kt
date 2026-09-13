/**
 * Project Euler 042 — Coded Triangle Numbers
 *
 * 优化解：读入单词表，把每个单词转成字母值，再用 8n+1 是否为奇完全平方判定三角数。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 注意：需从题目目录（content/problems/0042）运行，以便相对路径找到 words.txt。
 */

import java.io.File

fun wordValue(w: String): Int { return w.sumOf { it - 'A' + 1 } }

fun readWords(path: String): List<String> {
    return File(path).readText().split(',').map { it.trim().trim('"') }.filter { it.isNotEmpty() }
}

fun isTriangleNumber(n: Int): Boolean {
    val d = 1L + 8L * n
    val s = Math.sqrt(d.toDouble()).toLong()
    for (k in (s - 1)..(s + 1)) if (k * k == d && k % 2L == 1L) return true
    return false
}

fun solve(path: String = "words.txt"): Long {
    return readWords(path).count { isTriangleNumber(wordValue(it)) }.toLong()
}

fun main() {
    println(solve())
}
