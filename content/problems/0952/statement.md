# 952 · 阶乘模阶

> 中文意译。英文原文见 [Project Euler Problem 952](https://projecteuler.net/problem=952)；抓取底稿见 `statement.en.md`。

给定素数 $p$ 和正整数 $n < p$，设 $R(p, n)$ 为 $p$ 模 $n!$ 的乘法阶。
换言之，$R(p, n)$ 是满足如下条件的最小正整数 $r$：

$$
p^r \equiv 1 \pmod{n!}
$$

例如，$R(7, 4) = 2$ 且 $R(10^9 + 7, 12) = 17280$。

求 $R(10^9 + 7, 10^7) \bmod (10^9 + 7)$。
