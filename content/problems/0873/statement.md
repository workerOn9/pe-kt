# 873 · 带间隔的单词

> 中文意译。英文原文见 [Project Euler Problem 873](https://projecteuler.net/problem=873)；抓取底稿见 `statement.en.md`。

设 $W(p, q, r)$ 为由 $p$ 个字母 A、$q$ 个字母 B 和 $r$ 个字母 C 组成的满足以下条件的单词数量：每个字母 A 与每个字母 B 之间必须由至少两个字母 C 分隔开。例如，CACACCBB 是 $W(2, 2, 4)$ 的一个合法单词，但 ACBCACBC 不是。

已知 $W(2, 2, 4) = 32$，$W(4, 4, 44) = 13908607644$。

求 $W(10^6, 10^7, 10^8) \bmod 1\,000\,000\,007$。
