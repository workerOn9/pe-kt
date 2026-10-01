# 451 · 模逆元

> 中文意译。英文原文见 [Project Euler Problem 451](https://projecteuler.net/problem=451)；抓取底稿见 `statement.en.md`。

考虑数 $15$。
有八个小于 $15$ 且与 $15$ 互素的正数：$1, 2, 4, 7, 8, 11, 13, 14$。
这些数模 $15$ 的模逆分别是：$1, 8, 4, 13, 2, 11, 7, 14$，
因为
$$
\begin{aligned}
1 \cdot 1 \bmod 15 &= 1 \\
2 \cdot 8 = 16 \bmod 15 &= 1 \\
4 \cdot 4 = 16 \bmod 15 &= 1 \\
7 \cdot 13 = 91 \bmod 15 &= 1 \\
11 \cdot 11 = 121 \bmod 15 &= 1 \\
14 \cdot 14 = 196 \bmod 15 &= 1
\end{aligned}
$$

记 $I(n)$ 为小于 $n-1$ 且满足「模 $n$ 的模逆等于自身」的最大正整数 $m$。
于是 $I(15)=11$。
又有 $I(100)=51$ 与 $I(7)=1$。

求 $\sum I(n)$，其中 $3 \le n \le 2 \times 10^7$。
