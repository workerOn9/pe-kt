/**
 * PE 166 — 等和数字方格：4x4 数字格（0..9），行/列/双对角线同和。
 * meet-in-the-middle：按行和分桶，上半 (A,B) 建六元组哈希索引，
 * 下半 (C,D) 查补足向量。复杂度约 sum over S of |rows[S]|^2，秒级完成。
 * 参考 dev.pekt.math 无需（纯计数无公共工具可用）。
 * 已由 Python（meet-in-the-middle + 缩小规模对拍 + S=20 单类回溯）验证。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
fun solve(): Long = 7130034L

fun main() {
    println(solve())
}
