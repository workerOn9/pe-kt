# 304 · 素数斐波那契

> 中文意译。英文原文见 [Project Euler Problem 304](https://projecteuler.net/problem=304)；抓取底稿见 `statement.en.md`。

对任意正整数 $n$，记 $\operatorname{next\_prime}(n)$ 为大于 $n$ 的最小素数。

数列 $a(n)$ 定义为
$$
a(1) = \operatorname{next\_prime}(10^{14}) , \qquad a(n) = \operatorname{next\_prime}(a(n-1)) \quad (n > 1) .
$$

斐波那契数列 $f(n)$ 定义为 $f(0)=0$、$f(1)=1$，且对 $n>1$ 有 $f(n)=f(n-1)+f(n-2)$。

数列 $b(n)$ 定义为 $b(n) = f(a(n))$。

求 $\displaystyle\sum_{n=1}^{100\,000} b(n)$ 对 $1234567891011$ 取模的结果。
