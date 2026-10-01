# 813 · 异或幂

> 中文意译。英文原文见 [Project Euler Problem 813](https://projecteuler.net/problem=813)；抓取底稿见 `statement.en.md`。

我们用 $x\oplus y$ 表示 $x$ 与 $y$ 的按位异或。

定义 $x$ 与 $y$ 的异或乘积，记作 $x \otimes y$，它类似于二进制下的竖式乘法，只是中间结果用异或而非通常的整数加法相加。

例如 $11 \otimes 11 = 69$，即二进制下 $1011_2 \otimes 1011_2 = 1000101_2$：

$$
 \begin{aligned} \phantom{\otimes 1111} 1011_2 \\ \otimes \phantom{1111} 1011_2 \\ \hline \phantom{\otimes 1111} 1011_2 \\ \phantom{\otimes 111} 1011_2 \phantom{9} \\ \oplus \phantom{1} 1011_2  \phantom{999} \\ \hline \phantom{\otimes 11} 1000101_2 \\ \end{aligned}
$$

进一步定义 $P(n) = 11^{\otimes n} = \overbrace{11\otimes 11\otimes \ldots \otimes 11}^n$。例如 $P(2)=69$。

求 $P(8^{12}\cdot 12^8)$，答案对 $10^9+7$ 取模。
