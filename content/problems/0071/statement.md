# 有序分数

> 中文意译。英文原文见 [Project Euler Problem 71](https://projecteuler.net/problem=71)；抓取底稿见 `statement.en.md`。

考虑分数 $\dfrac n d$，其中 $n$、$d$ 都是正整数。若 $n < d$ 且 $\operatorname{HCF}(n,d)=1$（即 $\gcd(n,d)=1$），则称它是一个**既约真分数**。

把所有 $d \le 8$ 的既约真分数按大小升序列出，得到：

$$\frac 1 8,\ \frac 1 7,\ \frac 1 6,\ \frac 1 5,\ \frac 1 4,\ \frac 2 7,\ \frac 1 3,\ \frac 3 8,\ \mathbf{\frac 2 5},\ \frac 3 7,\ \frac 1 2,\ \frac 4 7,\ \frac 3 5,\ \frac 5 8,\ \frac 2 3,\ \frac 5 7,\ \frac 3 4,\ \frac 4 5,\ \frac 5 6,\ \frac 6 7,\ \frac 7 8$$

可以看到 $\dfrac 2 5$ 恰好是紧挨在 $\dfrac 3 7$ 左边的那个分数。

现在把所有 $d \le 1\,000\,000$ 的既约真分数按大小升序排列，求紧挨在 $\dfrac 3 7$ 左边的那个分数的**分子**。
