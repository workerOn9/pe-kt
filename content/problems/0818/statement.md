# 818 · SET

> 中文意译。英文原文见 [Project Euler Problem 818](https://projecteuler.net/problem=818)；抓取底稿见 `statement.en.md`。

SET® 卡牌游戏使用一副 $81$ 张互不相同的牌。每张牌有四个特征（形状、颜色、数量、填充）。每个特征有三种不同的取值（例如颜色可以是红、紫、绿）。

一个 SET 由三张互不相同的牌组成，使得每个特征在三张牌上要么都相同，要么都不同。

对 $n$ 张牌的集合 $C_n$，记 $S(C_n)$ 为 $C_n$ 中 SET 的个数。再定义 $F(n) = \sum\limits_{C_n} S(C_n)^4$，其中 $C_n$ 遍历所有由 $n$ 张牌组成的集合（在 $81$ 张牌中选取）。已知 $F(3) = 1080$，$F(6) = 159690960$。

求 $F(12)$。

$\scriptsize{\text{SET 是 Cannei, LLC 的注册商标。版权所有。经 PlayMonster, LLC 许可使用。}}$
