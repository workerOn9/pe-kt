# PE 209 — Circular Logic

## 题面

一个 $k$ 输入二元真值表是从 $k$ 个输入位（二进制数字，$0$ [假] 或 $1$ [真]）到 $1$ 个输出位的映射。例如，逻辑 $\text{AND}$ 与 $\text{XOR}$ 的 $2$ 输入二元真值表如下：

| $x$ | $y$ | $x \mathbin{\text{AND}} y$ |
|---|---|---|
| 0 | 0 | 0 |
| 0 | 1 | 0 |
| 1 | 0 | 0 |
| 1 | 1 | 1 |

| $x$ | $y$ | $x \mathbin{\text{XOR}} y$ |
|---|---|---|
| 0 | 0 | 0 |
| 0 | 1 | 1 |
| 1 | 0 | 1 |
| 1 | 1 | 0 |

问有多少个 $6$ 输入二元真值表 $\tau$，满足对**所有** $6$ 位输入 $(a, b, c, d, e, f)$：

$$
\tau(a, b, c, d, e, f) \mathbin{\text{AND}} \tau(b, c, d, e, f, a \mathbin{\text{XOR}} (b \mathbin{\text{AND}} c)) = 0
$$

## 原文链接

https://projecteuler.net/problem=209
