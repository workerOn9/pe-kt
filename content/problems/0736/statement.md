# 736 · 通向相等的路径

> 中文意译。英文原文见 [Project Euler Problem 736](https://projecteuler.net/problem=736)；抓取底稿见 `statement.en.md`。

在格点上定义两个函数：

$$
r(x,y) = (x+1,\,2y)
$$

$$
s(x,y) = (2x,\,y+1)
$$

对于数对 $(a,b)$，称长度为 $n$ 的序列 $\big((a_1,b_1),(a_2,b_2),\ldots,(a_n,b_n)\big)$ 为一条通向相等的路径，如果满足：

- $(a_1,b_1) = (a,b)$；
- 对 $k > 1$，$(a_k,b_k) = r(a_{k-1},b_{k-1})$ 或 $(a_k,b_k) = s(a_{k-1},b_{k-1})$；
- 对 $k < n$，$a_k \ne b_k$；
- $a_n = b_n$。

称 $a_n = b_n$ 为该路径的最终值。

例如：

$(45,90)\xrightarrow{r} (46,180)\xrightarrow{s}(92,181)\xrightarrow{s}(184,182)\xrightarrow{s}(368,183)\xrightarrow{s}(736,184)\xrightarrow{r}(737,368)\xrightarrow{s}(1474,369)\xrightarrow{r}(1475,738)\xrightarrow{r}(1476,1476)$

这是 $(45,90)$ 的一条通向相等的路径，长度为 10，最终值为 1476。$(45,90)$ 不存在长度更小的通向相等的路径。

求 $(45,90)$ 的奇长度最短的通向相等的路径，以其最终值作为答案。
