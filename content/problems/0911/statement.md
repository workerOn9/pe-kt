# 911 · 辛钦异常数

> 中文意译。英文原文见 [Project Euler Problem 911](https://projecteuler.net/problem=911)；抓取底稿见 `statement.en.md`。

一个无理数 $x$ 可以唯一表示为连分数 $[a_0; a_1,a_2,a_3,\dots]$：

$$
x = a_{0} + \cfrac{1}{a_1 + \cfrac{1}{a_2 + \cfrac{1}{a_3 + {\ddots}}}}
$$

其中 $a_0$ 为整数，而 $a_1,a_2,a_3,\dots$ 为正整数。

定义 $k_j(x)$ 为前 $j$ 项部分商 $a_1, a_2, \dots, a_j$ 的几何平均值，即 $k_j(x) = (a_1 a_2 \cdots a_j)^{1/j}$。
再定义其极限 $k_\infty(x) = \lim_{j\to \infty} k_j(x)$。

辛钦（Khinchin）证明了：几乎所有无理数 $x$ 都具有相同的 $k_\infty(x) \approx 2.685452\dots$，该数值被称为辛钦常数。然而，也有一些数不满足该定理（例外数）。

对于 $n\ge 0$，定义：

$$
\rho_n = \sum_{i=0}^{\infty} \frac{2^n}{2^{2^i}}
$$

例如 $\rho_2$ 的连分数展开以 $[3; 3, 1, 3, 4, 3, 1, 3, \dots]$ 开头，其极限值为 $k_\infty(\rho_2) \approx 2.059767$。

求所有 $0\le n\le 50$ 对应的 $k_\infty(\rho_n)$ 的几何平均值。答案四舍五入保留小数点后六位。
