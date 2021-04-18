#!/bin/zsh -xe
cd /github/workspace

where java

lein --help
lein deps
lein test
