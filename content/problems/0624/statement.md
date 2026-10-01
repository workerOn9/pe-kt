# 624 · 两个正面胜过一个

> 中文意译。英文原文见 [Project Euler Problem 624](https://projecteuler.net/problem=624)；抓取底稿见 `statement.en.md`。

反复抛一枚公平硬币，直到出现连续两个正面。假设它们出现在第 $(M-1)$ 次和第 $M$ 次抛掷中。

设 $P(n)$ 为 $M$ 能被 $n$ 整除的概率。例如结果 HH、HTHH、THTTHH 都计入 $P(2)$，而 THH 和 HTTHH 不计入。

已知 $P(2) =\frac 3 5$，$P(3)=\frac 9 {31}$。事实上可以证明 $P(n)$ 总是有理数。

对素数 $p$ 和最简分数 $\frac a b$，定义 $Q(\frac a b,p)$ 为满足 $a \equiv b q \pmod{p}$ 的最小正整数 $q$。

例如 $Q(P(2), 109) = Q(\frac 3 5, 109) = 66$，因为 $5 \cdot 66 = 330 \equiv 3 \pmod{109}$ 且 $66$ 是最小的这样的正整数。类似地 $Q(P(3),109) = 46$。

求 $Q(P(10^{18}),1\,000\,000\,009)$。
