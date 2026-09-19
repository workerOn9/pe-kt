/**
 * PE 162 — Hexadecimal numbers containing 0, 1, and A.
 *
 * Count hex positive integers with at most 16 digits that contain
 * at least one 0, at least one 1, and at least one A (hex digits).
 *
 * Inclusion-exclusion: |U| - |missing 0| - |missing 1| - |missing A|
 *   + |missing 0,1| + |missing 0,A| + |missing 1,A| - |missing 0,1,A|
 */
fun solve(): Long {
    fun countWithoutMissing(missingDigits: List<String>, length: Int): Long {
        val forbidden = missingDigits.toSet()
        val allAllowed = (0..15).filter { d -> d.toString(16).uppercase() !in forbidden }
        val firstAllowed = (1..15).filter { d -> d.toString(16).uppercase() !in forbidden }
        return firstAllowed.size.toLong() * (allAllowed.size.toLong().pow(length - 1))
    }

    val total = (1..16).sumOf { k -> 15L * 16L.pow(k - 1) }
    val digits = listOf("0", "1", "A")

    var ans = total
    for (mask in 1 until (1 shl digits.size)) {
        val missing = digits.filterIndexed { i, _ -> mask and (1 shl i) != 0 }
        val cnt = (1..16).sumOf { k -> countWithoutMissing(missing, k) }
        ans += if (mask.countOneBits() % 2 == 1) -cnt else cnt
    }
    return ans
}

fun main() {
    println(solve())
}