# Digital root sums of factorisations

Let mdr(n) be the product of the digits of n (in base 10). We define mdr(s) for a string s as mdr(d1)·mdr(d2)·...·mdr(dk) where d1,d2,...,dk are the digits of s. The digital root dr(n) of n is the single digit obtained by repeatedly summing the digits of n until a single digit remains.

Let mdrs(n) = dr(mdr(n)).

Consider the factorisations of n into three factors (order doesn't matter). Find the sum of all n ≤ 250000 such that mdrs(n) equals the sum of the digital roots of its three prime factors (with repetition).