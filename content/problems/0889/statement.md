# 889 · 有理布朗芒热函数

> 中文意译。英文原文见 [Project Euler Problem 889](https://projecteuler.net/problem=889)；抓取底稿见 `statement.en.md`。

回顾 [Problem 226](https://projecteuler.net/problem=226) 中的布朗芒热（blancmange）函数：$T(x) = \sum\limits_{n = 0}^\infty\dfrac{s(2^nx)}{2^n}$，其中 $s(x)$ 为 $x$ 到最近整数的距离。

对于正整数 $k, t, r$，我们记：

$$
F(k, t, r) = (2^{2k} - 1)T\left(\frac{(2^t + 1)^r}{2^k + 1}\right)
$$

可以证明 $F(k, t, r)$ 恒为整数。
已知 $F(3, 1, 1) = 42$，$F(13, 3, 3) = 23093880$，以及 $F(103, 13, 6) \equiv 878922518 \pmod{1\,000\,062\,031}$。

求 $F(10^{18} + 31, 10^{14} + 31, 62)$，并将答案对 $1\,000\,062\,031$ 取模。
