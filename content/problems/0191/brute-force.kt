/**
 * Project Euler 191 — Prize Strings (暴力解：全枚举 + 规则校验)
 *
 * 枚举全部 3^n 个 n 天 trinary 字符串，逐个检查是否违反规则：
 *  - 不含 "AAA" 子串（无连续三天缺席）
 *  - 不含两个以上 'A'（迟到标记最多 1 次）
 *
 * kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    val n = 30
    var count = 0L
    val buf = CharArray(n)
    val chars = charArrayOf('L', 'O', 'A')
    fun check(): Boolean {
        var absent = 0; var consecutive = 0
        for (ch in buf) {
            if (ch == 'A') { absent++; consecutive++ } else { consecutive = 0 }
            if (absent > 1 || consecutive > 2) return false
        }
        return true
    }
    fun dfs(pos: Int) {
        if (pos == n) { if (check()) count++; return }
        for (c in chars) { buf[pos] = c; dfs(pos + 1) }
    }
    dfs(0); return count
}

fun main() { println(solveBruteForce()) }
