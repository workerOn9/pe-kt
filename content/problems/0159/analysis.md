# 数字根因子和

## 定义回顾

- mdr$(n)$：$n$ 的各位数字之积
- dr$(n)$：数字根（反复求各位数字之和至一位数）
- mdrs$(n) = \text{dr}(\text{mdr}(n))$

## 算法

枚举三元组 $(a, b, c)$ 满足 $a \leq b \leq c$ 且 $abc \leq 250000$，检查 mdrs 等式：

$$\text{mdrs}(a \cdot b \cdot c) = \text{mdrs}(a) + \text{mdrs}(b) + \text{mdrs}(c)$$

## 复杂度

最坏情况 $O(N^{4/3}) \approx 2.6 \times 10^{11}$，实际因边界约束远小于此。

## 答案

Python 完整枚举校验得总和：

$$14\,489\,159$$