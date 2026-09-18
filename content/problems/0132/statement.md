# 大循环单位数的素因子

> 中文意译。英文原文见 [Project Euler Problem 132](https://projecteuler.net/problem=132)；抓取底稿见 `statement.en.md`。

全部由数字 $1$ 组成的数称为循环单位数（repunit）。记 $R(k)$ 为长度恰为 $k$ 的循环单位数，也就是

$$
R(k) = \underbrace{11\ldots 1}_{k\text{ 个}1} = \frac{10^k-1}{9}.
$$

例如 $R(10) = 1111111111 = 11 \times 41 \times 271 \times 9091$，这些素因子之和为 $9414$。

求 $R(10^9)$ 最小的四十个素因子之和。
