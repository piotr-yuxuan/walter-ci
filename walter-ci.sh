#!/bin/sh -xe
pwd
ls -hal

cd /
pwd
ls -hal

cd /github
pwd
ls -hal

cd /github/home
pwd
ls -hal

cd /github/workspace
pwd
ls -hal

lein deps
lein test
