# 754 · 高斯阶乘之积

> 中文意译。英文原文见 [Project Euler Problem 754](https://projecteuler.net/problem=754)；抓取底稿见 `statement.en.md`。

数 $n$ 的高斯阶乘定义为所有不超过 $n$ 且与 $n$ 互素的正整数之积。例如 $g(10)=1\times 3\times 7\times 9 = 189$。

再定义

$$
\displaystyle G(n) = \prod_{i=1}^{n}g(i)
$$

已知 $G(10) = 23044331520000$。

求 $G(10^8)$，答案对 $1\,000\,000\,007$ 取模。
