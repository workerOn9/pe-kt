/**
 * PE 162 — brute force check (too slow for full range).
 * Only valid for sanity check on small max.
 */
fun bruteForce(maxLen: Int = 6): Long {
    val digits = listOf("0", "1", "A")
    var count = 0L
    for (len in 1..maxLen) {
        for (numStr in generateHexStrings(len)) {
            if ("0" in numStr && "1" in numStr && "A" in numStr) count++
        }
    }
    return count
}

fun generateHexStrings(len: Int): Sequence<String> {
    return sequence {
        if (len == 0) { yield(""); return@sequence }
        // First digit: 1-F (15 choices)
        for (first in "123456789ABCDEF") {
            yieldAll(generateRemainder(len - 1).map { first + it })
        }
    }
}

fun generateRemainder(len: Int): Sequence<String> {
    return sequence {
        if (len == 0) { yield(""); return@sequence }
        for (ch in "0123456789ABCDEF") {
            for (rest in generateRemainder(len - 1)) {
                yield(ch + rest)
            }
        }
    }
}