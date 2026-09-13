/**
 * Project Euler 098 — Anagramic Squares
 *
 * 思路：把单词按「字母排序」分组得到异位词族；同族两个词能同时被同一个字母→数字双射
 * 映成平方数。双射保持「模式」——相同字符在各位置上的重复结构。于是先把每个平方数的
 * 模式编码成一个 Long（每个位置 4 bit），候选过滤退化成一次整数比较；只有模式一致的
 * 平方数才真正逐位建立映射，再把第二个词变换过去，用二分查找判断结果是否也是同长度的
 * 平方数。平方数按位数直接由整数开方定界生成，无需哈希表或装箱。
 * 需从题目目录运行（读取同目录 words.txt）。
 * 复杂度：预处理 O(S·L)（S 为候选平方数总数）；每对词 O(S_L) 次整数比较 + O(log S_L) 次查找。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private fun p098Isqrt(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return r
}

/** 十进制串的模式编码：每个位置 4 bit，取该字符首次出现的下标。 */
private fun p098Code(s: String): Long {
    val seen = IntArray(10)
    var next = 0
    var code = 0L
    for (ch in s) {
        val d = ch - '0'
        var idx = seen[d]
        if (idx == 0) {
            next++
            idx = next
            seen[d] = idx
        }
        code = (code shl 4) or (idx - 1).toLong()
    }
    return code
}

/** 大写单词的模式编码，与 [p098Code] 使用同一套「首次出现下标」规则。 */
private fun p098CodeWord(s: String): Long {
    val seen = IntArray(26)
    var next = 0
    var code = 0L
    for (ch in s) {
        val d = ch - 'A'
        var idx = seen[d]
        if (idx == 0) {
            next++
            idx = next
            seen[d] = idx
        }
        code = (code shl 4) or (idx - 1).toLong()
    }
    return code
}

private fun p098Contains(a: LongArray, v: Long): Boolean {
    var lo = 0
    var hi = a.size - 1
    while (lo <= hi) {
        val mid = (lo + hi) ushr 1
        val x = a[mid]
        when {
            x < v -> lo = mid + 1
            x > v -> hi = mid - 1
            else -> return true
        }
    }
    return false
}

private fun p098Words(path: String): List<String> =
    java.io.File(path).readText().split(',').map { it.trim().trim('"') }.filter { it.isNotEmpty() }

fun solve(path: String = "words.txt"): Long {
    val words = p098Words(path)

    val groups = LinkedHashMap<String, MutableList<String>>()
    for (w in words) groups.getOrPut(w.toCharArray().sorted().joinToString("")) { ArrayList() }.add(w)

    val wanted = sortedSetOf<Int>()
    for (g in groups.values) if (g.size > 1) for (w in g) wanted.add(w.length)
    if (wanted.isEmpty()) return 0L

    val squaresByLen = HashMap<Int, LongArray>()
    val codesByLen = HashMap<Int, LongArray>()
    for (len in wanted) {
        var low = 1L
        repeat(len - 1) { low *= 10 }
        val firstRoot = p098Isqrt(low)
        val n0 = if (firstRoot * firstRoot < low) firstRoot + 1 else firstRoot
        val n1 = p098Isqrt(low * 10 - 1)
        val count = (n1 - n0 + 1).toInt()
        val squares = LongArray(count)
        val codes = LongArray(count)
        for (k in 0 until count) {
            val v = (n0 + k) * (n0 + k)
            squares[k] = v
            codes[k] = p098Code(v.toString())
        }
        squaresByLen[len] = squares
        codesByLen[len] = codes
    }

    var best = 0L
    for (g in groups.values) {
        if (g.size < 2) continue
        for (i in g.indices) for (j in i + 1 until g.size) {
            val w1 = g[i]
            val w2 = g[j]
            if (w1 == w2) continue
            val squares = squaresByLen[w1.length] ?: continue
            val codes = codesByLen.getValue(w1.length)
            val target = p098CodeWord(w1)
            for (k in squares.indices) {
                if (codes[k] != target) continue
                val s1 = squares[k].toString()
                val map = HashMap<Char, Char>()
                for (p in w1.indices) map[w1[p]] = s1[p]
                val t = StringBuilder(w2.length)
                for (ch in w2) t.append(map[ch] ?: ' ')
                if (t[0] == '0') continue
                val v = t.toString().toLongOrNull() ?: continue
                if (!p098Contains(squares, v)) continue
                if (squares[k] > best) best = squares[k]
                if (v > best) best = v
            }
        }
    }
    return best
}

fun main() {
    println(solve())
}
