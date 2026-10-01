# 921 · 黄金递推

> 中文意译。英文原文见 [Project Euler Problem 921](https://projecteuler.net/problem=921)；抓取底稿见 `statement.en.md`。

考虑如下递推关系：

$$
\begin{aligned}
a_0 &= \frac{\sqrt{5} + 1}{2} \\
a_{n+1} &= \frac{a_n(a_n^4 + 10a_n^2 + 5)}{5a_n^4 + 10a_n^2 + 1}
\end{aligned}
$$

注意到 $a_0$ 即为黄金分割比。

$a_n$ 总能写成如下形式：

$$
a_n = \frac{p_n\sqrt{5} + 1}{q_n}
$$

其中 $p_n$ 和 $q_n$ 为正整数。

记 $s(n) = p_n^5 + q_n^5$。因此 $s(0) = 1^5 + 2^5 = 33$。

斐波那契数列定义为：$F_1 = 1$，$F_2 = 1$，且当 $n > 2$ 时 $F_n = F_{n-1} + F_{n-2}$。

定义：

$$
S(m) = \sum_{i=2}^{m} s(F_i)
$$

求 $S(1618034)$ 模 $398874989$ 的余数。
