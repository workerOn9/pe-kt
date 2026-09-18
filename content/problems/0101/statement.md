# 101 · 最优多项式

只给出一个数列的前 $k$ 项时，我们无法确定下一项的值——因为有无限多个多项式函数都能生成这段数列。

以立方数列为例，它的生成函数是 $u_n = n^3$：$1, 8, 27, 64, 125, 216, \dots$

假设只给出该数列的前两项。依照「越简单越好」的原则，我们应当假设二者成线性关系，并预测下一项为 $15$（公差为 $7$）。即使给出的是前三项，同样出于最简原则，也应当假设二次关系。

定义 $\operatorname{OP}(k, n)$ 为「由数列前 $k$ 项确定的最优多项式生成函数」的第 $n$ 项。显然 $\operatorname{OP}(k, n)$ 对 $n \le k$ 能准确生成数列的各项，而第一个错误项（FIT，first incorrect term）可能是 $\operatorname{OP}(k, k+1)$；此时称它为坏的最优多项式（BOP，bad OP）。

作为基础约定：若只给出数列的第一项，最合理的假设是常数列，即对 $n \ge 2$ 有 $\operatorname{OP}(1, n) = u_1$。

于是对立方数列可得到如下最优多项式：

$$
\begin{aligned}
\operatorname{OP}(1, n) &= 1 &&\rightarrow 1, \mathbf{1}, 1, 1, \dots \\
\operatorname{OP}(2, n) &= 7n - 6 &&\rightarrow 1, 8, \mathbf{15}, \dots \\
\operatorname{OP}(3, n) &= 6n^2 - 11n + 6 &&\rightarrow 1, 8, 27, \mathbf{58}, \dots \\
\operatorname{OP}(4, n) &= n^3 &&\rightarrow 1, 8, 27, 64, 125, \dots
\end{aligned}
$$

其中加粗的数字是各条 $\operatorname{OP}$ 的第一个错误项。显然 $k \ge 4$ 时不存在 BOP。

把上述 BOP 产生的 FIT 相加，得到 $1 + 15 + 58 = 74$。

现在考虑下面这个十次多项式生成函数：

$$
u_n = 1 - n + n^2 - n^3 + n^4 - n^5 + n^6 - n^7 + n^8 - n^9 + n^{10}.
$$

求所有 BOP 所产生的 FIT 之和。

（题面原文：[projecteuler.net/problem=101](https://projecteuler.net/problem=101)）
