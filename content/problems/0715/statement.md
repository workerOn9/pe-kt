# 715 · 六元组范数

> 中文意译。英文原文见 [Project Euler Problem 715](https://projecteuler.net/problem=715)；抓取底稿见 `statement.en.md`。

记 $f(n)$ 为满足以下条件的 $6$ 元组 $(x_1,x_2,x_3,x_4,x_5,x_6)$ 的个数：

所有 $x_i$ 都是满足 $0 \leq x_i < n$ 的整数
$\gcd(x_1^2+x_2^2+x_3^2+x_4^2+x_5^2+x_6^2,\ n^2)=1$

记 $\displaystyle G(n)=\displaystyle\sum_{k=1}^n \frac{f(k)}{k^2\varphi(k)}$
其中 $\varphi(n)$ 是欧拉函数。

例如，$G(10)=3053$，$G(10^5) \equiv 157612967 \pmod{1\,000\,000\,007}$。

求 $G(10^{12})\bmod 1\,000\,000\,007$。
