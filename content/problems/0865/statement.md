# 865 · 三连数字

> 中文意译。英文原文见 [Project Euler Problem 865](https://projecteuler.net/problem=865)；抓取底稿见 `statement.en.md`。

若一个正整数在反复移除相邻三个相同数位后，其所有数位最终能被全部消除，则称该正整数为一个**三连数字**（triplicate number）。

例如，整数 $122555211$ 是一个三连数字：

$$
122{\color{red}555}211 \rightarrow 1{\color{red}222}11 \rightarrow {\color{red}111} \rightarrow \varnothing
$$

相反，$663633$ 和 $9990$ 都不是三连数字。

设 $T(n)$ 为小于 $10^n$ 的三连数字的个数。

已知 $T(6) = 261$，$T(30) = 5576195181577716$。

求 $T(10^4) \bmod 998244353$。
