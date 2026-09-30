一个偶数正整数 $N$ 称为 **admissible（可容许）**，如果它是 $2$ 的幂，或者它的不同素因子恰为连续的素数。

前 12 个 admissible 数是：$2, 4, 6, 8, 12, 16, 18, 24, 30, 32, 36, 48$。

如果 $N$ 是 admissible 的，那么使 $N + M$ 为素数的最小整数 $M > 1$，称为 $N$ 的 **pseudo-Fortunate 数（伪幸运数）**。

例如，$N = 630$ 是 admissible 的，因为它是偶数，且它的不同素因子是连续的素数 $2, 3, 5$ 和 $7$。

$631$ 之后的下一个素数是 $641$；因此 $630$ 的 pseudo-Fortunate 数是 $M = 11$。

同样可知 $16$ 的 pseudo-Fortunate 数是 $3$。

求所有小于 $10^9$ 的 admissible 数相对应的**不同** pseudo-Fortunate 数之和。
