# 丢番图方程

> 中文意译。英文原文见 [Project Euler Problem 66](https://projecteuler.net/problem=66)；抓取底稿见 `statement.en.md`。

考虑形如

$$x^2 - Dy^2 = 1$$

的二次丢番图方程。

例如 $D=13$ 时，$x$ 的最小解为 $649^2 - 13 \times 180^2 = 1$。

当 $D$ 是完全平方数时可以认为方程没有正整数解。对 $D = \{2,3,5,6,7\}$ 求 $x$ 的最小解，得到：

$$\begin{aligned} 3^2 - 2 \times 2^2 &= 1\\ 2^2 - 3 \times 1^2 &= 1\\ 9^2 - 5 \times 4^2 &= 1\\ 5^2 - 6 \times 2^2 &= 1\\ 8^2 - 7 \times 3^2 &= 1 \end{aligned}$$

可见在 $D \le 7$ 中，$x$ 的最小解最大时取 $D=5$。

求 $D \le 1000$ 时，使 $x$ 的最小解取得最大值的那个 $D$。
