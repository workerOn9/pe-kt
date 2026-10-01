# 877 · 异或方程 A

> 中文意译。英文原文见 [Project Euler Problem 877](https://projecteuler.net/problem=877)；抓取底稿见 `statement.en.md`。

记 $x \oplus y$ 为 $x$ 和 $y$ 的按位异或运算。
定义 $x$ 和 $y$ 的**异或乘积**（XOR-product），记作 $x \otimes y$，其计算类似于二进制下的长乘法，只是中间相加的结果采用按位异或而非普通的整数加法。

例如 $7 \otimes 3 = 9$，在二进制下即 $111_2 \otimes 11_2 = 1001_2$：

$$
\begin{aligned}
&\phantom{\otimes 111} 111_2 \\
\otimes &\phantom{1111} 11_2 \\
\hline
&\phantom{\otimes 111} 111_2 \\
\oplus &\phantom{11} 111_2 \phantom{9} \\
\hline
&\phantom{\otimes 11} 1001_2
\end{aligned}
$$

考虑方程：

$$
(a \otimes a) \oplus (2 \otimes a \otimes b) \oplus (b \otimes b) = 5
$$

例如，$(a, b) = (3, 6)$ 是该方程的一个解。

设 $X(N)$ 为该方程在满足 $0 \le a \le b \le N$ 的所有解中，$b$ 值的异或和。
已知 $X(10) = 5$。

求 $X(10^{18})$。
