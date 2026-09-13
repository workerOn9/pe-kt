# 可视化协议（Visualization Protocol v1）

> 决策 Q-2 的 spike 结论。原则（架构约束 #3）：**可视化是数据驱动**——前端只做渲染，分步数据由后端 Kotlin 代码真实执行生成，"看到的 = 真实执行的"。

## 顶层结构

```json
{
  "version": 1,
  "problemId": 7,
  "title": "埃氏筛法（演示实例：筛到 100）",
  "kind": "grid1d",
  "scene": { },
  "steps": [ ]
}
```

- `kind`：场景种类，决定前端渲染器。v1 定义三种：`grid1d` / `grid2d` / `sequence`。
- `scene`：初始场景，kind 特定（见下）。
- `steps`：有序步骤数组；前端播放器按步推进，每步应用 `updates` 到场景状态。

## 步骤（Step）

```json
{
  "i": 12,
  "updates": [ {"index": 5, "state": "eliminated"} ],
  "caption": "划去 3 的倍数：9",
  "codeLine": 16
}
```

| 字段 | 说明 |
|------|------|
| `i` | 步骤序号（从 0 开始，冗余自描述） |
| `updates` | 本步对单元格/元素的增量修改数组（见下） |
| `caption` | 中文解说（一句话，渲染在播放器下方） |
| `codeLine` | 可选；对应 `solution.kt` 的行号，前端高亮该行（代码联动） |

### update 条目

- grid1d / grid2d：`{"index": <cell 序号>, "state": <状态>}`，可选 `"label": "<覆盖显示文本>"`
- sequence：`{"append": <数值>, "state": <状态>}`（向序列追加一个点）

### 状态枚举（v1）

`idle`（默认）、`current`（当前焦点）、`eliminated`（划去/排除）、`prime`（确认素数）、`onPath`（在最优路径上）、`considered`（被比较过）、`done`（收尾态）。

前端为每个状态定义固定配色/样式，新增 kind 不需要改状态语义。

## 场景种类

### grid1d —— 一维网格（候选：埃氏筛）

```json
"scene": {
  "columns": 10,
  "cells": [ {"index": 0, "value": 2, "state": "idle"}, ... ]
}
```

### grid2d —— 二维/三角网格（候选：018 路径 DP）

```json
"scene": {
  "rows": 15,
  "triangle": true,
  "cells": [ {"index": 0, "row": 0, "col": 0, "value": 75, "state": "idle"}, ... ]
}
```

`index` 按行优先编号；`triangle: true` 时前端按三角阵居中排布。

### sequence —— 数值序列（候选：Collatz 轨道）

```json
"scene": { "start": 27, "xLabel": "步数", "yLabel": "值" }
```

步骤用 `{"append": v}` 逐点追加，前端画折线。

## 演示实例约定

可视化用**缩小规模的演示实例**（如筛到 100、Collatz 起点 27、018 原题三角形），与原题求解分离；生成器代码注明演示参数。步骤总数控制在 ≤ 300（超出则按"每个素数一步"这类粒度合并 updates）。

## API

`GET /api/problems/{id}/visualize` → 上述 JSON；题目无可视化时 404 `{"error":"no_visualization", ...}`。题目的 `meta.hasVisualization` 与生成器注册表保持一致。
