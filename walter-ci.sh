#!/bin/sh -lxe
cd /github/workspace
lein --help
lein deps
lein test
