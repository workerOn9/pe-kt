# 831 · 三重积

> 中文意译。英文原文见 [Project Euler Problem 831](https://projecteuler.net/problem=831)；抓取底稿见 `statement.en.md`。

记 $g(m)$ 为由下列二项式系数乘积的双重和定义的整数：

$$
\sum_{j=0}^m\sum_{i = 0}^j (-1)^{j-i}\binom mj \binom ji \binom{j+5+6i}{j+5}.
$$

已知 $g(10) = 127278262644918$。它的前（最高）五位数字是 $12727$。
求 $g(142857)$ 写成七进制时的前十个数字。
