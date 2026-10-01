# 722 · 缓慢收敛的级数

> 中文意译。英文原文见 [Project Euler Problem 722](https://projecteuler.net/problem=722)；抓取底稿见 `statement.en.md`。

对非负整数 $k$，定义

$$
E_k(q) = \sum\limits_{n = 1}^\infty \sigma_k(n)q^n
$$

其中 $\sigma_k(n) = \sum_{d \mid n} d^k$ 是 $n$ 的所有正因数的 $k$ 次幂之和。

可以证明，对任意 $k$，级数 $E_k(q)$ 对任意 $0 < q < 1$ 收敛。

例如，
$E_1(1 - \frac{1}{2^4}) = 3.872155809243\mathrm e2$
$E_3(1 - \frac{1}{2^8}) = 2.767385314772\mathrm e10$
$E_7(1 - \frac{1}{2^{15}}) = 6.725803486744\mathrm e39$
以上所有数值均以科学计数法给出，保留小数点后十二位。

求 $E_{15}(1 - \frac{1}{2^{25}})$ 的值。答案以科学计数法给出，保留小数点后十二位。
