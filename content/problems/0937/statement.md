# 937 · 等积划分

> 中文意译。英文原文见 [Project Euler Problem 937](https://projecteuler.net/problem=937)；抓取底稿见 `statement.en.md`。

设 $\theta = \sqrt{-2}$。

定义集合 $T$ 由所有形如 $a + b\theta$ 的数组成，其中 $a, b$ 为整数，且满足 $a > 0$，或者 $a = 0$ 且 $b > 0$。对于子集 $S \subseteq T$ 和元素 $z \in T$，定义 $p(S, z)$ 为从 $S$ 中选出两个不同元素、使其乘积为 $z$ 或 $-z$ 的方案数。

例如，若 $S = \{1, 2, 4\}$ 且 $z = 4$，则乘积为 $\pm 4$ 的不同元素对只有一组，即 $1$ 和 $4$。因此在此情形下 $p(S, z) = 1$。

又如，若 $S = \{1, \theta, 1+\theta, 2-\theta\}$ 且 $z = 2 - \theta$，由于 $1 \cdot (2 - \theta) = z$ 且 $\theta \cdot (1 + \theta) = -z$，因此 $p(S, z) = 2$。

设 $A$ 和 $B$ 是满足以下四个条件的集合：
- $1 \in A$
- $A \cap B = \emptyset$
- $A \cup B = T$
- 对所有 $z \in T$，均有 $p(A, z) = p(B, z)$

令人惊奇的是，这四个条件唯一确定了集合 $A$ 和 $B$。

设 $F_n$ 为前 $n$ 个阶乘构成的集合：$F_n = \{1!, 2!, \dots, n!\}$，并定义 $G(n)$ 为 $F_n \cap A$ 中所有元素之和。

已知 $G(4) = 25$，$G(7) = 745$，且 $G(100) \equiv 709772949 \pmod{10^9+7}$。

求 $G(10^8) \bmod (10^9 + 7)$。
