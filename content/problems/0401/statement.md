# 401 · 除数平方和

> 中文意译。英文原文见 [Project Euler Problem 401](https://projecteuler.net/problem=401)；抓取底稿见 `statement.en.md`。

$6$ 的约数为 $1,2,3$ 与 $6$。这些数的平方和为 $1+4+9+36=50$。

记 $\operatorname{sigma}_2(n)$ 为 $n$ 的各约数的平方和。于是 $\operatorname{sigma}_2(6)=50$。

记 $\operatorname{SIGMA}_2$ 为 $\operatorname{sigma}_2$ 的前缀和函数，即 $\operatorname{SIGMA}_2(n)=\sum \operatorname{sigma}_2(i)$，其中 $i$ 从 $1$ 取到 $n$。
$\operatorname{SIGMA}_2$ 的前 $6$ 个值为 $1,6,16,37,63$ 与 $113$。

求 $\operatorname{SIGMA}_2(10^{15})$ 模 $10^9$ 的结果。
