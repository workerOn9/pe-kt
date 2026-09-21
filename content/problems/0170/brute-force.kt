/**
 * PE 170 — brute force：只在小规模上做「输入拼接全数字 + 乘积拼接全数字」的完整校验，
 * 用于验证位数剪枝结论（d<=2）。这里对 d=1 的乘数枚举所有切分。
 */
fun checkCase(m: Long, parts: List<Long>): String? {
    val prods = parts.map { m * it }
    val s = prods.joinToString("")
    val inp = m.toString() + parts.joinToString("")
    return if (s.length == 10 && s.toSet().size == 10 && inp.length == 10 && inp.toSet().size == 10) s else null
}

fun main() {
    println(checkCase(27, listOf(36508, 149)))
    println(checkCase(6, listOf(1273, 9854)))
}
