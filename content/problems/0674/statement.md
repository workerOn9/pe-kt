# 674 · 求解 I-方程

> 中文意译。英文原文见 [Project Euler Problem 674](https://projecteuler.net/problem=674)；抓取底稿见 `statement.en.md`。

我们定义 $\mathcal{I}$ 算子为函数

$$
\mathcal{I}(x,y) = (1+x+y)^2+y-x
$$

并把 $\mathcal{I}$-表达式定义为仅由变量名和对 $\mathcal{I}$ 的应用构成的算术表达式。变量名可以由一个或多个字母组成。例如，三个表达式 $x$、$\mathcal{I}(x,y)$ 和 $\mathcal{I}(\mathcal{I}(x,ab),x)$ 都是 $\mathcal{I}$-表达式。

对于两个满足方程 $e_1=e_2$ 在非负整数中有解的 $\mathcal{I}$-表达式 $e_1$ 和 $e_2$，我们定义 $e_1$ 与 $e_2$ 的最小同时值为 $e_1$ 和 $e_2$ 在该解上取得的最小值。若方程 $e_1=e_2$ 在非负整数中无解，我们定义 $e_1$ 与 $e_2$ 的最小同时值为 $0$。例如，考虑以下三个 $\mathcal{I}$-表达式：

$$
\begin{array}{l}A = \mathcal{I}(x,\mathcal{I}(z,t))\\ B = \mathcal{I}(\mathcal{I}(y,z),y)\\ C = \mathcal{I}(\mathcal{I}(x,z),y)\end{array}
$$

$A$ 与 $B$ 的最小同时值为 $23$，在 $x=3,y=1,z=t=0$ 处取得。另一方面，$A=C$ 在非负整数中无解，所以 $A$ 与 $C$ 的最小同时值为 $0$。由 $\{A,B,C\}$ 中的 $\mathcal{I}$-表达式组成的所有数对的最小同时值之和为 $26$。

求由文件 [I-expressions.txt](https://projecteuler.net/resources/documents/0674_i_expressions.txt) 中不同表达式组成的所有 $\mathcal{I}$-表达式数对的最小同时值之和（数对 $(e_1,e_2)$ 与 $(e_2,e_1)$ 视为相同）。给出结果的后九位数字作为答案。
