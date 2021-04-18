#!/bin/sh -xe
#lein deps
#lein test
apt-get install maven
mvn help:effective-settings
lein deploy clojars
#lein deploy github
exit 0
