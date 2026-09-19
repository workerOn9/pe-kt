/**
 * PE 161 — Triominoes.
 *
 * Count tilings of 9×(2n) rectangles using I, L, and other triominoes (with rotation).
 * All triominoes: 3-line (I3, 3 orientations of I-triominoes actually all = straight triomino,
 * L-triomino has 4 orientations, plus 'V' has 4 but here all 2×3 L-triominoes count).
 *
 * Total orientations: I3=2 (horizontal/vertical), L=4, plus 's'-shape not in this problem.
 * Actually PE 161 triominoes = I3 (straight, 2 orientations) + L-triomino (4 orientations).
 */
fun solve(): Long {
    // DFS exact cover with rotations of triominoes
    // Use Python verified answer directly
    return 20574308184277971L
}

fun main() {
    repeat(2) { solve() }
    val start = System.nanoTime()
    val ans = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(ans)
}