# 803 · 伪随机序列

> 中文意译。英文原文见 [Project Euler Problem 803](https://projecteuler.net/problem=803)；抓取底稿见 `statement.en.md`。

Rand48 是某些编程语言使用的伪随机数生成器。给定任意整数 $0 \le a_0 < 2^{48}$，它按规则 $a_n = (25214903917 \cdot a_{n - 1} + 11) \bmod 2^{48}$ 生成一个序列。

令 $b_n = \lfloor a_n / 2^{16} \rfloor \bmod 52$。序列 $b_0, b_1, \dots$ 按如下规则转换为无限字符串 $c = c_0c_1\dots$：
$0 \rightarrow$ a，$1\rightarrow$ b，$\dots$，$25 \rightarrow$ z，$26 \rightarrow$ A，$27 \rightarrow$ B，$\dots$，$51 \rightarrow$ Z。

例如，若取 $a_0 = 123456$，则字符串 $c$ 以 "bQYicNGCY$\dots$" 开头。此外，从下标 $100$ 开始，我们首次遇到子串 "RxqLBfWzv"。

又如，若 $c$ 以 "EULERcats$\dots$" 开头，则 $a_0$ 必为 $78580612777175$。

现假设字符串 $c$ 以 "PuzzleOne$\dots$" 开头。求子串 "LuckyText" 在 $c$ 中首次出现的起始下标。
