# 608 · 除数之和

> 中文意译。英文原文见 [Project Euler Problem 608](https://projecteuler.net/problem=608)；抓取底稿见 `statement.en.md`。

设 $D(m,n)=\displaystyle\sum_{d\mid m}\sum_{k=1}^n\sigma_0(kd)$，其中 $d$ 取遍 $m$ 的所有正因数，$\sigma_0(n)$ 表示 $n$ 的正因数个数。

已知 $D(3!,10^2)=3398$，$D(4!,10^6)=268882292$。

求 $D(200!,10^{12}) \bmod (10^9 + 7)$。
