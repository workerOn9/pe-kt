# 679 · Freefarea

> 中文意译。英文原文见 [Project Euler Problem 679](https://projecteuler.net/problem=679)；抓取底稿见 `statement.en.md`。

设 $S$ 为由四个字母 $\{\texttt{`A'},\texttt{`E'},\texttt{`F'},\texttt{`R'}\}$ 组成的集合。
对 $n\ge 0$，设 $S^*(n)$ 表示由 $S$ 中字母组成的所有长度为 $n$ 的单词的集合。
我们指定单词 $\texttt{FREE}, \texttt{FARE}, \texttt{AREA}, \texttt{REEF}$ 为关键词。

设 $f(n)$ 为 $S^*(n)$ 中包含全部四个关键词且每个恰好出现一次的单词数。

这种情况最早出现在 $n=9$，事实上存在唯一的 9 字母单词恰好包含每个关键词一次：$\texttt{FREEFAREA}$
所以 $f(9)=1$。

已知 $f(15)=72863$。

求 $f(30)$。
