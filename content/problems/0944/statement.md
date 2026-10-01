# 944 · 元约数之和

> 中文意译。英文原文见 [Project Euler Problem 944](https://projecteuler.net/problem=944)；抓取底稿见 `statement.en.md`。

给定正整数集合 $E$，若 $E$ 中的元素 $x$ 能整除 $E$ 中的另一个元素，则称 $x$ 为 $E$ 的一个**元约数**（element divisor，简称 elevisor）。

记 $E$ 的所有元约数之和为 $\operatorname{sev}(E)$。
例如，$\operatorname{sev}(\{1, 2, 5, 6\}) = 1 + 2 = 3$（因为 1 能整除 2、5、6，2 能整除 6，而 5 和 6 不能整除集合中的其它元素）。

设 $S(n)$ 为 $\{1, 2, \dots, n\}$ 的所有子集 $E$ 的 $\operatorname{sev}(E)$ 之和。
已知 $S(10) = 4927$。

求 $S(10^{14}) \bmod 1234567891$。
