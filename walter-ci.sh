#!/bin/sh -xe
cd /github/workspace

which java

lein --help
lein deps
lein test
