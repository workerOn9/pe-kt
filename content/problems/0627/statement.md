# 627 · 计数乘积

> 中文意译。英文原文见 [Project Euler Problem 627](https://projecteuler.net/problem=627)；抓取底稿见 `statement.en.md`。

考虑不超过 $m$ 的 $n$ 个正整数所有可能的乘积构成的集合 $S$，即

$S=\{ x_1x_2\cdots x_n \mid 1 \le x_1, x_2, \dots, x_n \le m \}$。

设 $F(m,n)$ 为集合 $S$ 中不同元素的个数。

例如 $F(9, 2) = 36$，$F(30,2)=308$。

求 $F(30, 10001) \bmod 1\,000\,000\,007$。
