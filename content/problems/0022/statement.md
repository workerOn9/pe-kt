# 姓名分数

> 中文意译。英文原文见 [Project Euler Problem 22](https://projecteuler.net/problem=22)；抓取底稿见 `statement.en.md`。

使用 [names.txt](https://projecteuler.net/resources/documents/0022_names.txt)（右键另存为），一个 46K 的文本文件，包含五千多个名字。首先把它按字母序排序，然后计算每个名字的字母值（A=1, B=2, …），将字母值乘以它在排序后列表中的位置，得到该名字的分数。

例如，排序后 COLIN 是第 938 个名字，其字母值为 3 + 15 + 12 + 9 + 14 = 53，所以 COLIN 的分数为 938 × 53 = 49714。

文件中所有名字的分数总和是多少？
