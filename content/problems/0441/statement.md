# 441 · 互素对的逆求和

> 中文意译。英文原文见 [Project Euler Problem 441](https://projecteuler.net/problem=441)；抓取底稿见 `statement.en.md`。

对整数 $M$，定义 $R(M)$ 为所有满足以下全部条件的整数对 $p$、$q$ 的 $1/(p \cdot q)$ 之和：

- $1 \leq p \lt q \leq M$；
- $p + q \geq M$；
- $p$ 与 $q$ 互素。

又定义 $S(N)$ 为 $R(i)$ 对 $2 \leq i \leq N$ 的和。
可以验证 $S(2) = R(2) = 1/2$，$S(10) \approx 6.9147$，$S(100) \approx 58.2962$。

求 $S(10^7)$。答案保留四位小数。
