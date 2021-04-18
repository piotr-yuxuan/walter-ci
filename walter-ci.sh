#!/bin/sh -xe
lein deps
lein test
lein deploy github
#lein deploy clojars
exit 0
