set -x

env
pwd
id
lein deps 1> /dev/null
ls -hal
export DEBUG=true
lein deploy clojars
