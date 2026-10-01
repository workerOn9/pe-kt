# 533 · 卡迈克尔函数的最小值

> 中文意译。英文原文见 [Project Euler Problem 533](https://projecteuler.net/problem=533)；抓取底稿见 `statement.en.md`。

卡迈克尔函数 $\lambda(n)$ 定义为最小的正整数 $m$，使得对一切与 $n$ 互素的整数 $a$ 都有 $a^m = 1 \pmod n$。例如 $\lambda(8) = 2$，$\lambda(240) = 4$。

定义 $L(n)$ 为最小的正整数 $m$，使得对所有 $k \ge m$ 都有 $\lambda(k) \ge n$。

例如 $L(6) = 241$，$L(100) = 20\,174\,525\,281$。

求 $L(20\,000\,000)$，给出答案的后 $9$ 位数字。
