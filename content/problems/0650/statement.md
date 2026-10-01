# 650 · 二项式系数乘积的因数

> 中文意译。英文原文见 [Project Euler Problem 650](https://projecteuler.net/problem=650)；抓取底稿见 `statement.en.md`。

设 $B(n) = \displaystyle \prod_{k=0}^n {n \choose k}$，即二项式系数的乘积。
例如，$B(5) = {5 \choose 0} \times {5 \choose 1} \times {5 \choose 2} \times {5 \choose 3} \times {5 \choose 4} \times {5 \choose 5} = 1 \times 5 \times 10 \times 10 \times 5 \times 1 = 2500$。

设 $D(n) = \displaystyle \sum_{d|B(n)} d$，即 $B(n)$ 的因数之和。
例如，B(5) 的因数为 1, 2, 4, 5, 10, 20, 25, 50, 100, 125, 250, 500, 625, 1250 和 2500，
所以 D(5) = 1 + 2 + 4 + 5 + 10 + 20 + 25 + 50 + 100 + 125 + 250 + 500 + 625 + 1250 + 2500 = 5467。

设 $S(n) = \displaystyle \sum_{k=1}^n D(k)$。
已知 $S(5) = 5736$，$S(10) = 141740594713218418$，$S(100)$ mod $1\,000\,000\,007 = 332792866$。

求 $S(20\,000)$ mod $1\,000\,000\,007$。
