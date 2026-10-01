# 838 · 互素

> 中文意译。英文原文见 [Project Euler Problem 838](https://projecteuler.net/problem=838)；抓取底稿见 `statement.en.md`。

设 $f(N)$ 为最小的正整数，使得它不与任何末位数字为 $3$ 且满足 $n \le N$ 的正整数 $n$ 互素。

例如 $f(40)$ 等于 $897 = 3 \cdot 13 \cdot 23$，因为它不与 $3, 13, 23, 33$ 中的任何一个互素。取[自然对数](https://en.wikipedia.org/wiki/Natural_logarithm)（以 $e$ 为底的对数）后，四舍五入到小数点后六位可得 $\ln f(40) = \ln 897 \approx 6.799056$。

还已知 $\ln f(2800) \approx 715.019337$。

求 $f(10^6)$，给出其自然对数四舍五入到小数点后六位的结果。
