/**
 * Project Euler 022 — Names Scores
 *
 * 优化解：读入 names.txt（CSV、引号包裹），库排序（TimSort，O(n log n)），
 * 逐个计算字母值 × 排位并累加。
 * 注意：需从题目目录（content/problems/0022）运行，以便相对路径找到 names.txt。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.io.File

/** 姓名的字母值：A=1, B=2, ..., Z=26 之和 */
fun alphabeticalValue(name: String): Int = name.sumOf { it - 'A' + 1 }

/** 对姓名文本排序后求总分数（纯计算部分，与 IO 分离，便于基准测试） */
fun solveScores(text: String): Long {
    val names = text.split(',').map { it.trim('"') }.sorted()
    var total = 0L
    names.forEachIndexed { i, name ->
        total += (i + 1).toLong() * alphabeticalValue(name)
    }
    return total
}

fun main() {
    val text = File("names.txt").readText()
    println(solveScores(text))
}
