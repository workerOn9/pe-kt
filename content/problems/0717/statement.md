# 717 · 一个模公式的求和

> 中文意译。英文原文见 [Project Euler Problem 717](https://projecteuler.net/problem=717)；抓取底稿见 `statement.en.md`。

对奇质数 $p$，定义 $f(p) = \left\lfloor\frac{2^{(2^p)}}{p}\right\rfloor\bmod{2^p}$
例如，当 $p=3$ 时，$\lfloor 2^8/3\rfloor = 85 \equiv 5 \pmod 8$，因此 $f(3) = 5$。

再定义 $g(p) = f(p)\bmod p$。已知 $g(31) = 17$。

现在定义 $G(N)$ 为所有小于 $N$ 的奇质数的 $g(p)$ 之和。
已知 $G(100) = 474$，$G(10^4) = 2819236$。

求 $G(10^7)$。
