# 073 · 区间内的分数计数 — 解析

## 思路推导

要统计的是满足下式的二元组 $(n,d)$ 的个数：

$$2 \le d \le 12\,000,\quad 1 \le n < d,\quad \gcd(n,d)=1,\quad \frac 13 < \frac n d < \frac 12 .$$

**第一步：把分数比较换成整数不等式。** 用浮点数比较既慢又有精度风险，而两个界都含有理数，可以直接交叉相乘：

$$
\frac{n}{d} > \frac 13 \iff 3n > d \iff n \ge \left\lfloor \frac d 3 \right\rfloor + 1 =: lo, \qquad
\frac{n}{d} < \frac 12 \iff 2n < d \iff n \le \left\lfloor \frac{d-1}{2} \right\rfloor =: hi .
$$

窗口长度约 $d/6$，仅此一步就把每个分母的候选从 $d$ 个压到 $d/6$ 个。

**第二步：窗口内与 $d$ 互素的个数用容斥算。** 设 $d$ 的不同素因子为 $p_1,\dots,p_k$，则

$$\gcd(n,d)=1 \iff \forall i,\ p_i \nmid n .$$

对「被 $p_i$ 整除」这 $k$ 个事件做容斥，并注意区间 $[lo,hi]$ 中 $m$ 的倍数个数是 $\lfloor hi/m\rfloor - \lfloor (lo-1)/m \rfloor$，于是

$$
\#\{n \in [lo,hi] : \gcd(n,d)=1\}
= \sum_{S \subseteq \{1..k\}} (-1)^{|S|} \left( \left\lfloor \frac{hi}{\prod_{i \in S} p_i} \right\rfloor - \left\lfloor \frac{lo-1}{\prod_{i \in S} p_i} \right\rfloor \right).
$$

**第三步：确认 $2^k$ 足够小。** $d \le 12\,000$ 时不同素因子最多 $5$ 个：

$$2 \cdot 3 \cdot 5 \cdot 7 \cdot 11 = 2310 \le 12\,000, \qquad 2 \cdot 3 \cdot 5 \cdot 7 \cdot 11 \cdot 13 = 30\,030 > 12\,000 .$$

所以每个 $d$ 最多枚举 $2^5 = 32$ 个子集，总运算量约 $12\,000 \times 32 \approx 4 \times 10^5$ 次整数除法，远低于直接判 gcd 的 $1.2 \times 10^7$ 次欧几里得算法。最小素因子筛（SPF）让「分解并去重出 $p_1..p_k$」在 $O(\log d)$ 内完成。

用题面样例校验：$d \le 8$ 时算法给出 $3$（$3/8, 2/5, 3/7$），与题面一致。答案：**7295372**。

## 复杂度对比

| 方案 | 时间 | 实测（JIT 预热后） |
|------|------|------|
| 优化解：SPF 分解 $d$ + 容斥计数 | $O\!\left(\sum_{d \le N} 2^{\omega(d)}\right) \le 32N$ | ~1.311 ms |
| 暴力解：扫遍 $n=1..d-1$，交叉相乘后逐个 gcd | $O(N^2/2)$ 次循环，其中 $1.2\times10^7$ 次 gcd | ~445.165 ms |

作为参照，本机还实测了「只把区间化成窗口、仍逐个判 gcd」的中间写法：约 $717\ \text{ms}$。可见把候选范围缩小六倍几乎不省钱——真正的开销是欧几里得算法本身；只有把「互素判定」整体替换成素因子容斥，才拿到数量级的改善。这也解释了为什么数论代码里 $\varphi$ 筛、Möbius 反演、容斥这类「用分解换判定」的手法如此常见：一次分解的代价，可以抵消成千上万次逐对判定。

## 实现要点

- SPF 筛：对每个素数 $p$ 只填「尚未被填过」的倍数，保证 `spf[x]` 是最小素因子，分解时 `x /= p` 反复除尽即可去重
- 每个 $d$ 的素因子个数 $k \le 5$，`factors` 开长度 8 足够；子集枚举用 `mask in 0 until (1 shl k)`
- 区间内整除数个数必须用 $\lfloor hi/m \rfloor - \lfloor (lo-1)/m \rfloor$，写成 $\lfloor lo/m \rfloor$ 会漏掉 $m \mid lo$ 的情况
- 小分母（$d \le 4$）窗口为空，用 `if (lo > hi) continue` 跳过
- 用 `Int` 存乘积已够（$\prod p_i \le d \le 12000$），但计数累加与返回值用 `Long`

## 代码

参考实现：[solution.kt](solution.kt)（SPF + 容斥）｜对比：[brute-force.kt](brute-force.kt)（全窗口扫描 + 逐个 gcd）
