# 463 · 一个古怪的递推关系

> 中文意译。英文原文见 [Project Euler Problem 463](https://projecteuler.net/problem=463)；抓取底稿见 `statement.en.md`。

函数 $f$ 在所有正整数上定义如下：

$$
\begin{aligned}
f(1) &= 1 \\
f(3) &= 3 \\
f(2n) &= f(n) \\
f(4n + 1) &= 2f(2n + 1) - f(n) \\
f(4n + 3) &= 3f(2n + 1) - 2f(n)
\end{aligned}
$$

函数 $S(n)$ 定义为 $S(n) = \sum_{i=1}^{n} f(i)$。

已知 $S(8) = 22$，$S(100) = 3604$。

求 $S(3^{37})$。请给出答案的最后 $9$ 位数字。
