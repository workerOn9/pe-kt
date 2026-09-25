#!/usr/bin/env kotlin
// PE 222 — Sphere Packing（球体装管）暴力对照版
// 思路：只做「随机排列 + 单次 2-opt/or-opt 下降」，不重启、不交叉校验。
//       目标函数与优化版完全一致（同样含端点球半径），纯粹为了对照耗时。
//       优化版靠 200 次随机重启跳出局部极小；本版只有一次机会，
//       在 21 个球上会停在比最优解更长的排法上（长度会打印出来对照）。
// 复杂度：单次下降 O(轮数 · n^3)，n = 21。
// 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
// 运行：java -jar brute.jar

import kotlin.math.sqrt
import kotlin.random.Random

private val RADII = (30..50).map { it.toDouble() }

private fun dz(r1: Double, r2: Double): Double = sqrt(200.0 * (r1 + r2 - 50.0))

private fun length(order: List<Double>): Double =
    order.first() + order.dropLast(1).zip(order.drop(1)).sumOf { (x, y) -> dz(x, y) } + order.last()

private fun improve(order: List<Double>): List<Double> {
    var cur = order
    var moved = true
    while (moved) {
        moved = false
        for (i in 0 until cur.size - 1) {
            for (j in i + 2 until cur.size) {
                val cand = cur.subList(0, i + 1) + cur.subList(i + 1, j + 1).reversed() + cur.subList(j + 1, cur.size)
                if (length(cand) < length(cur) - 1e-12) { cur = cand; moved = true }
            }
        }
        for (i in cur.indices) {
            for (j in cur.indices) {
                if (i == j || j == i + 1) continue
                val rest = cur.toMutableList()
                rest.add(j, rest.removeAt(i))
                if (length(rest) < length(cur) - 1e-12) { cur = rest; moved = true }
            }
        }
    }
    return cur
}

fun main() {
    val rnd = Random(1)
    val opt = improve(RADII.shuffled(rnd))
    println("order = ${opt.map { it.toInt() }}")
    println("length = %.6f mm".format(length(opt)))
    println("answer = ${Math.round(length(opt) * 1000.0)}")
}
