# 871 · 漂移子集

> 中文意译。英文原文见 [Project Euler Problem 871](https://projecteuler.net/problem=871)；抓取底稿见 `statement.en.md`。

设 $f$ 是有限集 $S$ 到自身的映射。若 $S$ 的子集 $A$ 满足并集 $A \cup f(A)$ 的元素个数恰好等于 $A$ 的元素个数的两倍，则称 $A$ 为映射 $f$ 的一个**漂移子集**（drifting subset）。
记 $D(f)$ 为映射 $f$ 的所有漂移子集中元素个数的最大值。

对于正整数 $n$，定义映射 $f_n\colon \{0, 1, \dots, n - 1\} \to \{0, 1, \dots, n - 1\}$ 为 $f_n(x) = (x^3 + x + 1) \bmod n$。
已知 $D(f_5) = 1$，$D(f_{10}) = 3$。

求

$$
\sum_{i=1}^{100} D(f_{10^5 + i})
$$
