#!/bin/sh -xe
cd /github/workspace

which java
apt-get install java
which java

lein --help
lein deps
lein test
