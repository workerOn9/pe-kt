# 708 · 你只需要二

> 中文意译。英文原文见 [Project Euler Problem 708](https://projecteuler.net/problem=708)；抓取底稿见 `statement.en.md`。

把正整数 $n$ 分解为质因数。我们定义 $f(n)$ 为把每个质因数替换成 $2$ 之后的乘积。此外定义 $f(1)=1$。

例如，$90 = 2\times 3\times 3\times 5$，替换质数后得 $2\times 2\times 2\times 2 = 16$，因此 $f(90) = 16$。

记 $\displaystyle S(N)=\sum_{n=1}^{N} f(n)$。已知 $S(10^8)=9613563919$。

求 $S(10^{14})$。
