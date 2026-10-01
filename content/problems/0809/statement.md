# 809 · 有理递推关系

> 中文意译。英文原文见 [Project Euler Problem 809](https://projecteuler.net/problem=809)；抓取底稿见 `statement.en.md`。

下式定义了一个对所有正有理数 $x$ 都有定义的函数。

$$
f(x)=\begin{cases} x  &x\text{ 为整数}\\      f(\frac 1{1-x}) &x \lt 1\\      f\Big(\frac 1{\lceil x\rceil -x}-1+f(x-1)\Big) &\text{其他情形}\end{cases}
$$

例如，$f(3/2)=3$，$f(1/6) = 65533$，$f(13/10) = 7625597484985$。

求 $f(22/7)$，答案对 $10^{15}$ 取模。
