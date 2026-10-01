# 785 · 对称丢番图方程

> 中文意译。英文原文见 [Project Euler Problem 785](https://projecteuler.net/problem=785)；抓取底稿见 `statement.en.md`。

考虑如下丢番图方程：

$$
15  (x^2 + y^2 + z^2) = 34  (xy + yz + zx)
$$

其中 $x$、$y$、$z$ 为正整数。

记 $S(N)$ 为该方程所有满足 $1 \le x \le y \le z \le N$ 且 $\gcd(x, y, z) = 1$ 的解 $(x,y,z)$ 之和。

对于 $N = 10^2$，有三个这样的解 —— $(1, 7, 16), (8, 9, 39), (11, 21, 72)$。因此 $S(10^2) = 184$。

求 $S(10^9)$。
