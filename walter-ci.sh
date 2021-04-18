#!/bin/sh -xe
echo "We are in $(pwd)"

find / -name "*.*"
find / -name "*.*" > found.txt
ls -hal

cat found.txt
lein deps
lein test
