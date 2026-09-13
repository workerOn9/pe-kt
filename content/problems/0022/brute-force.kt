/**
 * Project Euler 022 — 暴力解（教学对比用）
 *
 * 不排序：每个姓名的排位 = 字典序比它小的姓名个数 + 1，O(n²) 次字符串比较。
 * 注意：需从题目目录（content/problems/0022）运行，以便相对路径找到 names.txt。
 */

import java.io.File

fun alphabeticalValueBrute(name: String): Int = name.sumOf { it - 'A' + 1 }

fun solveBruteForce(text: String): Long {
    val names = text.split(',').map { it.trim('"') }
    var total = 0L
    for (i in names.indices) {
        var rank = 1
        for (j in names.indices) {
            if (j != i && names[j] < names[i]) rank++
        }
        total += rank.toLong() * alphabeticalValueBrute(names[i])
    }
    return total
}

fun main() {
    val text = File("names.txt").readText()
    println(solveBruteForce(text))
}
