# 323 · 随机整数的按位或

> 中文意译。英文原文见 [Project Euler Problem 323](https://projecteuler.net/problem=323)；抓取底稿见 `statement.en.md`。

设 $y_0, y_1, y_2, \dots$ 是随机无符号 $32$ 位整数序列（即 $0 \le y_i < 2^{32}$，每个取值等概率）。

序列 $x_i$ 由如下递推给出：
$$
x_0 = 0 , \qquad x_i = x_{i-1} \mid y_{i-1} \quad (i > 0) ,
$$
其中 $\mid$ 表示按位或。

可以看出最终必存在下标 $N$，使得对一切 $i \ge N$ 有 $x_i = 2^{32}-1$（二进制全为 $1$）。

求 $N$ 的期望值。结果四舍五入到小数点后 $10$ 位。
