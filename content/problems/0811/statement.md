# 811 · 按位递推

> 中文意译。英文原文见 [Project Euler Problem 811](https://projecteuler.net/problem=811)；抓取底稿见 `statement.en.md`。

记 $b(n)$ 为整除 $n$ 的最大 2 的幂。例如 $b(24) = 8$。

定义递归函数：

$$
\begin{aligned} \begin{split} A(0) &= 1\\ A(2n) &= 3A(n) + 5A\big(2n - b(n)\big)  \qquad n \gt 0\\ A(2n+1) &= A(n) \end{split} \end{aligned}
$$

并令 $H(t,r) = A\big((2^t+1)^r\big)$。

已知 $H(3,2) = A(81) = 636056$。

求 $H(10^{14}+31,62)$，答案对 $1\,000\,062\,031$ 取模。
