# 433 · 欧几里得算法的步数

> 中文意译。英文原文见 [Project Euler Problem 433](https://projecteuler.net/problem=433)；抓取底稿见 `statement.en.md`。

设 $E(x_0, y_0)$ 为用欧几里得算法确定 $x_0$ 与 $y_0$ 的最大公约数所需的步数。更形式化地说：

$$
x_1 = y_0, \quad y_1 = x_0 \bmod y_0
$$

$$
x_n = y_{n-1}, \quad y_n = x_{n-1} \bmod y_{n-1}
$$

$E(x_0, y_0)$ 是使得 $y_n = 0$ 的最小 $n$。

我们有 $E(1,1) = 1$，$E(10,6) = 3$，$E(6,10) = 4$。

定义 $S(N)$ 为 $E(x,y)$ 之和，其中 $1 \leq x, y \leq N$。

我们有 $S(1) = 1$，$S(10) = 221$，$S(100) = 39826$。

求 $S(5\cdot 10^6)$。
