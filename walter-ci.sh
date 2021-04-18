#!/bin/sh -xe
echo "WALTER_GITHUB_USERNAME=${WALTER_GITHUB_USERNAME}" > lol.txt
cat lol.txt
echo "WALTER_GITHUB_USERNAME="
lein deps
lein test
lein deploy github
#lein deploy clojars
exit 0
