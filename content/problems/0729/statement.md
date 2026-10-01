# 729 · 周期序列的极差

> 中文意译。英文原文见 [Project Euler Problem 729](https://projecteuler.net/problem=729)；抓取底稿见 `statement.en.md`。

考虑由初值 $a_0$ 和递推式 $\displaystyle a_{n+1}=a_n-\frac 1 {a_n}$（对任意 $n \ge 0$）定义的实数序列 $a_n$。

对某些初值 $a_0$，该序列是周期的。例如 $a_0=\sqrt{\frac 1 2}$ 产生的序列为：$\sqrt{\frac 1 2},-\sqrt{\frac 1 2},\sqrt{\frac 1 2}, \dots$。

我们关注这种周期序列的极差，即序列最大值与最小值之差。例如上述序列的极差为 $\sqrt{\frac 1 2}-(-\sqrt{\frac 1 2})=\sqrt{ 2}$。

设 $S(P)$ 为所有周期不超过 $P$ 的这种周期序列的极差之和。
例如 $S(2)=2\sqrt{2} \approx 2.8284$，它是从 $a_0=\sqrt{\frac 1 2}$ 和 $a_0=-\sqrt{\frac 1 2}$ 出发的两个序列极差之和。
已知 $S(3) \approx 14.6461$，$S(5) \approx 124.1056$。

求 $S(25)$，四舍五入到 $4$ 位小数。
