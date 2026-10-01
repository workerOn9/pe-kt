# 913 · 行主序与列主序

> 中文意译。英文原文见 [Project Euler Problem 913](https://projecteuler.net/problem=913)；抓取底稿见 `statement.en.md`。

将数字 $1$ 到 $12$ 排列成一个 $3 \times 4$ 的矩阵，既可以按行主序排列，也可以按列主序排列：

$$
R = \begin{pmatrix} 1 & 2 & 3 & 4\\ 5 & 6 & 7 & 8\\ 9 & 10 & 11 & 12\end{pmatrix}, \quad C = \begin{pmatrix} 1 & 4 & 7 & 10\\ 2 & 5 & 8 & 11\\ 3 & 6 & 9 & 12\end{pmatrix}
$$

每次交换其中的任意两个元素，要将矩阵 $R$ 变换为矩阵 $C$，至少需要 $8$ 次交换。

设 $S(n, m)$ 为将一个包含 $1$ 到 $nm$ 的 $n \times m$ 矩阵从行主序变换为列主序所需的最少交换次数。因此 $S(3, 4) = 8$。

已知对于所有满足 $2 \le n \le m \le 100$ 的数对 $(n, m)$，$S(n, m)$ 的总和为 $12578833$。

求对于所有满足 $2 \le n \le m \le 100$ 的数对 $(n, m)$，$S(n^4, m^4)$ 的总和。
