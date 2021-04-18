#!/bin/sh -xe
cd /github/workspace
pwd
ls -hal
lein deps
lein test
