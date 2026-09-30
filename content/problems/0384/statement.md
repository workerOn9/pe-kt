# 384 · Rudin-Shapiro 数列

> 中文意译。英文原文见 [Project Euler Problem 384](https://projecteuler.net/problem=384)；抓取底稿见 `statement.en.md`。

记 $a(n)$ 为 $n$ 的二进制展开中相邻「1」的个数（允许重叠）。例如 $a(5)=a(101_2)=0$，$a(6)=a(110_2)=1$，$a(7)=a(111_2)=2$。

再定义 $b(n) = (-1)^{a(n)}$，该数列称为 **Rudin-Shapiro 数列**。

考虑 $b(n)$ 的前缀和数列 $s(n) = \displaystyle\sum_{i=0}^{n} b(i)$。这些数列的前若干项为：

$$
\begin{array}{c|cccccccc}
n & 0 & 1 & 2 & 3 & 4 & 5 & 6 & 7 \\
\hline
a(n) & 0 & 0 & 0 & 1 & 0 & 0 & 1 & 2 \\
b(n) & 1 & 1 & 1 & -1 & 1 & 1 & -1 & 1 \\
s(n) & 1 & 2 & 3 & 2 & 3 & 4 & 3 & 4
\end{array}
$$

数列 $s(n)$ 有一个显著性质：它的所有项都是正数，且每个正整数 $k$ 恰好出现 $k$ 次。

对 $1 \le c \le t$，定义 $g(t,c)$ 为 $s(n)$ 中 $t$ 第 $c$ 次出现时 $n$ 的下标。例如 $g(3,3)=6$，$g(4,2)=7$，$g(54321,12345)=1220847710$。

令 $F(n)$ 为斐波那契数列：$F(0)=F(1)=1$，$F(n)=F(n-1)+F(n-2)$（$n>1$）。

定义 $GF(t) = g\bigl(F(t),\, F(t-1)\bigr)$。

求 $\displaystyle\sum_{t=2}^{45} GF(t)$。
