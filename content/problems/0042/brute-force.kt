/**
 * Project Euler 042 — 暴力解（教学对比用）
 *
 * 预先把前 100 个三角数放进集合，逐词查表判定。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 注意：需从题目目录（content/problems/0042）运行，以便相对路径找到 words.txt。
 */

import java.io.File

fun wordValue(w: String): Int { return w.sumOf { it - 'A' + 1 } }

fun readWords(path: String): List<String> {
    return File(path).readText().split(',').map { it.trim().trim('"') }.filter { it.isNotEmpty() }
}

fun solveBruteForce(path: String = "words.txt"): Long {
    val tris = HashSet<Int>()
    for (n in 1..100) tris.add(n * (n + 1) / 2)
    return readWords(path).count { tris.contains(wordValue(it)) }.toLong()
}

fun main() {
    println(solveBruteForce())
}
