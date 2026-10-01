# 480 · 最后一个问题

> 中文意译。英文原文见 [Project Euler Problem 480](https://projecteuler.net/problem=480)；抓取底稿见 `statement.en.md`。

考虑所有可以按任意顺序从下面这句话中选取字母构成的单词：

```
thereisasyetinsufficientdataforameaningfulanswer
```

把其中长度不超过 $15$ 个字母的单词按字典序排列并从 $1$ 开始依次编号。列表的前若干项如下：

| 编号 | 单词 |
| --- | --- |
| 1 | a |
| 2 | aa |
| 3 | aaa |
| 4 | aaaa |
| 5 | aaaaa |
| 6 | aaaaaa |
| 7 | aaaaaac |
| 8 | aaaaaacd |
| 9 | aaaaaacde |
| 10 | aaaaaacdee |
| 11 | aaaaaacdeee |
| 12 | aaaaaacdeeee |
| 13 | aaaaaacdeeeee |
| 14 | aaaaaacdeeeeee |
| 15 | aaaaaacdeeeeeef |
| 16 | aaaaaacdeeeeeeg |
| 17 | aaaaaacdeeeeeeh |
| $\dots$ | |
| 28 | aaaaaacdeeeeeey |
| 29 | aaaaaacdeeeeef |
| 30 | aaaaaacdeeeeefe |
| $\dots$ | |
| 115246685191495242 | euleoywuttttsss |
| 115246685191495243 | euler |
| 115246685191495244 | eulera |
| $\dots$ | |
| 525069350231428029 | ywuuttttssssrrr |

定义 $P(w)$ 为单词 $w$ 所在的位置。
定义 $W(p)$ 为位置 $p$ 上的单词。
可以看到 $P(w)$ 与 $W(p)$ 互为反函数：$P(W(p)) = p$ 且 $W(P(w)) = w$。
例如：

- $W(10) = \texttt{aaaaaacdee}$，$P(\texttt{aaaaaacdee}) = 10$
- $W(115246685191495243) = \texttt{euler}$，$P(\texttt{euler}) = 115246685191495243$

求 $W(P(\texttt{legionary}) + P(\texttt{calorimeters}) - P(\texttt{annihilate}) + P(\texttt{orchestrated}) - P(\texttt{fluttering}))$。

请用小写字母（不含标点与空格）给出你的答案。
