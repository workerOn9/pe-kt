# 779 · 质因数与指数

> 中文意译。英文原文见 [Project Euler Problem 779](https://projecteuler.net/problem=779)；抓取底稿见 `statement.en.md`。

对于正整数 $n \gt 1$，设 $p(n)$ 为整除 $n$ 的最小质数，$\alpha(n)$ 为其 $p$ 进阶，即使 $p(n)^{\alpha(n)}$ 整除 $n$ 的最大整数。

对于正整数 $K$，定义函数 $f_K(n)$ 为：

$$
f_K(n)=\frac{\alpha(n)-1}{(p(n))^K}.
$$

再定义 $\overline{f_K}$ 为：

$$
\overline{f_K}=\lim_{N \to \infty} \frac{1}{N}\sum_{n=2}^{N} f_K(n).
$$

可以验证 $\overline{f_1} \approx 0.282419756159$。

求 $\displaystyle \sum_{K=1}^{\infty}\overline{f_K}$，答案四舍五入到小数点后 $12$ 位。
