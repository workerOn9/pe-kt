# 995 · 一对特殊的多项式

> 中文意译。英文原文见 [Project Euler Problem 995](https://projecteuler.net/problem=995)；抓取底稿见 `statement.en.md`。

对于每个质数 $p$ 和每个正整数 $n$，定义如下两个多项式：

$$
\begin{aligned}
f_p(x) &= \sum_{i=0}^{p-1}x^i \\
g_n(x) &= 1+\sum_{d\mid n}x^d
\end{aligned}
$$

设 $S(p)$ 为使得 $f_p(x)$ 整除 $g_s(x)$ 的最小正整数 $s$。例如，由于 $f_2(x)=g_1(x)$，故 $S(2)=1$。另外 $S(5)=8$，因为 $f_5(x)\cdot(x^4-x^3+1)=g_8(x)$。

定义 $T(m)$ 为对于所有满足 $p < m$ 的质数 $p$ 的 $S(p)$ 的乘积。已知 $T(20)=1348422598656$ 以及 $T(100)\approx 1.37451\text{e}123$。

求 $T(20\,000)$，用科学记数法给出答案并四舍五入保留小数点后五位有效数字。使用小写字母 e 分隔尾数和指数。
