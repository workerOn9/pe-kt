# 458 · 「project」的排列

> 中文意译。英文原文见 [Project Euler Problem 458](https://projecteuler.net/problem=458)；抓取底稿见 `statement.en.md`。

考虑由单词「project」的字母构成的字母表 $A$：$A=\{\text{c},\text{e},\text{j},\text{o},\text{p},\text{r},\text{t}\}$。
记 $T(n)$ 为长度 $n$、由 $A$ 中字母组成的字符串个数，要求其中不含「project」的 $5040$ 个排列中的任何一个作为子串。

$T(7)=7^7-7!=818503$。

求 $T(10^{12})$。给出你答案的后 $9$ 位数字。
