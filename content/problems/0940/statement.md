# 940 · 二维递推

> 中文意译。英文原文见 [Project Euler Problem 940](https://projecteuler.net/problem=940)；抓取底稿见 `statement.en.md`。

斐波那契数列 $(f_i)$ 是满足以下条件的唯一数列：
- $f_0 = 0$
- $f_1 = 1$
- $f_{i+1} = f_i + f_{i-1}$

类似地，存在唯一的二元函数 $A(m, n)$ 满足以下递推关系：
- $A(0, 0) = 0$
- $A(0, 1) = 1$
- $A(m+1, n) = A(m, n+1) + A(m, n)$
- $A(m+1, n+1) = 2A(m+1, n) + A(m, n)$

定义 $S(k) = \displaystyle\sum_{i=2}^k \sum_{j=2}^k A(f_i, f_j)$。例如：

$$
\begin{aligned}
S(3) &= A(1, 1) + A(1, 2) + A(2, 1) + A(2, 2) \\
&= 2 + 5 + 7 + 16 \\
&= 30
\end{aligned}
$$

已知 $S(5) = 10396$。

求 $S(50) \bmod 1123581313$。
