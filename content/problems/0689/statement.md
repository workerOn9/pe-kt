# 689 · 二进制级数

> 中文意译。英文原文见 [Project Euler Problem 689](https://projecteuler.net/problem=689)；抓取底稿见 `statement.en.md`。

对 $0 \le x \lt 1$，定义 $d_i(x)$ 为 $x$ 的二进制表示中小数点后第 $i$ 位数字。例如 $d_2(0.25) = 1$，而对 $i \ne 2$ 有 $d_i(0.25) = 0$。

设 $f(x) = \displaystyle{\sum_{i=1}^{\infty}\frac{d_i(x)}{i^2}}$。

设 $p(a)$ 为在 $x$ 于 $0$ 到 $1$ 之间均匀分布的条件下 $f(x) \gt a$ 的概率。

求 $p(0.5)$，答案四舍五入到小数点后 $8$ 位。
