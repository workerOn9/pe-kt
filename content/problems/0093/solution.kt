/**
 * Project Euler 093 — Arithmetic Expressions
 *
 * 优化解：用 0..9 中四个不同数字的每种组合（C(10,4)=210 组）穷举全部表达式。
 * 表达式的结构性描述是「把四个数两两合并，直到只剩一个」，因此直接对数值多重集做递归：
 * 每次任取两个数、用 + − × ÷ 之一合并（减法与除法两个方向都取），再对剩下的三数递归。
 * 这种写法天然覆盖所有二叉树结构与所有括号方式，且不依赖数字顺序，省掉 4! 种排列的重复。
 * 全程用约分后的分数精确运算，避免浮点误差把 4/3−(1/3) 这类结果判丢。
 * 取每个数字集合能得到的最长的 1..n 连续正整数段，记录最长者。
 *
 * 复杂度：时间 O(C(10,4) · 6^3) 级别的表达式枚举，空间 O(1)（每组重开集合）
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private data class Frac(val n: Long, val d: Long) {
    companion object {
        fun of(num: Long, den: Long): Frac {
            var a = num
            var b = den
            if (b < 0) {
                a = -a
                b = -b
            }
            val g = gcdLong(a, b)
            return Frac(a / g, b / g)
        }
    }
}

private fun gcdLong(a: Long, b: Long): Long {
    var x = if (a < 0) -a else a
    var y = if (b < 0) -b else b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return if (x == 0L) 1L else x
}

private fun allValues(list: MutableList<Frac>, out: MutableSet<Frac>) {
    if (list.size == 1) {
        out.add(list[0])
        return
    }
    for (i in list.indices) {
        for (j in i + 1 until list.size) {
            val a = list[i]
            val b = list[j]
            val rest = ArrayList<Frac>(list.size - 2)
            for (k in list.indices) if (k != i && k != j) rest.add(list[k])
            val cands = LinkedHashSet<Frac>()
            cands.add(Frac.of(a.n * b.d + b.n * a.d, a.d * b.d))
            cands.add(Frac.of(a.n * b.d - b.n * a.d, a.d * b.d))
            cands.add(Frac.of(b.n * a.d - a.n * b.d, a.d * b.d))
            cands.add(Frac.of(a.n * b.n, a.d * b.d))
            if (b.n != 0L) cands.add(Frac.of(a.n * b.d, a.d * b.n))
            if (a.n != 0L) cands.add(Frac.of(b.n * a.d, b.d * a.n))
            for (c in cands) {
                rest.add(c)
                allValues(rest, out)
                rest.removeAt(rest.size - 1)
            }
        }
    }
}

fun solve(): Long {
    var bestLen = -1
    var bestCode = 0L
    for (a in 0..9) {
        for (b in a + 1..9) {
            for (c in b + 1..9) {
                for (d in c + 1..9) {
                    val start = mutableListOf(
                        Frac.of(a.toLong(), 1L), Frac.of(b.toLong(), 1L),
                        Frac.of(c.toLong(), 1L), Frac.of(d.toLong(), 1L),
                    )
                    val values = HashSet<Frac>()
                    allValues(start, values)
                    val ints = HashSet<Long>()
                    for (f in values) if (f.d == 1L && f.n > 0L) ints.add(f.n)
                    var k = 1L
                    while (ints.contains(k)) k++
                    val len = (k - 1L).toInt()
                    if (len > bestLen) {
                        bestLen = len
                        bestCode = a * 1000L + b * 100L + c * 10L + d
                    }
                }
            }
        }
    }
    return bestCode
}

fun main() {
    println(solve())
}
