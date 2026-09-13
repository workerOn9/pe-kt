/**
 * Project Euler 039 — 暴力解（教学对比用）
 *
 * 对每个周长 p ≤1000，暴力枚举两条直角边 a ≤ b ≤ c 并验证 a²+b²=c²。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    var best = 0; var bestP = 0
    for (p in 12..1000) {
        var c = 0
        for (a in 1..p / 3) for (b in a..(p - a) / 2) {
            val cc = p - a - b
            if (cc < b) break
            if (a * a + b * b == cc * cc) c++
        }
        if (c > best) { best = c; bestP = p }
    }
    return bestP.toLong()
}

fun main() {
    println(solveBruteForce())
}
