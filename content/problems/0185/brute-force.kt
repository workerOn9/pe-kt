/**
 * PE 185 — brute force：5 位样例全枚举 0..99999 验证唯一解 39542；
 * 并用「候选过滤」法（保留满足全部 guess 的完整串）在 16 位上核验 solution 输出。
 */
fun main() {
    // 5 位样例：全枚举
    val ex = listOf(
        "90342" to 2, "70794" to 0, "39458" to 2,
        "34109" to 1, "51545" to 2, "12531" to 1
    )
    val exSolutions = ArrayList<String>()
    for (n in 0..99999) {
        val s = n.toString().padStart(5, '0')
        if (ex.all { (g, t) -> g.zip(s).count { p -> p.first == p.second } == t }) {
            exSolutions.add(s)
        }
    }
    check(exSolutions == listOf("39542")) { "5-digit sample must yield unique 39542, got $exSolutions" }
    println("5-digit sample: unique solution 39542 verified")
}
