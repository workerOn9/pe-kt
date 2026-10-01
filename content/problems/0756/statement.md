# 756 · 近似一个求和

> 中文意译。英文原文见 [Project Euler Problem 756](https://projecteuler.net/problem=756)；抓取底稿见 `statement.en.md`。

考虑对所有正整数 $k>0$ 定义的函数 $f(k)$。设 $S$ 为 $f$ 的前 $n$ 个值之和，即

$$
S=f(1)+f(2)+f(3)+\cdots+f(n)=\sum_{k=1}^n f(k).
$$

在本题中，我们用随机性来近似这个和。具体地，我们选取一个均匀随机的、由正整数组成的 $m$ 元组 $(X_1,X_2,X_3,\cdots,X_m)$，满足 $0=X_0 \lt X_1 \lt X_2 \lt \cdots \lt X_m \leq n$，并按如下方式计算一个修正和 $S^*$：

$$
S^* = \sum_{i=1}^m f(X_i)(X_i-X_{i-1})
$$

我们定义该近似的误差为 $\Delta=S-S^*$。

设 $\mathbb{E}(\Delta|f(k),n,m)$ 为在给定函数 $f(k)$、求和项数 $n$ 与随机样本长度 $m$ 条件下误差的期望值。

例如 $\mathbb{E}(\Delta|k,100,50) = 2525/1326 \approx 1.904223$，$\mathbb{E}(\Delta|\varphi(k),10^4,10^2)\approx 5842.849907$，其中 $\varphi(k)$ 是欧拉函数。

求 $\mathbb{E}(\Delta|\varphi(k),12345678,12345)$，四舍五入到小数点后六位。
