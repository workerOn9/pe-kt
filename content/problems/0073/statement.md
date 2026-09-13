# 区间内的分数计数

> 中文意译。英文原文见 [Project Euler Problem 73](https://projecteuler.net/problem=73)；抓取底稿见 `statement.en.md`。

考虑分数 $\dfrac n d$，其中 $n$ 与 $d$ 都是正整数。若 $n < d$ 且 $\operatorname{HCF}(n,d)=1$，则称 $\dfrac n d$ 为一个**既约真分数**。

把 $d \le 8$ 的全部既约真分数按从小到大排列，得到：

$$\frac 1 8, \frac 1 7, \frac 1 6, \frac 1 5, \frac 1 4, \frac 2 7, \frac 1 3, \mathbf{\frac 3 8, \frac 2 5, \frac 3 7}, \frac 1 2, \frac 4 7, \frac 3 5, \frac 5 8, \frac 2 3, \frac 5 7, \frac 3 4, \frac 4 5, \frac 5 6, \frac 6 7, \frac 7 8$$

可以看到，位于 $\dfrac 1 3$ 与 $\dfrac 1 2$ 之间的分数有 $3$ 个。

当 $d \le 12\,000$ 时，这个排好序的既约真分数集合中，位于 $\dfrac 1 3$ 与 $\dfrac 1 2$ 之间的分数有多少个？
