# 乘积和数

> 中文意译。英文原文见 [Project Euler Problem 88](https://projecteuler.net/problem=88)；抓取底稿见 `statement.en.md`。

一个自然数 $N$，若能表示成某组**至少两个**自然数 $\{a_1, a_2, \dots, a_k\}$ 之和，同时又等于它们的乘积，就称为乘积和数：

$$N = a_1 + a_2 + \cdots + a_k = a_1 \times a_2 \times \cdots \times a_k.$$

例如 $6 = 1 + 2 + 3 = 1 \times 2 \times 3$。

对给定的元素个数 $k$，把具有该性质的最小 $N$ 称为**最小乘积和数**。$k = 2, 3, 4, 5, 6$ 时依次为：

- $k=2$：$4 = 2 \times 2 = 2 + 2$
- $k=3$：$6 = 1 \times 2 \times 3 = 1 + 2 + 3$
- $k=4$：$8 = 1 \times 1 \times 2 \times 4 = 1 + 1 + 2 + 4$
- $k=5$：$8 = 1 \times 1 \times 2 \times 2 \times 2 = 1 + 1 + 2 + 2 + 2$
- $k=6$：$12 = 1 \times 1 \times 1 \times 1 \times 2 \times 6 = 1 + 1 + 1 + 1 + 2 + 6$

因此 $2 \le k \le 6$ 的最小乘积和数之和为 $4+6+8+12 = 30$——注意 $8$ 在求和时只计一次。

事实上 $2 \le k \le 12$ 的全部最小乘积和数构成集合 $\{4, 6, 8, 12, 15, 16\}$，其和为 $61$。

求 $2 \le k \le 12000$ 的全部最小乘积和数之和。
