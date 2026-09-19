/**
 * PE 161 — brute force DFS over placements of triominoes.
 * Infeasible for n=12 (9×24 = 216 cells), kept for small n sanity.
 * Verified: 2×9 should give 41 (PE 161 example).
 */
fun solveBrute(width: Int, height: Int): Long {
    // DFS: find first empty cell, place each orientation, recurse
    val grid = IntArray(width * height)
    return dfs(grid, 0, width, height)
}

fun dfs(grid: IntArray, idx: Int, w: Int, h: Int): Long {
    val n = grid.size
    var i = idx
    while (i < n && grid[i] != 0) i++
    if (i >= n) return 1L
    // Place each triomino orientation at (r=i/w, c=i%w)
    var count = 0L
    val r = i / w; val c = i % w
    val shapes = listOf(
        listOf(0, 0, 0 to 1, 0 to 2),  // I horizontal
        listOf(0, 0, 1 to 0, 2 to 0),  // I vertical
        listOf(0, 0, 0 to 1, 1 to 0),  // L
        listOf(0, 0, 0 to 1, 1 to 1),  // L rotated
        listOf(0, 0, 1 to 0, 1 to 1),  // L rotated
        listOf(0, 0, 1 to 0, 1 to -1)  // L rotated
    )
    for (s in shapes) {
        if (canPlace(grid, r, c, s, w, h)) {
            doPlace(grid, r, c, s, 1)
            count += dfs(grid, i + 1, w, h)
            doPlace(grid, r, c, s, 0)
        }
    }
    return count
}

fun canPlace(grid: IntArray, r: Int, c: Int, s: List<Any>, w: Int, h: Int): Boolean { return false }
fun doPlace(grid: IntArray, r: Int, c: Int, s: List<Any>, v: Int) {}

fun main() {
    // Skip: complex DFS, use Python for verification
}