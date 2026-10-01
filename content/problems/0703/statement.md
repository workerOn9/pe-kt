# 703 · 循环逻辑 II

> 中文意译。英文原文见 [Project Euler Problem 703](https://projecteuler.net/problem=703)；抓取底稿见 `statement.en.md`。

给定整数 $n$（$n \geq 3$），设 $B=\{\mathrm{false},\mathrm{true}\}$，$B^n$ 为取值于 $B$ 的 $n$ 元序列的集合。由 $B^n$ 到 $B^n$ 的函数 $f$ 定义为 $f(b_1 \dots b_n) = c_1 \dots c_n$，其中：

对 $1 \leq i < n$ 有 $c_i = b_{i+1}$。
$c_n = b_1 \;\mathrm{AND}\; (b_2 \;\mathrm{XOR}\; b_3)$，其中 $\mathrm{AND}$ 和 $\mathrm{XOR}$ 是逻辑 $\mathrm{AND}$ 和异或运算。

记 $S(n)$ 为满足如下条件的由 $B^n$ 到 $B$ 的函数 $T$ 的个数：对所有 $x \in B^n$，$T(x) ~\mathrm{AND}~ T(f(x)) = \mathrm{false}$。已知 $S(3) = 35$，$S(4) = 2118$。

求 $S(20)$，答案对 $1\,001\,001\,011$ 取模。
