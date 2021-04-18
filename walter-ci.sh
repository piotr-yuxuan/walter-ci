#!/bin/sh -lxe
echo ls -hal
lein deps
lein test
