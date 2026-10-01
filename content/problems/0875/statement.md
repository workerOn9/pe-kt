# 875 · 四元二次同余方程

> 中文意译。英文原文见 [Project Euler Problem 875](https://projecteuler.net/problem=875)；抓取底稿见 `statement.en.md`。

对于正整数 $n$，定义 $q(n)$ 为满足以下同余式的解的组数：

$$
a_1^2 + a_2^2 + a_3^2 + a_4^2 \equiv b_1^2 + b_2^2 + b_3^2 + b_4^2 \pmod n
$$

其中 $0 \le a_i, b_i < n$。例如，$q(4) = 18432$。

定义

$$
Q(n) = \sum_{i=1}^n q(i)
$$

已知 $Q(10) = 18573381$。

求 $Q(12345678) \bmod 1001961001$。
