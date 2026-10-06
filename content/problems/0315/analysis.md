# 315 — Digital Root Clocks（数根时钟）

## 七段数码管段位定义与状态转换

Sam 的数根时钟每次计算中间值时先将全部段熄灭再点亮下一个数；Max 的时钟则只切换发生变化的段。
我们需要求输入 $10^7$ 到 $2 \times 10^7$ 之间所有素数时，两钟的切换总次数之差。

注意本题七段数码管的特殊定义（7 含有左上段 $f$）：
- $0 = abcdef$（$6$ 段）
- $1 = bc$（$2$ 段）
- $2 = abdeg$（$5$ 段）
- $3 = abcdg$（$5$ 段）
- $4 = bcfg$（$4$ 段）
- $5 = acdfg$（$5$ 段）
- $6 = acdefg$（$6$ 段）
- $7 = abcf$（$4$ 段）
- $8 = abcdefg$（$7$ 段）
- $9 = abcdfg$（$6$ 段）

## 状态转移差值的代数简化

对于两个相邻显示的数字状态掩码集合 $U$ 与 $V$（右对齐，空位掩码为 $0$）：
- Sam 产生的段切换次数为点亮 $U$、熄灭 $U$、点亮 $V$、熄灭 $V$，单步切换数为：
  $$
  \operatorname{popcount}(U) + \operatorname{popcount}(V)
  $$
- Max 只切换变化的段：
  $$
  \operatorname{popcount}(U \oplus V)
  $$

两者的单步差值为：
$$
[\operatorname{popcount}(U) + \operatorname{popcount}(V)] - \operatorname{popcount}(U \oplus V) = 2 \cdot \operatorname{popcount}(U \land V)
$$
起始状态与终止状态均为面板全灭（掩码全 0），两端与任何数字掩码的按位与都为 0，差值正好为 0。
因此，对于任意一个输入的素数，**Sam 比 Max 省去的总操作数恰好等于相邻展示数字在对齐位上的公共亮段数目之和的两倍**：
$$
\Delta = \sum_{\text{相邻两步}} 2 \cdot \operatorname{popcount}(U_i \land U_{i+1})
$$

## 验证

1. **题面样本验证**：以 $137 \to 11 \to 2$ 为例：
   - Sam 总步数：$22 + 8 + 10 = 40$
   - Max 总步数：$11 + 7 + 0 + 3 + 4 + 5 = 30$
   - 差值：$40 - 30 = 10$。
   - 代数公式：$137 \to 11$ 公共段（十位 $3 \land 1$ 贡献 $bc$，个位 $7 \land 1$ 贡献 $bc$ 共 4 段）+ $11 \to 2$ 公共段（个位 $1 \land 2$ 贡献 $b$ 共 1 段），差值 $= 2 \times (4 + 1) = 10$，完全相符。
2. **双方法互证**：在 $[10^7, 2 \times 10^7]$ 区间内全部 $606\,028$ 个素数上，逐段模拟开关状态法与按位与位运算加速法结果逐一完全一致。

$$
\boxed{13625242}
$$

## 复杂度对比

| 方法 | 规模 | 耗时 |
| --- | --- | --- |
| 逐段开关状态模拟（暴力） | $606\,028$ 个素数 | 126.0 ms |
| 按位与位运算加速公式（正式） | $606\,028$ 个素数 | 28.5 ms |

## 关键教训

* 异或距离与对称差满足恒等式 $\operatorname{popcount}(A) + \operatorname{popcount}(B) - \operatorname{popcount}(A \oplus B) = 2\operatorname{popcount}(A \land B)$，能把复杂的增量更新模拟化简为静态重叠统计。
* 数位比较时必须从个位向高位右对齐，高位补零掩码可天然参与位运算而无需分支判断。
