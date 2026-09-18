# 122 · 高效幂运算

计算 $n^{15}$ 最朴素的方法需要十四次乘法：

$$
n \times n \times \cdots \times n = n^{15}.
$$

但用「二进制」方法只要六次乘法：

$$
\begin{aligned}
n \times n &= n^2\\
n^2 \times n^2 &= n^4\\
n^4 \times n^4 &= n^8\\
n^8 \times n^4 &= n^{12}\\
n^{12} \times n^2 &= n^{14}\\
n^{14} \times n &= n^{15}
\end{aligned}
$$

不过还能只用五次乘法：

$$
\begin{aligned}
n \times n &= n^2\\
n^2 \times n &= n^3\\
n^3 \times n^3 &= n^6\\
n^6 \times n^6 &= n^{12}\\
n^{12} \times n^3 &= n^{15}
\end{aligned}
$$

我们定义 $m(k)$ 为计算 $n^k$ 所需的最少乘法次数，例如 $m(15) = 5$。

求 $\sum\limits_{k = 1}^{200} m(k)$。

（题面原文：[projecteuler.net/problem=122](https://projecteuler.net/problem=122)）
