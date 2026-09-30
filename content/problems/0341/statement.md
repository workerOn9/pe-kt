# 341 · Golomb 自描述数列

> 中文意译。英文原文见 [Project Euler Problem 341](https://projecteuler.net/problem=341)；抓取底稿见 `statement.en.md`。

Golomb 自描述数列 $(G(n))$ 是这样一个唯一的单调不减自然数序列：数 $n$ 在序列中恰好出现 $G(n)$ 次。前若干项为
$$
\begin{array}{c|cccccccccccccccc}
n & 1 & 2 & 3 & 4 & 5 & 6 & 7 & 8 & 9 & 10 & 11 & 12 & 13 & 14 & 15 & \cdots \\
\hline
G(n) & 1 & 2 & 2 & 3 & 3 & 4 & 4 & 4 & 5 & 5 & 5 & 6 & 6 & 6 & 6 & \cdots
\end{array}
$$

已知 $G(10^3)=86$，$G(10^6)=6137$；又已知 $1 \le n < 10^3$ 时 $\sum G(n^3) = 153506976$。

求 $1 \le n < 10^6$ 时 $\sum G(n^3)$。
