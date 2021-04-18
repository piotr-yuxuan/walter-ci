#!/bin/sh -lxe
cd /github/workspace
which java
which clojure
which lein

lein deps
lein test
