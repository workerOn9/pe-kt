# 330 · 欧拉常数递推

> 中文意译。英文原文见 [Project Euler Problem 330](https://projecteuler.net/problem=330)；抓取底稿见 `statement.en.md`。

实数序列 $a(n)$ 对一切整数 $n$ 定义如下：
$$
a(n) = \begin{cases} 1 & n < 0 \\ \displaystyle\sum_{i=1}^{\infty} \frac{a(n-i)}{i!} & n \ge 0 \end{cases}
$$

例如：
$$
\begin{aligned}
a(0) &= \frac{1}{1!} + \frac{1}{2!} + \frac{1}{3!} + \cdots = e - 1 \\
a(1) &= \frac{e-1}{1!} + \frac{1}{2!} + \frac{1}{3!} + \cdots = 2e - 3 \\
a(2) &= \frac{2e-3}{1!} + \frac{e-1}{2!} + \frac{1}{3!} + \cdots = \frac{7}{2}e - 6
\end{aligned}
$$
其中 $e = 2.7182818\dots$ 为自然对数的底。

可以证明，$a(n)$ 总能写成 $\dfrac{A(n)e + B(n)}{n!}$ 的形式，其中 $A(n)$ 与 $B(n)$ 均为整数。

例如 $a(10) = \dfrac{328161643e - 652694486}{10!}$。

求 $A(10^9) + B(10^9)$ 对 $77\,777\,777$ 取模的结果。
