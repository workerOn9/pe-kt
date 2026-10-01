# 912 · 何处寻奇数

> 中文意译。英文原文见 [Project Euler Problem 912](https://projecteuler.net/problem=912)；抓取底稿见 `statement.en.md`。

设 $s_n$ 为二进制表示中不包含三个连续 $1$ 的第 $n$ 个正整数。
例如，$s_1 = 1$ 且 $s_7 = 8$。

定义 $F(N)$ 为所有使得 $s_n$ 为奇数的指标 $n \le N$ 对应的 $n^2$ 之和。已知 $F(10) = 199$。

求 $F(10^{16})$，并将答案对 $10^9+7$ 取模。
