# 783 · 瓮

> 中文意译。英文原文见 [Project Euler Problem 783](https://projecteuler.net/problem=783)；抓取底稿见 `statement.en.md`。

给定两个正整数 $n$ 和 $k$，初始有一个装有 $kn$ 个白球的瓮。接着进行 $n$ 轮，每轮先向瓮中加入 $k$ 个黑球，再随机取出 $2k$ 个球。

记 $B_t(n,k)$ 为第 $t$ 轮取出的黑球数目。

进一步定义 $E(n,k)$ 为 $\displaystyle \sum_{t=1}^n B_t(n,k)^2$ 的期望。

已知 $E(2,2) = 9.6$。

求 $E(10^6,10)$，答案四舍五入到最接近的整数。
