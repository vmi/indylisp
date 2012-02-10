#!/usr/bin/perl

my $s = int($ARGV[0]);
my $e = int($ARGV[1]);

for (my $i = $s; $i <= $e; $i++) {
  if ($i % 15 == 0) {
    print "FizzBuzz\n";
  } elsif ($i % 5 == 0) {
    print "Buzz\n";
  } elsif ($i % 3 == 0) {
    print "Fizz\n";
  } else {
    print "$i\n";
  }
}
