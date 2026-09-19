# Exploring strings for which lower three ASCII characters are in alphabetical order

Take a string of length n consisting of characters 'p', 'q', 'r'. If the three leftmost (and only) distinct characters in the string are in alphabetical order (left-to-right), we call the string valid.

Generalising: length-n strings over the first L lowercase letters such that the three leftmost distinct characters are in alphabetical order. For L = 26, n = 18, count valid strings.

Closed form: p(n) = C(26, n)(2^n − n − 1).