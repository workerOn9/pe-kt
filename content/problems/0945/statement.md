# 945 · 异或方程 C

> 中文意译。英文原文见 [Project Euler Problem 945](https://projecteuler.net/problem=945)；抓取底稿见 `statement.en.md`。

我们用 $x \oplus y$ 表示 $x$ 与 $y$ 的按位异或（XOR）。
定义 $x$ 和 $y$ 的**无进位异或乘积**（XOR-product）$x \otimes y$：其运算过程类似于二进制竖式乘法，但在中间步骤累加各移位项时采用按位异或代替通常的整数加法。
例如 $7 \otimes 3 = 9$，用二进制表示即 $111_2 \otimes 11_2 = 1001_2$：

$$
\begin{aligned}
\phantom{\otimes 111} 111_2 \\
\otimes \phantom{1111} 11_2 \\
\hline
\phantom{\otimes 111} 111_2 \\
\oplus \phantom{11} 111_2 \phantom{9} \\
\hline
\phantom{\otimes 11} 1001_2
\end{aligned}
$$

考虑方程：

$$
(a \otimes a) \oplus (2 \otimes a \otimes b) \oplus (b \otimes b) = c \otimes c
$$

例如，$(a, b, c) = (1, 2, 1)$ 是该方程的一个解，$(1, 8, 13)$ 也是该方程的一个解。

设 $F(N)$ 为满足 $0 \le a \le b \le N$ 的解 $(a, b, c)$ 的组数。已知 $F(10) = 21$。

求 $F(10^7)$。
