# 421 · 素因子之和

> 中文意译。英文原文见 [Project Euler Problem 421](https://projecteuler.net/problem=421)；抓取底稿见 `statement.en.md`。

形如 $n^{15}+1$ 的数对每个整数 $n \gt 1$ 都是合数。

对正整数 $n$ 与 $m$，定义 $s(n,m)$ 为 $n^{15}+1$ 的所有不超过 $m$ 的**不同**素因子之和。

例如 $2^{15}+1 = 3 \times 3 \times 11 \times 331$，所以 $s(2,10) = 3$，$s(2,1000) = 3+11+331 = 345$。

又有 $10^{15}+1 = 7 \times 11 \times 13 \times 211 \times 241 \times 2161 \times 9091$，所以 $s(10,100) = 31$，$s(10,1000) = 483$。

求
$$
\sum_{n=1}^{10^{11}} s(n,10^8) .
$$
