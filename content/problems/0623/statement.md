# 623 · Lambda 计数

> 中文意译。英文原文见 [Project Euler Problem 623](https://projecteuler.net/problem=623)；抓取底稿见 `statement.en.md`。

lambda 演算是函数式编程语言核心处的一种通用计算模型。它基于 lambda 项——一种极简编程语言，只含函数定义、函数调用和变量。lambda 项按以下规则构造：

- 任何变量 $x$（单字母，取自某个无限字母表）都是 lambda 项。

- 若 $M$ 和 $N$ 是 lambda 项，则 $(M N)$ 是 lambda 项，称为把 $M$ 应用于 $N$。

- 若 $x$ 是变量且 $M$ 是项，则 $(\lambda x. M)$ 是 lambda 项，称为一个抽象。抽象定义一个匿名函数，以 $x$ 为参数并返回 $M$。

称 lambda 项 $T$ 是**封闭的**，如果对所有变量 $x$，$T$ 中出现的每个 $x$ 都包含在 $T$ 的某个抽象 $(\lambda x. M)$ 之中。最小的这类抽象称为约束了变量 $x$ 的该次出现。换句话说，一个 lambda 项是封闭的，当且仅当它所有的变量都受包围它的函数定义所约束。例如项 $(\lambda x. x)$ 是封闭的，而项 $(\lambda x. (x y))$ 不是，因为 $y$ 不受约束。

此外，只要约束性的抽象不改变，我们就可以重命名变量。这意味着 $(\lambda x. x)$ 和 $(\lambda y. y)$ 应视为等价，因为我们只是重命名了一个参数。两个在此类重命名下等价的项称为 $\alpha$-等价。注意 $(\lambda x. (\lambda y. (x y)))$ 与 $(\lambda x. (\lambda x. (x x)))$ 不 $\alpha$-等价，因为约束第一个变量的抽象原本在外层，此时变成了内层。然而 $(\lambda x. (\lambda y. (x y)))$ 与 $(\lambda y. (\lambda x. (y x)))$ 是 $\alpha$-等价的。

下表列出了用至多 $15$ 个符号（符号指括号、$\lambda$、点号和变量）能写出的 lambda 项。

$$
\begin{array}{|c|c|c|c|} \hline (\lambda x.x) & (\lambda x.(x x)) & (\lambda x.(\lambda y.x)) & (\lambda x.(\lambda y.y)) \\ \hline (\lambda x.(x (x x))) & (\lambda x.((x x) x)) & (\lambda x.(\lambda y.(x x))) & (\lambda x.(\lambda y.(x y))) \\ \hline (\lambda x.(\lambda y.(y x))) & (\lambda x.(\lambda y.(y y))) & (\lambda x.(x (\lambda y.x))) & (\lambda x.(x (\lambda y.y))) \\ \hline (\lambda x.((\lambda y.x) x)) & (\lambda x.((\lambda y.y) x)) & ((\lambda x.x) (\lambda x.x)) & (\lambda x.(x (x (x x)))) \\ \hline (\lambda x.(x ((x x) x))) & (\lambda x.((x x) (x x))) & (\lambda x.((x (x x)) x)) & (\lambda x.(((x x) x) x)) \\ \hline \end{array}
$$

设 $\Lambda(n)$ 为用至多 $n$ 个符号能写出的、互不 $\alpha$-等价的封闭 lambda 项个数。已知 $\Lambda(6) = 1$，$\Lambda(9) = 2$，$\Lambda(15) = 20$，$\Lambda(35) = 3166438$。

求 $\Lambda(2000)$，答案对 $1\,000\,000\,007$ 取模。
