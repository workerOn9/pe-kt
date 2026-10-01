# 924 · 数位重排极大数 II

> 中文意译。英文原文见 [Project Euler Problem 924](https://projecteuler.net/problem=924)；抓取底稿见 `statement.en.md`。

设 $B(n)$ 表示通过重新排列 $n$ 的各个数位所能构成的严格大于 $n$ 的最小整数；若不存在这样的数，则定义 $B(n) = 0$。例如 $B(245) = 254$，$B(542) = 0$。

数列定义为 $a_0 = 0$，且对 $n > 0$ 有 $a_n = a_{n-1}^2 + 2$。

记：

$$
U(N) = \sum_{n = 1}^N B(a_n)
$$

已知 $U(10) \equiv 543870437 \pmod{10^9+7}$。

求 $U(10^{16}) \bmod (10^9 + 7)$。
