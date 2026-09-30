# 375 · 子序列的最小值

> 中文意译。英文原文见 [Project Euler Problem 375](https://projecteuler.net/problem=375)；抓取底稿见 `statement.en.md`。

用如下伪随机数生成器产生整数序列 $S_n$：

$$
S_0 = 290797 , \qquad S_{n+1} = S_n^2 \bmod 50515093
$$

记 $A(i,j)$ 为 $S_i, S_{i+1}, \dots, S_j$ 中的最小值（$i \le j$）。

记 $M(N) = \displaystyle\sum_{1 \le i \le j \le N} A(i,j)$。

可以验证 $M(10)=432256955$，$M(10\,000)=3264567774119$。

求 $M(2\,000\,000\,000)$。
