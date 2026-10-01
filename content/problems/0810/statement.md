# 810 · 异或素数

> 中文意译。英文原文见 [Project Euler Problem 810](https://projecteuler.net/problem=810)；抓取底稿见 `statement.en.md`。

我们用 $x\oplus y$ 表示 $x$ 与 $y$ 的按位异或。

定义 $x$ 与 $y$ 的异或乘积，记作 $x \otimes y$，它类似于二进制下的竖式乘法，只是中间结果用异或而非通常的整数加法相加。

例如 $7 \otimes 3 = 9$，即二进制下 $111_2 \otimes 11_2 = 1001_2$：

$$
 \begin{aligned} \phantom{\otimes 111} 111_2 \\ \otimes \phantom{1111} 11_2 \\ \hline \phantom{\otimes 111} 111_2 \\ \oplus \phantom{11} 111_2  \phantom{9} \\ \hline \phantom{\otimes 11} 1001_2 \\ \end{aligned}
$$

异或素数是大于 $1$ 且不能表示为两个大于 $1$ 的整数的异或乘积的整数。上面的例子表明 $9$ 不是异或素数。类似地，$5 = 3 \otimes 3$ 也不是异或素数。前几个异或素数为 $2, 3, 7, 11, 13, ...$，第 10 个异或素数是 $41$。

求第 $5\,000\,000$ 个异或素数。
