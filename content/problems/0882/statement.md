# 882 · 删位博弈

> 中文意译。英文原文见 [Project Euler Problem 882](https://projecteuler.net/problem=882)；抓取底稿见 `statement.en.md`。

One 博士与 Zero 博士正在进行如下不平等博弈（partisan game）。
游戏开始时有 $1$ 个 $1$、$2$ 个 $2$、$3$ 个 $3$、……、$n$ 个 $n$。由 One 博士先手，两人轮流操作。
One 博士选择一个数，并从其二进制表示中删去一个数字 $1$。
Zero 博士选择一个数，并从其二进制表示中删去一个数字 $0$。
无法进行合法移动的玩家判负。
注意任何二进制表示均不允许有前导零；特别地，两位玩家都不能对数字 $0$ 进行任何操作。

他们很快意识到 Zero 博士绝无可能获胜。为了让游戏更有趣，允许 Zero 博士进行若干次“跳过回合”（skip the turn），即将行动权直接交还给 One 博士而不做任何改动。

例如，当 $n = 2$ 时，若允许 Zero 博士跳过 $2$ 次回合，他便能赢得游戏。一局示例过程如下：

$$
[1, 2, 2]\xrightarrow{\text{One 博士}}[1, 0, 2]\xrightarrow{\text{Zero 博士}}[1, 0, 1]\xrightarrow{\text{One 博士}}[1, 0, 0]\xrightarrow[\text{跳过}]{\text{Zero 博士}} [1, 0, 0]\xrightarrow{\text{One 博士}}[0, 0, 0]\xrightarrow[\text{跳过}]{\text{Zero 博士}}[0, 0, 0]
$$

设 $S(n)$ 为保证 Zero 博士拥有必胜策略所需的最少跳过回合次数。
已知 $S(2) = 2$，$S(5) = 17$，$S(10) = 64$。

求 $S(10^5)$。
