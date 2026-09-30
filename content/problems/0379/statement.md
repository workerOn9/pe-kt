# 379 · 最小公倍数求和

> 中文意译。英文原文见 [Project Euler Problem 379](https://projecteuler.net/problem=379)；抓取底稿见 `statement.en.md`。

记 $f(n)$ 为满足 $1 \le x \le y$ 且 $\operatorname{lcm}(x, y) = n$ 的正整数对 $(x, y)$ 的个数。

记 $g$ 为 $f$ 的前缀和函数，即 $g(n) = \displaystyle\sum_{i=1}^n f(i)$。

已知 $g(10^6) = 37429395$。

求 $g(10^{12})$。
