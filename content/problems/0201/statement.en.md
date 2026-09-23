For any set $S$ of numbers, let $\sigma(S)$ be the sum of the elements of $S$.

Consider the set $V=\{1,2,3,4,5,6\}$.

There are ${6 \choose 3}=20$ subsets of $V$ containing three elements, and their sums are:

$\{1,2,3\} \to 6,\{1,2,4\} \to 7,\{1,2,5\} \to 8,\{1,2,6\} \to 9,$
$\{1,3,4\} \to 8,\{1,3,5\} \to 9,\{1,3,6\} \to 10,$
$\{1,4,5\} \to 10,\{1,4,6\} \to 11,\{1,5,6\} \to 12,$
$\{2,3,4\} \to 9,\{2,3,5\} \to 10,\{2,3,6\} \to 11,$
$\{2,4,5\} \to 11,\{2,4,6\} \to 12,\{2,5,6\} \to 13,$
$\{3,4,5\} \to 12,\{3,4,6\} \to 13,\{3,5,6\} \to 14,\{4,5,6\} \to 15.$

Some of these sums occur more than once, others are unique.

For a set $V$, let $U_k(V)$ be the set of unique sums of $k$-element subsets of $V$, in our example we find $U_3(V)=\{6,7,14,15\}$ and $\sigma(U_3(V))=42$.

Now consider the $100$-element set $V=\{1,2,3,\dots,100\}$.

$V$ has ${100 \choose 50}$ $50$-element subsets.

Determine the sum of all integers which are the sum of exactly one of the $50$-element subsets of $V$, i.e. find $\sigma(U_{50}(V))$.
