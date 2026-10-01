# 795 · 交错 GCD 和

> 中文意译。英文原文见 [Project Euler Problem 795](https://projecteuler.net/problem=795)；抓取底稿见 `statement.en.md`。

对于正整数 $n$，函数 $g(n)$ 定义为

$$
\displaystyle g(n)=\sum_{i=1}^{n} (-1)^i \gcd \left(n,i^2\right).
$$

例如，$g(4) = -\gcd \left(4,1^2\right) + \gcd \left(4,2^2\right) - \gcd \left(4,3^2\right) + \gcd \left(4,4^2\right) = -1+4-1+4=6$。
还已知 $g(1234)=1233$。

设 $\displaystyle G(N) = \sum_{n=1}^N g(n)$。已知 $G(1234) = 2194708$。

求 $G(12345678)$。
