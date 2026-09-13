# 硬币分拆

> 中文意译。英文原文见 [Project Euler Problem 78](https://projecteuler.net/problem=78)；抓取底稿见 `statement.en.md`。

设 $p(n)$ 表示把 $n$ 枚硬币分堆的方案数（堆与堆之间不计顺序）。例如 5 枚硬币恰好有 7 种分堆方式，即 $p(5)=7$：

```
OOOOO
OOOO   O
OOO   OO
OOO   O   O
OO   OO   O
OO   O   O   O
O   O   O   O   O
```

求最小的 $n$，使得 $p(n)$ 能被一百万整除。
