# 969 · 袋鼠跳跃

> 中文意译。英文原文见 [Project Euler Problem 969](https://projecteuler.net/problem=969)；抓取底稿见 `statement.en.md`。

一只袋鼠从实数轴的原点 $0$ 出发，沿着正方向跳跃。每次跳跃的距离都在区间 $[0, 1]$ 上独立均匀随机。设 $H(n)$ 为袋鼠在实数轴上跨过 $n$ 所需的期望跳跃次数。

记 $\alpha = H(1)$，则对所有正整数 $n$，$H(n)$ 均可表示为关于 $\alpha$ 且系数为有理数的多项式函数。例如 $H(3)=\alpha^3-2\alpha^2+\frac{1}{2}\alpha$。定义 $S(n)$ 为 $H(n)$ 的多项式形式中所有整数系数之和。因此 $S(1)=1$ 且 $S(3)=1+(-2)=-1$。
已知 $\displaystyle \sum_{n=1}^{10} S(n)=43$。

求 $\displaystyle\sum_{n=1}^{10^{18}} S(n)$ 对 $10^9+7$ 取模的结果。
