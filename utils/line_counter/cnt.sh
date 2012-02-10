#!/bin/bash

cmd=$(readlink -f "${0%/*}/line_counter.sh")

cd ../..
$cmd src/*/java
