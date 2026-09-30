# 343 · 分数数列

> 中文意译。英文原文见 [Project Euler Problem 343](https://projecteuler.net/problem=343)；抓取底稿见 `statement.en.md`。

对任意正整数 $k$，定义由分数 $x_i/y_i$ 构成的有限数列 $a_i$：
$$
a_1 = 1/k , \qquad a_i = (x_{i-1} + 1) / (y_{i-1} - 1) \quad (i > 1) \text{，并约成最简分数} .
$$
当 $a_i$ 约分后成为某个整数 $n$ 时数列终止（即 $y_i = 1$）。定义 $f(k) = n$。

例如 $k=20$ 时：
$$
1/20 \to 2/19 \to 3/18 = 1/6 \to 2/5 \to 3/4 \to 4/3 \to 5/2 \to 6/1 = 6
$$
所以 $f(20) = 6$。

又有 $f(1)=1$，$f(2)=2$，$f(3)=1$，且 $1 \le k \le 100$ 时 $\sum f(k^3) = 118937$。

求 $1 \le k \le 2\times 10^6$ 时 $\sum f(k^3)$。
