#!/bin/sh -lxe
cd /github/workspace
command -v clojure
echo ls -hal
lein deps
lein test
