# 782 · 不同的行与列

> 中文意译。英文原文见 [Project Euler Problem 782](https://projecteuler.net/problem=782)；抓取底稿见 `statement.en.md`。

一个 $n\times n$ 二进制矩阵的复杂度是其不同的行与列的总数。

例如，考虑 $3\times 3$ 矩阵

$$
  \mathbf{A} = \begin{pmatrix} 1&0&1\\0&0&0\\1&0&1\end{pmatrix} \quad   \mathbf{B} = \begin{pmatrix} 0&0&0\\0&0&0\\1&1&1\end{pmatrix}
$$

$\mathbf{A}$ 的复杂度是 $2$，因为其行与列的集合是 $\{000,101\}$。$\mathbf{B}$ 的复杂度是 $3$，因为其行与列的集合是 $\{000,001,111\}$。

对于 $0 \le k \le n^2$，记 $c(n, k)$ 为恰有 $k$ 个 $1$ 的 $n\times n$ 二进制矩阵的最小复杂度。

设

$$
C(n) = \sum_{k=0}^{n^2} c(n, k)
$$

例如，$C(2) = c(2, 0) + c(2, 1) + c(2, 2) + c(2, 3) + c(2, 4) = 1 + 2 + 2 + 2 + 1 = 8$。
已知 $C(5) = 64$，$C(10) = 274$，$C(20) = 1150$。

求 $C(10^4)$。
