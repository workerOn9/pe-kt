package dev.pekt.visualize

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 可视化协议 v1 的数据模型（docs/06-visualization-protocol.md）。
 *
 * Scene / Update 的可选字段按 kind 裁剪：grid1d 用 columns+cells，
 * grid2d 用 rows+triangle+cells（Cell 带 row/col），sequence 用 start+xLabel+yLabel。
 * 序列化使用 explicitNulls = false，未用到的字段不出现在 JSON 里。
 */
val VisualizationJson = Json {
    explicitNulls = false
    encodeDefaults = true // version=1、Cell.state="idle" 等默认字段也必须输出
}

@Serializable
data class Visualization(
    val version: Int = 1,
    val problemId: Int,
    val title: String,
    val kind: String,
    val scene: Scene,
    val steps: List<Step>,
) {
    companion object {
        /** 协议约定：步骤总数上限（演示实例约定，见 06-visualization-protocol.md）。 */
        const val MAX_STEPS = 300
    }
}

@Serializable
data class Scene(
    val columns: Int? = null, // grid1d
    val rows: Int? = null, // grid2d
    val triangle: Boolean? = null, // grid2d
    val cells: List<Cell>? = null, // grid1d / grid2d
    val start: Long? = null, // sequence
    val xLabel: String? = null, // sequence
    val yLabel: String? = null, // sequence
)

@Serializable
data class Cell(
    val index: Int,
    val value: Long,
    val row: Int? = null, // grid2d
    val col: Int? = null, // grid2d
    val state: String = State.IDLE,
)

@Serializable
data class Step(
    val i: Int,
    val updates: List<Update>,
    val caption: String? = null,
    val codeLine: Int? = null,
)

@Serializable
data class Update(
    val state: String,
    val index: Int? = null, // grid1d / grid2d：单元格序号
    val append: Long? = null, // sequence：追加的数值
    val label: String? = null, // 覆盖显示文本
)

/** 状态枚举（v1）：idle / current / eliminated / prime / onPath / considered / done。 */
object State {
    const val IDLE = "idle"
    const val CURRENT = "current"
    const val ELIMINATED = "eliminated"
    const val PRIME = "prime"
    const val ON_PATH = "onPath"
    const val CONSIDERED = "considered"
    const val DONE = "done"
}
