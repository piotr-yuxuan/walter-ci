#!/bin/sh -xe
echo "We are in $(pwd)"
env > env.txt
ls -hal env.txt
cat env.txt
lein deps
lein test
