# 918 · 递归序列求和

> 中文意译。英文原文见 [Project Euler Problem 918](https://projecteuler.net/problem=918)；抓取底稿见 `statement.en.md`。

序列 $a_n$ 定义为 $a_1=1$，且对于 $n\ge 1$ 满足递推关系：

$$
\begin{aligned}
a_{2n} &= 2a_n \\
a_{2n+1} &= a_n - 3a_{n+1}
\end{aligned}
$$

前十项依次为：$1, 2, -5, 4, 17, -10, -17, 8, -47, 34$。
定义：

$$
S(N) = \sum_{n=1}^N a_n
$$

已知 $S(10) = -13$。

求 $S(10^{12})$。
