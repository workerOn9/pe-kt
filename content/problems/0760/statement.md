# 760 · 按位算子求和

> 中文意译。英文原文见 [Project Euler Problem 760](https://projecteuler.net/problem=760)；抓取底稿见 `statement.en.md`。

定义

$$
\displaystyle g(m,n) = (m\oplus n)+(m\vee n)+(m\wedge n)
$$

其中 $\oplus, \vee, \wedge$ 分别是按位异或、或、与运算。

再令

$$
\displaystyle G(N) = \sum_{n=0}^N\sum_{k=0}^n g(k,n-k)
$$

例如 $G(10) = 754$，$G(10^2) = 583766$。

求 $G(10^{18})$，答案对 $1\,000\,000\,007$ 取模。
