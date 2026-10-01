# 448 · 最小公倍数的平均值

> 中文意译。英文原文见 [Project Euler Problem 448](https://projecteuler.net/problem=448)；抓取底稿见 `statement.en.md`。

函数 $\operatorname{\mathbf{lcm}}(a,b)$ 表示 $a$ 与 $b$ 的最小公倍数。
记 $A(n)$ 为 $\operatorname{lcm}(n,i)$（$1 \le i \le n$）的平均值。
例如：$A(2)=(2+2)/2=2$，$A(10)=(10+10+30+20+10+30+70+40+90+10)/10=32$。

设 $S(n)=\sum A(k)$，其中 $1 \le k \le n$。
$S(100)=122726$。

求 $S(99999999019) \bmod 999999017$。
