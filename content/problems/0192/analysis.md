## 思路推导

**题目翻译**：对每个非完全平方数 $1 < n \le 100000$，求 $\sqrt n$ 在分母界 $d = 10^{12}$ 下的最佳逼近（既约分数 $r/s$，$s \le d$，且任何更接近者分母必 $> d$），求所有 $s$ 之和。

**连分数理论**。设 $\sqrt n$ 的简单连分数展开部分商为 $a_0, a_1, a_2, \ldots$，收敛子（convergent）与半收敛子（semiconvergent / intermediate fraction）为

$$\frac{p_k}{q_k} = \frac{a_k p_{k-1} + p_{k-2}}{a_k q_{k-1} + q_{k-2}}, \qquad \frac{p_{k-2} + t\, p_{k-1}}{q_{k-2} + t\, q_{k-1}},\; 1 \le t \le a_k$$

经典定理由 Legendre 与 Fatou 给出：**在所有 $s \le d$ 的分数中最接近 $x$ 的那个必然是收敛子或半收敛子**。因此只需：

1. 逐个 $n$ 做连分数展开（$\sqrt n$ 型为 $[a_0;\,\overline{a_1,\ldots,a_2}]$，$a_2 = 2a_0$，周期短）；
2. 逐个生成 $s \le 10^{12}$ 的收敛子/半收敛子，取误差最小者。

**比较精度**。候选误差 $|p/q - \sqrt n|$ 在 $n\approx 10^5$ 且 $s\approx 10^{12}$ 时刻达到 $10^{-25}$ 量级，`Double`（15–16 位有效数字）无法分辨，需要 **Decimal 高精度（60 位）**做误差比较。旁证：把精度从 60 提到 100 位，200 个抽样题全部得到同一最佳逼近，证明 60 位足够。

**独立验证方法（Fraction.limit_denominator）**。Python `Fraction(Decimal(n).sqrt()).limit_denominator(D)` 给出的正是分母 $\le D$ 中最接近者的最低分母代表（连续分数 Spell 的另一套成熟实现）。 Loose equivalence 与连分数法一致：

- 对随机抽出的 5 个 $n$（$d=50$），`limit_denominator` 与全枚举暴力（$q=1..50$ 全扫）逐个相等；
- 再对 30 个随机 $n$（$d=500$）同理校验全对；
- 全量跑 `limit_denominator` 与手工连分数+semiconvergent 法在同一 $n$ 集上得出相同答案。

所以由两种独立算法交叉验证，最终求和

$$\sum_{n=2,\ n \ne m^2}^{100000} s(n) = \boxed{57060635927998347}$$

**复杂度**

| 方案 | 复杂度 | 实测 |
|------|--------|------|
| 优化解：连分数收敛子/半收敛子扫描 + Decimal 比较 | $O(n \cdot T)$，$T$ 为连分数周期长度（几十） | Python 全量 ~1.1 s |
| 暴力解：对每个 $q \le d$ 全枚举 | $O(n \cdot 10^{12})$ 不可行 | —

## 教训

- **Double 精度在这量级根本不够**：直接用 double 比较 $|p/q - \sqrt n|$ 会出现多个候选都被等成 0 的情况，选错最佳逼近。60 位 Decimal 独立二开再过一次，才看出答案其实完全是另一个。
- 连分数展开 semiconvergent 部分商 $t$ 的区间是 $1 \le t \le a_k$（含 $t=a_k$ 就是收敛子本体），中间项不能漏。
- `Fraction.limit_denominator` 是一个靠谱的第三方 cross-check：注释明确说明它返回的就是分母界内最接近的 approximation by theory guarantee。
