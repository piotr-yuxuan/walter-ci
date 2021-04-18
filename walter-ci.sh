#!/bin/sh -xe
echo "We are in $(pwd)"
find / -name "*.clj"
lein deps
lein test
