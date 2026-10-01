# 646 · 有界因数

> 中文意译。英文原文见 [Project Euler Problem 646](https://projecteuler.net/problem=646)；抓取底稿见 `statement.en.md`。

设 $n$ 为自然数，$p_1^{\alpha_1}\cdot p_2^{\alpha_2}\cdots p_k^{\alpha_k}$ 为其质因数分解。
定义刘维尔函数 $\lambda(n) = (-1)^{\sum\limits_{i=1}^{k}\alpha_i}$。
（即若指数 $\alpha_i$ 之和为奇数则为 $-1$，若指数之和为偶数则为 $1$。）
设 $S(n,L,H)$ 为对所有满足 $L \leq d \leq H$ 的 $n$ 的因数 $d$ 求 $\lambda(d) \cdot d$ 之和。

已知：

$S(10! , 100, 1000) = 1457$
$S(15!, 10^3, 10^5) = -107974$
$S(30!,10^8, 10^{12}) = 9766732243224$。

求 $S(70!,10^{20}, 10^{60})$，答案模 $1\,000\,000\,007$。
