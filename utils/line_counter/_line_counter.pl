#!/usr/bin/perl

use strict;

sub dump_src {
  my $s = $_;
  my $i = 0;
  $s =~ s{^}{$i++;"$i: "}egm;
  print "-" x 78, "\n", $s, "-" x 78, "\n";
}

sub line_cnt {
  my $lines = 0;
  $lines++ while (/\n/g);
  $lines++ unless (/\n\z/s);
  return $lines;
}

sub num_format {
  my $max = 0;
  for my $n (@_) {
    $$n =~ s/\B(?=(?:\d{3})+$)/,/g;
    if ($max < length($$n)) {
      $max = length($$n);
    }
  }
  for my $n (@_) {
    $$n = sprintf("%${max}s", $$n);
  }
}

my $debug = 0;
if ($ARGV[0] eq '-d') {
  $debug = 1;
  shift(@ARGV);
}

local $/;
local $_;
my $lines = '';
while (<>) {
  s{[ \t\r]+}{}gs; # remove SPC, TAB, and CR. (keep LF)
  s{\n*\z} {\n}s; # tail of file have single LF.
  $lines .= $_;
}
$_ = $lines;

my $total_lines = line_cnt;

# remove Java comments.
s{/\*.*?\*/}{}gs;
s{//.*$}{}gm;

# remove blank lines.
s{(?:\A|(?<=\n))\s*\n}{}gs;

# lines without comment and blank.
my $nc_lines = line_cnt;

# remove braces.
s/[{}]+//gs;
s/^(?:try|return;|else)$//gm;
# remove package, import, and annotations
s/^(?:package|import|\@\w).*//gm;

# remove blank lines.
s{(?:\A|(?<=\n))\s*\n}{}gs;

dump_src if $debug;

my $stmt_lines = line_cnt;

num_format(\$total_lines, \$nc_lines, \$stmt_lines);

print <<EOF;
Total Lines     : $total_lines
Without Comment : $nc_lines
Statement Only  : $stmt_lines
EOF
