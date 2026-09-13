# 平方根的渐近分数

> 中文意译。英文原文见 [Project Euler Problem 57](https://projecteuler.net/problem=57)；抓取底稿见 `statement.en.md`。

可以证明，$\sqrt 2$ 能表示为无限连分数：

$$\sqrt 2 = 1 + \cfrac{1}{2 + \cfrac{1}{2 + \cfrac{1}{2 + \dots}}}$$

把它展开，前四次迭代得到

$$1 + \frac{1}{2} = \frac{3}{2} = 1.5$$

$$1 + \cfrac{1}{2 + \cfrac{1}{2}} = \frac{7}{5} = 1.4$$

$$1 + \cfrac{1}{2 + \cfrac{1}{2 + \cfrac{1}{2}}} = \frac{17}{12} = 1.41666\dots$$

$$1 + \cfrac{1}{2 + \cfrac{1}{2 + \cfrac{1}{2 + \cfrac{1}{2}}}} = \frac{41}{29} = 1.41379\dots$$

接下来三次展开分别是 $\frac{99}{70}$、$\frac{239}{169}$、$\frac{577}{408}$；而第八次展开 $\frac{1393}{985}$ 是第一个分子位数超过分母位数的例子。

在前 $1000$ 次展开中，有多少个分数的分子位数多于分母位数？
