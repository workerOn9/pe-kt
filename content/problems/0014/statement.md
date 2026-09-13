# 最长 Collatz 序列

> 中文意译。英文原文见 [Project Euler Problem 14](https://projecteuler.net/problem=14)；抓取底稿见 `statement.en.md`。

对正整数定义如下迭代序列：

- n → n/2（n 为偶数）
- n → 3n + 1（n 为奇数）

从 13 出发应用上述规则，得到如下序列：

$$13 \to 40 \to 20 \to 10 \to 5 \to 16 \to 8 \to 4 \to 2 \to 1$$

可以看出，这条序列（从 13 开始，到 1 结束）共包含 10 项。虽然至今未被证明（Collatz 猜想），但人们认为所有起点最终都会到达 1。

一百万以内，从哪个数出发能产生最长的链？

> 注意：链一旦开始，链上的项允许超过一百万。
