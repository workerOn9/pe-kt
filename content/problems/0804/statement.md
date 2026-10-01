# 804 · 二元二次型表示计数

> 中文意译。英文原文见 [Project Euler Problem 804](https://projecteuler.net/problem=804)；抓取底稿见 `statement.en.md`。

记 $g(n)$ 为正整数 $n$ 表示为

$$
x^2+xy+41y^2
$$

（其中 $x$ 与 $y$ 为整数）的方式数。例如 $g(53)=4$，对应 $(x,y) \in \{(-4,1),(-3,-1),(3,1),(4,-1)\}$。

定义 $\displaystyle T(N)=\sum_{n=1}^{N}g(n)$。已知 $T(10^3)=474$，$T(10^6)=492128$。

求 $T(10^{16})$。
