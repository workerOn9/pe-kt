# 512 · 幂的欧拉函数之和

> 中文意译。英文原文见 [Project Euler Problem 512](https://projecteuler.net/problem=512)；抓取底稿见 `statement.en.md`。

记 $\varphi(n)$ 为欧拉函数。

令 $f(n) = (\sum_{i=1}^{n}\varphi(n^i)) \bmod (n+1)$。

令 $g(n) = \sum_{i=1}^{n} f(i)$。

已知 $g(100) = 2007$。

求 $g(5 \times 10^8)$。
