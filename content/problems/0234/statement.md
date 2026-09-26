对整数 $n \ge 4$，定义 $n$ 的**下素数平方根** $\operatorname{lps}(n)$ 为不超过 $\sqrt n$ 的最大素数，**上素数平方根** $\operatorname{ups}(n)$ 为不小于 $\sqrt n$ 的最小素数。

例如 $\operatorname{lps}(4) = 2 = \operatorname{ups}(4)$，$\operatorname{lps}(1000) = 31$，$\operatorname{ups}(1000) = 37$。

若 $\operatorname{lps}(n)$ 与 $\operatorname{ups}(n)$ 中**恰有一个**整除 $n$，则称整数 $n \ge 4$ 是**半可除的**。

不超过 $15$ 的半可除数之和为 $30$，它们是 $8$、$10$、$12$；$15$ 不是半可除数，因为它是 $\operatorname{lps}(15) = 3$ 与 $\operatorname{ups}(15) = 5$ 的公倍数。

再举一例：不超过 $1000$ 的 $92$ 个半可除数之和为 $34825$。

求所有不超过 $999966663333$ 的半可除数之和。
