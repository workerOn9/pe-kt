# 778 · 大一新生之积

> 中文意译。英文原文见 [Project Euler Problem 778](https://projecteuler.net/problem=778)；抓取底稿见 `statement.en.md`。

若 $a,b$ 是两个非负整数，其十进制表示分别为 $a=(\dots a_2a_1a_0)$ 和 $b=(\dots b_2b_1b_0)$，则 $a$ 与 $b$ 的大一新生之积记作 $a\boxtimes b$，是十进制表示为 $c=(\dots c_2c_1c_0)$ 的整数 $c$，其中 $c_i$ 是 $a_i\cdot b_i$ 的末位数字。
例如，$234 \boxtimes 765 = 480$。

记 $F(R,M)$ 为对所有满足 $0\leq x_i \leq M$ 的整数数列 $(x_1,\dots,x_R)$ 求 $x_1 \boxtimes \dots \boxtimes x_R$ 之和。
例如，$F(2, 7) = 204$，$F(23, 76) \equiv 5870548 \pmod{ 1\,000\,000\,009}$。

求 $F(234567,765432)$，答案对 $1\,000\,000\,009$ 取模。
