# 949 · 左右对弈 II

> 中文意译。英文原文见 [Project Euler Problem 949](https://projecteuler.net/problem=949)；抓取底稿见 `statement.en.md`。

两名玩家 Left 和 Right 在若干个由字母 L 和 R 组成的单词上轮流博弈。
- 轮到 Left 时，对于每一个单词，Left 可以从该单词的最左端移除任意数量的字母（可以为 0 个，但不能移除全部字母）；但整个回合中，必须至少从至少一个单词中移除至少一个字母。
- 轮到 Right 时，Right 进行对称操作，从各单词的最右端移除任意数量的字母（不能将任何单词移空），且同样必须至少从至少一个单词中移除至少一个字母。

游戏持续进行，直到每个单词均只剩下单个字母。
若剩余字母中 'L' 的数量严格多于 'R'，则 Left 获胜；反之，若 'R' 的数量严格多于 'L'，则 Right 获胜。
本题中我们仅考虑单词总数为奇数的游戏，因此不可能出现平局。

设 $G(n, k)$ 为选出 $k$ 个长度为 $n$ 的单词、使得在 Left 先手的前提下 Right 拥有必胜策略的方案数。同一组单词的不同排列顺序视为不同方案。

已知 $G(2, 3) = 14$，对应的所有可行解（及其排列）如下：

$$
\begin{aligned}
(\texttt{LL},\texttt{RR},\texttt{RR}) &: 3\text{ 种排列} \\
(\texttt{LR},\texttt{LR},\texttt{LR}) &: 1\text{ 种排列} \\
(\texttt{LR},\texttt{LR},\texttt{RR}) &: 3\text{ 种排列} \\
(\texttt{LR},\texttt{RR},\texttt{RR}) &: 3\text{ 种排列} \\
(\texttt{RL},\texttt{RR},\texttt{RR}) &: 3\text{ 种排列} \\
(\texttt{RR},\texttt{RR},\texttt{RR}) &: 1\text{ 种排列}
\end{aligned}
$$

已知 $G(4, 3) = 496$ 且 $G(8, 5) = 26359197010$。

求 $G(20, 7) \bmod 1001001011$。
