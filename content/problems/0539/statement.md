# 539 · 奇数剔除

> 中文意译。英文原文见 [Project Euler Problem 539](https://projecteuler.net/problem=539)；抓取底稿见 `statement.en.md`。

从 $1$ 到 $n$ 的所有整数组成的有序列表开始。从左到右，删去第一个数以及其后的每隔一个数，直到列表末尾。接着从右到左重复这一过程，删去最右边的数以及剩下的数中每隔一个数。如此交替地从左到右、从右到左删去每隔一个数，直到只剩下一个数为止。

从 $n = 9$ 开始，我们有：

$$
\begin{aligned}
&\underline{1}\,2\,\underline{3}\,4\,\underline{5}\,6\,\underline{7}\,8\,\underline{9}\\
&2\,\underline{4}\,6\,\underline{8}\\
&\underline{2}\,6\\
&6
\end{aligned}
$$

记 $P(n)$ 为从长度为 $n$ 的列表开始最后剩下的数。记 $\displaystyle S(n) = \sum_{k=1}^n P(k)$。已知 $P(1)=1$，$P(9) = 6$，$P(1000)=510$，$S(1000)=268271$。

求 $S(10^{18}) \bmod 987654321$。
