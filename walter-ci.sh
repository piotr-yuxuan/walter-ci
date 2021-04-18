#!/bin/sh -xe
lein deps
lein test
mvn help:effective-settings
lein deploy clojars
lein deploy github
exit 0
