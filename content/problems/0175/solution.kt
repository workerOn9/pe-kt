/**
 * PE 175 — 缩短二进制展开（SBE）。
 *
 * 原理：f 是超二进制计数，f(n)=s(n+1)（Stern 二原子序列），
 * 而 s(n)/s(n+1) 正是 Calkin–Wilf 树第 n 个节点，n 的二进制（去掉开头的 1）
 * 给出从根 1/1 到该节点的路径（0=左子 a/(a+b)，1=右子 (a+b)/b）。
 * 又 f(n)/f(n-1) = 1 / (s(n)/s(n+1))，所以目标有理数为 q/p 时：
 *   对 q/p 在 Calkin–Wilf 树上逆向走回 1/1（连续同向步用整除一次跳完），
 *   反转得到根->叶的比特串，接在开头的 1 之后，再做游程编码即为 SBE。
 *
 * 该实现与暴力枚举（对每个 n 算 f(n)/f(n-1)，取各分数的最小 n）在
 * 20 万个有理数上逐一对拍一致；题面例子 13/17 -> 4,3,1 也吻合。
 *
 * 本题 SBE = 1,13717420,8（段长和 13717429 位）。
 * 注意：仓库 meta.answer 字段为 Long，无法直接存逗号串，
 * 故此处按「各段十进制数字顺次拼接」编码为 1137174208 返回（analysis.md 有说明）。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

/** 返回 SBE 的段长列表 */
fun shortenedBinaryExpansion(p0: Long, q0: Long): List<Long> {
    val g = gcd(p0, q0)
    var p = p0 / g
    var q = q0 / g
    var a = q          // Calkin–Wilf 树上要定位的值 a/b = q/p
    var b = p
    val moves = ArrayList<Pair<Int, Long>>()   // (比特, 连续步数)，从叶往根
    while (!(a == 1L && b == 1L)) {
        if (a < b) {
            val k = (b - 1) / a
            b -= k * a
            moves.add(0 to k)
        } else {
            val k = (a - 1) / b
            a -= k * b
            moves.add(1 to k)
        }
    }
    moves.reverse()                            // 根 -> 叶
    val runs = ArrayList<Long>()
    if (moves.isNotEmpty() && moves[0].first == 1) {
        runs.add(moves[0].second + 1)          // 二进制首位固定的那个 1 并入第一段
        for (i in 1 until moves.size) runs.add(moves[i].second)
    } else {
        runs.add(1L)
        for (mv in moves) runs.add(mv.second)
    }
    return runs
}

fun solve(): Long =
    shortenedBinaryExpansion(123456789L, 987654321L).joinToString("").toLong()

fun main() {
    val sbe = shortenedBinaryExpansion(123456789L, 987654321L)
    println("SBE = " + sbe.joinToString(","))
    println("编码值 = " + solve())
}
