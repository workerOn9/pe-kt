# 751 · 连接巧合

> 中文意译。英文原文见 [Project Euler Problem 751](https://projecteuler.net/problem=751)；抓取底稿见 `statement.en.md`。

一个非递减的整数序列 $a_n$ 可以由任意正实数 $\theta$ 通过以下过程生成：

$$
\begin{aligned}
b_1 &= \theta \\
b_n &= \left\lfloor b_{n-1} \right\rfloor \left(b_{n-1} - \left\lfloor b_{n-1} \right\rfloor + 1\right)~~~\forall ~ n \geq 2 \\
a_n &= \left\lfloor b_{n} \right\rfloor
\end{aligned}
$$

其中 $\left\lfloor \cdot \right\rfloor$ 是下取整函数。

例如 $\theta=2.956938891377988...$ 生成斐波那契数列：$2, 3, 5, 8, 13, 21, 34, 55, 89, ...$

正整数序列 $a_n$ 的连接是一个实数，记作 $\tau$，它由该序列的各元素从小数点后开始依次拼接而成，起始于 $a_1$：$a_1.a_2a_3a_4...$

例如，由 $\theta=2.956938891377988...$ 构造的斐波那契数列给出连接 $\tau=2.3581321345589...$ 显然，对该 $\theta$ 有 $\tau \neq \theta$。

求唯一使生成序列以 $a_1=2$ 开始、且生成序列的连接等于原值（即 $\tau = \theta$）的 $\theta$，答案四舍五入到小数点后 $24$ 位。
