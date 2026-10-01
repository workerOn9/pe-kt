# 530 · 因子的最大公约数

> 中文意译。英文原文见 [Project Euler Problem 530](https://projecteuler.net/problem=530)；抓取底稿见 `statement.en.md`。

数 $n$ 的每个因子 $d$ 都有一个互补因子 $n/d$。

记 $f(n)$ 为 $n$ 的所有正因子 $d$ 对应的 $\gcd(d, n/d)$ 之和，即 $f(n) = \displaystyle\sum_{d \mid n} \gcd\left(d, \frac{n}{d}\right)$。

记 $F$ 为 $f$ 的和函数，即 $F(k) = \displaystyle\sum_{n=1}^k f(n)$。

已知 $F(10) = 32$，$F(1000) = 12776$。

求 $F(10^{15})$。
