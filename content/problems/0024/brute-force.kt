/**
 * Project Euler 024 — 暴力解（教学对比用）
 *
 * 回溯法按字典序逐个生成排列，数到第 1,000,000 个为止。
 * 10! = 3,628,800 个排列，生成前 100 万个完全可行。
 */

fun solveBruteForce(target: Long = 1_000_000): String {
    val used = BooleanArray(10)
    val current = IntArray(10)
    var count = 0L
    var answer = ""

    fun dfs(depth: Int): Boolean {
        if (depth == 10) {
            count++
            if (count == target) {
                answer = current.joinToString("")
                return true                   // 找到目标，逐层回溯退出
            }
            return false
        }
        for (d in 0..9) {                     // 按数字升序尝试，保证字典序
            if (!used[d]) {
                used[d] = true
                current[depth] = d
                if (dfs(depth + 1)) return true
                used[d] = false
            }
        }
        return false
    }

    dfs(0)
    return answer
}

fun main() {
    println(solveBruteForce())
}
