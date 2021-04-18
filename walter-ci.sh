#!/bin/sh -xe
lein deps
lein test
lein deploy clojars
lein deploy github
exit 0
