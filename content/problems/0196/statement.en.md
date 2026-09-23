Build a triangle from all positive integers in the following way:

```
    1
   2 3
  4 5 6
 7 8 9 10
11 12 13 14 15
```

Each positive integer has up to eight neighbours in the triangle.

A set of three primes is called a prime triplet if one of the three primes has the other two as neighbours in the triangle.

For example, in the second row, the prime numbers $2$ and $3$ are elements of some prime triplet.

If row $8$ is considered, it contains two primes which are elements of some prime triplet, i.e. $29$ and $31$.
If row $9$ is considered, it contains only one prime which is an element of some prime triplet: $37$.

Define $S(n)$ as the sum of the primes in row $n$ which are elements of any prime triplet.
Then $S(8)=60$ and $S(9)=37$.

You are given that $S(10000)=950007619$.

Find $S(5678027) + S(7208785)$.
