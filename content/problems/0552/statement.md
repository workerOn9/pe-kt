# 552 · 中国剩余 II

> 中文意译。英文原文见 [Project Euler Problem 552](https://projecteuler.net/problem=552)；抓取底稿见 `statement.en.md`。

记 $A_n$ 为满足对所有 $1 \le i \le n$ 都有 $A_n \bmod p_i = i$ 的最小正整数，其中 $p_i$ 是第 $i$ 个素数。

例如 $A_2 = 5$，因为它是同余方程组

$$
\begin{aligned}
A_2 &\bmod 2 = 1 \\
A_2 &\bmod 3 = 2
\end{aligned}
$$

的最小正解。

$A_3$ 的方程组又增加了一个约束，即 $A_3$ 是

$$
\begin{aligned}
A_3 &\bmod 2 = 1 \\
A_3 &\bmod 3 = 2 \\
A_3 &\bmod 5 = 3
\end{aligned}
$$

的最小正解，因此 $A_3 = 23$。类似地可得 $A_4 = 53$，$A_5 = 1523$。

记 $S(n)$ 为不超过 $n$ 且能整除序列 $A$ 中至少一个元素的所有素数之和。例如 $S(50) = 69 = 5 + 23 + 41$，因为 $5$ 整除 $A_2$，$23$ 整除 $A_3$，$41$ 整除 $A_{10} = 5765999453$；不超过 $50$ 的其它素数都不能整除 $A$ 中的任何元素。

求 $S(300000)$。
