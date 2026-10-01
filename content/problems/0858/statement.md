# 858 · 子集最小公倍数之和

> 中文意译。英文原文见 [Project Euler Problem 858](https://projecteuler.net/problem=858)；抓取底稿见 `statement.en.md`。

定义

$$
G(N) = \sum_S \operatorname{lcm}(S)
$$

其中 $S$ 遍历集合 $\{1, \dots, N\}$ 的所有子集，$\operatorname{lcm}$ 表示最小公倍数。规定空集的最小公倍数为 $1$。

已知 $G(5) = 528$，$G(20) = 8463108648960$。

求 $G(800) \bmod (10^9 + 7)$。
