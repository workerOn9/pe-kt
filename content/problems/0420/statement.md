# 420 · 正整数矩阵

> 中文意译。英文原文见 [Project Euler Problem 420](https://projecteuler.net/problem=420)；抓取底稿见 `statement.en.md`。

**正整数矩阵**指所有元素都是正整数的矩阵。
有些正整数矩阵可以用两种不同的方式表示为某个正整数矩阵的平方。下面是一个例子：

$$
\begin{pmatrix} 40 & 12 \\ 48 & 40 \end{pmatrix} = \begin{pmatrix} 2 & 3 \\ 12 & 2 \end{pmatrix}^2 = \begin{pmatrix} 6 & 1 \\ 4 & 6 \end{pmatrix}^2
$$

我们定义 $F(N)$ 为迹小于 $N$、且能用两种不同方式表示为正整数矩阵之平方的 $2\times 2$ 正整数矩阵的个数。
可以验证 $F(50) = 7$，$F(1000) = 1019$。

求 $F(10^7)$。
