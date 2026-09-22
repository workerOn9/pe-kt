一个数 $a$ 关于正整数 $b$ 的超乘方（Hyperexponentiation，又称迭代幂次、Tetration），记作 $a\mathbin{\uparrow \uparrow}b$ 或 $^b a$，递归定义如下：

- $a \mathbin{\uparrow \uparrow} 1 = a$；
- $a \mathbin{\uparrow \uparrow} (k+1) = a^{(a \mathbin{\uparrow \uparrow} k)}$。

例如：
- $3 \mathbin{\uparrow \uparrow} 2 = 3^3 = 27$；
- $3 \mathbin{\uparrow \uparrow} 3 = 3^{27} = 7625597484987$；
- $3 \mathbin{\uparrow \uparrow} 4 = 3^{7625597484987} \approx 10^{3.6383346400240996 \cdot 10^{12}}$。

请求出 $1777 \mathbin{\uparrow \uparrow} 1855$ 的最后 $8$ 位数字。
