# 438 · 多项式方程解的整数部分

> 中文意译。英文原文见 [Project Euler Problem 438](https://projecteuler.net/problem=438)；抓取底稿见 `statement.en.md`。

对整数 $n$ 元组 $t = (a_1, \dots, a_n)$，设 $(x_1, \dots, x_n)$ 为多项式方程
$$
x^n + a_1 x^{n-1} + a_2 x^{n-2} + \cdots + a_{n-1}x + a_n = 0
$$
的解。

考虑下面两个条件：

- $x_1, \dots, x_n$ 全为实数。
- 若将 $x_1, \dots, x_n$ 排序，则对 $1 \leq i \leq n$ 有 $\lfloor x_i\rfloor = i$（$\lfloor \cdot \rfloor$ 为向下取整函数）。

当 $n = 4$ 时，满足这两个条件的整数 $n$ 元组有 $12$ 个。定义 $S(t)$ 为 $t$ 中各整数绝对值之和。对 $n = 4$，可以验证所有满足两个条件的 $n$ 元组 $t$ 满足 $\sum S(t) = 2087$。

求 $n = 7$ 时的 $\sum S(t)$。
