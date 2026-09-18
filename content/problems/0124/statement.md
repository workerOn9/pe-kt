# 124 · 根基排序

> 中文意译。英文原文见 [Project Euler Problem 124](https://projecteuler.net/problem=124)；抓取底稿见 `statement.en.md`。

$n$ 的**根基** $\operatorname{rad}(n)$ 定义为 $n$ 的所有**不同**质因子的乘积。
例如 $504 = 2^3 \times 3^2 \times 7$，于是 $\operatorname{rad}(504) = 2 \times 3 \times 7 = 42$。

现在计算 $1 \le n \le 10$ 的 $\operatorname{rad}(n)$，然后按 $\operatorname{rad}(n)$ 升序排序；
根基相等时按 $n$ 升序排序，得到

| $k$ | 排序前的 $n$ | 排序前的 $\operatorname{rad}(n)$ | 排序后的 $n$ | 排序后的 $\operatorname{rad}(n)$ |
|-----|--------------|--------------------------------|--------------|--------------------------------|
| 1 | 1 | 1 | 1 | 1 |
| 2 | 2 | 2 | 2 | 2 |
| 3 | 3 | 3 | 4 | 2 |
| 4 | 4 | 2 | 8 | 2 |
| 5 | 5 | 5 | 3 | 3 |
| 6 | 6 | 6 | 9 | 3 |
| 7 | 7 | 7 | 5 | 5 |
| 8 | 8 | 2 | 6 | 6 |
| 9 | 9 | 3 | 7 | 7 |
| 10 | 10 | 10 | 10 | 10 |

用 $E(k)$ 表示排序后 $n$ 那一列的第 $k$ 个元素，例如 $E(4) = 8$、$E(6) = 9$。

若把 $1 \le n \le 100\,000$ 的 $\operatorname{rad}(n)$ 全部算出并按上述规则排序，求 $E(10\,000)$。

（题面原文：[projecteuler.net/problem=124](https://projecteuler.net/problem=124)）
