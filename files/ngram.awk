BEGIN { n = 2; }
{ for (i = 1; i <= length($1) - (n - 1); ++i) { print substr($1, i, n); } }
