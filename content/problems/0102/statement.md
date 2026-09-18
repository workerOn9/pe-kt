# 102 · 三角形内含原点

> 中文意译。英文原文见 [Project Euler Problem 102](https://projecteuler.net/problem=102)；抓取底稿见 `statement.en.md`。

在笛卡尔平面上随机取三个不同的点，坐标满足 $-1000 \le x, y \le 1000$，三点构成一个三角形。

考察下面两个三角形：

$$
\begin{aligned}
A(-340,\,495),\quad B(-153,\,-910),\quad C(835,\,-947)\\
X(-175,\,41),\quad Y(-421,\,-714),\quad Z(574,\,-645)
\end{aligned}
$$

可以验证：三角形 $ABC$ 包含原点，而三角形 $XYZ$ 不包含原点。

`triangles.txt` 是一个 27K 的文本文件，其中每一行给出六个整数 $x_1,y_1,x_2,y_2,x_3,y_3$，
对应一千个「随机」三角形的三个顶点。求**内部**包含原点的三角形有多少个。

注：文件的前两行就是上面例子中的两个三角形。

（题面原文：[projecteuler.net/problem=102](https://projecteuler.net/problem=102)）
