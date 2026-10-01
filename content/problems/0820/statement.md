# 820 · 倒数的小数第 n 位

> 中文意译。英文原文见 [Project Euler Problem 820](https://projecteuler.net/problem=820)；抓取底稿见 `statement.en.md`。

记 $d_n(x)$ 为 $x$ 的小数部分的第 $n$ 位小数；若小数部分的位数少于 $n$ 位，则为 $0$。

例如：

$d_7 \mathopen{}\left( 1 \right)\mathclose{} = d_7 \mathopen{}\left( \frac 1 2 \right)\mathclose{} = d_7 \mathopen{}\left( \frac 1 4 \right)\mathclose{} = d_7 \mathopen{}\left( \frac 1 5 \right)\mathclose{} = 0$
$d_7 \mathopen{}\left( \frac 1 3 \right)\mathclose{} = 3$，因为 $\frac 1 3 =$ 0.3333333333...
$d_7 \mathopen{}\left( \frac 1 6 \right)\mathclose{} = 6$，因为 $\frac 1 6 =$ 0.1666666666...
$d_7 \mathopen{}\left( \frac 1 7 \right)\mathclose{} = 1$，因为 $\frac 1 7 =$ 0.1428571428...

设 $\displaystyle S(n) = \sum_{k=1}^n d_n \mathopen{}\left( \frac 1 k \right)\mathclose{}$。

已知：

$S(7) = 0 + 0 + 3 + 0 + 0 + 6 + 1 = 10$
$S(100) = 418$

求 $S(10^7)$。
