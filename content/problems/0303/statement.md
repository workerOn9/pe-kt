# 303 · 小数字倍数

> 中文意译。英文原文见 [Project Euler Problem 303](https://projecteuler.net/problem=303)；抓取底稿见 `statement.en.md`。

对正整数 $n$，记 $f(n)$ 为 $n$ 的最小正整数倍数中、十进制写法只用到数字 $\le 2$ 的那个。

例如 $f(2)=2$，$f(3)=12$，$f(7)=21$，$f(42)=210$，$f(89)=1121222$。

又有
$$
\sum_{n=1}^{100} \frac{f(n)}{n} = 11363107 .
$$

求
$$
\sum_{n=1}^{10000} \frac{f(n)}{n} .
$$
