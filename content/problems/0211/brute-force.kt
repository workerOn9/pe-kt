#!/usr/bin/env kotlin
// PE 211 暴力参照：对每个 n 枚举到 sqrt(n) 的因子，并独立累加因子平方。
// 仅用于小范围交叉验证；正式规模需约 N^(3/2) 的试除量。
// 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar

fun main() {
    val limit = 20_000
    var sum = 0L
    for (n in 1..limit) {
        var sigma2 = 0L
        var d = 1
        while (d.toLong() * d <= n) {
            val other = n / d
            sigma2 += d.toLong() * d
            if (other != d) sigma2 += other.toLong() * other
            d++
        }
        val root = Math.sqrt(sigma2.toDouble()).toLong()
        if (root * root == sigma2) sum += n
    }
    println(sum)
}
