# 531 · 中国剩余

> 中文意译。英文原文见 [Project Euler Problem 531](https://projecteuler.net/problem=531)；抓取底稿见 `statement.en.md`。

记 $g(a, n, b, m)$ 为下述同余方程组的最小非负解 $x$：

$$
\begin{aligned}
x &\equiv a \pmod n \\
x &\equiv b \pmod m
\end{aligned}
$$

若这样的解存在则取该最小非负解，否则取 $0$。

例如 $g(2,4,4,6) = 10$，而 $g(3,4,4,6) = 0$。

记 $\phi(n)$ 为欧拉函数。

记 $f(n,m) = g(\phi(n), n, \phi(m), m)$。

求 $\sum f(n,m)$，其中 $1000000 \le n \lt m \lt 1005000$。
