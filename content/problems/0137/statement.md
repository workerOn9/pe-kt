# 斐波那契金块

> 中文意译。英文原文见 [Project Euler Problem 137](https://projecteuler.net/problem=137)；抓取底稿见 `statement.en.md`。

考虑无穷多项式级数

$$
A_F(x) = xF_1 + x^2F_2 + x^3F_3 + \cdots,
$$

其中 $F_k$ 是斐波那契数列 $1, 1, 2, 3, 5, 8, \dots$ 的第 $k$ 项，即 $F_k = F_{k-1} + F_{k-2}$，$F_1 = 1$，$F_2 = 1$。

本题关心那些使 $A_F(x)$ 取**正整数**的 $x$。

令人意外的是：

$$
\begin{aligned}
A_F(\tfrac12) &= (\tfrac12)\times 1 + (\tfrac12)^2\times 1 + (\tfrac12)^3\times 2 + (\tfrac12)^4\times 3 + (\tfrac12)^5\times 5 + \cdots \\
&= \tfrac12 + \tfrac14 + \tfrac28 + \tfrac3{16} + \tfrac5{32} + \cdots \\
&= 2
\end{aligned}
$$

前五个自然数对应的 $x$ 值如下表：

| $x$ | $A_F(x)$ |
|---|---|
| $\sqrt2 - 1$ | $1$ |
| $\tfrac12$ | $2$ |
| $\dfrac{\sqrt{13}-2}{3}$ | $3$ |
| $\dfrac{\sqrt{89}-5}{8}$ | $4$ |
| $\dfrac{\sqrt{34}-3}{5}$ | $5$ |

当 $x$ 是有理数时，我们称 $A_F(x)$ 为一个**金块**（golden nugget）——这样的 $x$ 越来越稀有；例如第 $10$ 个金块是 $74049690$。

求第 $15$ 个金块。
