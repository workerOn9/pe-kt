# 525 · 滚动的椭圆

> 中文意译。英文原文见 [Project Euler Problem 525](https://projecteuler.net/problem=525)；抓取底稿见 `statement.en.md`。

椭圆 $E(a, b)$ 的初始位置由方程 $\frac {x^2} {a^2} + \frac {(y - b)^2} {b^2} = 1$ 给出。

该椭圆沿 $x$ 轴无滑动地滚动一整圈。有趣的是，焦点描出的曲线长度与半短轴的大小无关：
$F(a,b) = 2 \pi \max(a,b)$

![椭圆沿 x 轴无滑动地滚动时，其焦点描出的曲线](0525-rolling-ellipse-1.gif)

椭圆*中心*描出的曲线则没有这个性质。记 $C(a, b)$ 为椭圆沿 $x$ 轴无滑动地滚动一整圈时，其中心所描出曲线的长度。

![椭圆沿 x 轴无滑动地滚动时，其中心描出的曲线](0525-rolling-ellipse-2.gif)

已知 $C(2, 4) \approx 21.38816906$。

求 $C(1, 4) + C(3, 4)$，结果四舍五入到小数点后 $8$ 位，写成 ab.cdefghij 的形式。
