# 535 · 分形序列

> 中文意译。英文原文见 [Project Euler Problem 535](https://projecteuler.net/problem=535)；抓取底稿见 `statement.en.md`。

考虑以如下开头的一个无穷整数序列 $S$：

$S = 1, 1, 2, 1, 3, 2, 4, 1, 5, 3, 6, 2, 7, 8, 4, 9, 1, 10, 11, 5, \dots$

圈出每个整数首次出现的位置：

$S = \underline{1}, 1, \underline{2}, 1, \underline{3}, 2, \underline{4}, 1, \underline{5}, 3, \underline{6}, 2, \underline{7}, \underline{8}, 4, \underline{9}, 1, \underline{10}, \underline{11}, 5, \dots$

该序列具有以下性质：

- 被圈出的数是从 $1$ 开始的连续整数。
- 紧接在每个未被圈出的数 $a_i$ 之前，恰好有 $\lfloor \sqrt{a_i} \rfloor$ 个相邻的被圈出的数，其中 $\lfloor \cdot \rfloor$ 是向下取整函数。
- 若去掉所有被圈出的数，剩下的数构成的序列与 $S$ 完全相同，因此 $S$ 是一个分形序列。

记 $T(n)$ 为序列前 $n$ 项之和。已知 $T(1) = 1$，$T(20) = 86$，$T(10^3) = 364089$，$T(10^9) = 498676527978348241$。

求 $T(10^{18})$，给出答案的后 $9$ 位数字。
