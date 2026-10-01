# 515 · 不协和数

> 中文意译。英文原文见 [Project Euler Problem 515](https://projecteuler.net/problem=515)；抓取底稿见 `statement.en.md`。

记 $d(p, n, 0)$ 为 $n$ 模素数 $p$ 的乘法逆元，即 $n$ 的逆元 $d$ 满足 $n \times d(p, n, 0) = 1 \bmod p$。

对 $k \ge 1$，记 $d(p, n, k) = \sum_{i = 1}^n d(p, i, k - 1)$。

记 $D(a, b, k) = \sum (d(p, p-1, k) \bmod p)$，其中求和遍历所有满足 $a \le p \lt a + b$ 的素数 $p$。

已知：

$$
\begin{aligned}
D(101,1,10) &= 45 \\
D(10^3,10^2,10^2) &= 8334 \\
D(10^6,10^3,10^3) &= 38162302
\end{aligned}
$$

求 $D(10^9,10^5,10^5)$。
