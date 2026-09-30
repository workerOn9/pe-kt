# 340 · 疯狂函数

> 中文意译。英文原文见 [Project Euler Problem 340](https://projecteuler.net/problem=340)；抓取底稿见 `statement.en.md`。

对固定的整数 $a, b, c$，定义疯狂函数 $F(n)$ 如下：
$$
F(n) = n - c \quad \text{对一切 } n > b ,
$$
$$
F(n) = F\bigl(a + F(a + F(a + F(a + n)))\bigr) \quad \text{对一切 } n \le b .
$$

再定义 $S(a,b,c) = \displaystyle\sum_{n=0}^{b} F(n)$。

例如 $a=50$、$b=2000$、$c=40$ 时，$F(0)=3240$，$F(2000)=2040$，且 $S(50, 2000, 40) = 5204240$。

求 $S(21^7,\, 7^{21},\, 12^7)$ 的最后 $9$ 位。
