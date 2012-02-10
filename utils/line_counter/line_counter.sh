#!/bin/bash

name=$(basename "$0" .sh)
dir=$(readlink -f "$0")
dir=${dir%/*}

if [ x"$1" = x-d ]; then
  shift
  debug=-d
fi

for d in "$@"; do
  d=${d%/}
  echo "[$d]"
  find $d -name "*.java" | xargs cat | perl "$dir/_$name.pl" $debug
done
