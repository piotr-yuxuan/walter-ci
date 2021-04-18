👋 You can't use version `0.0.0` of this action. See
[Installation](#installation).

# `piotr-yuxuan/walter-ci`

Walter Kohl is the younger son of Helmut Kohl. Like his father he
likes to break down walls and reunify friends under one common
fundamental law.

![](./doc/helmut-kohl-1.jpg)

This action runs an opinionated set of standard steps for all your
Clojure projects, defined by conventions and no configuration.

The goal of Walter is to automate and standardize CI maintenance jobs
as much as possible so that you can scale it to more than two public
personal projects on GitHub.

It's extremely tedious to maintain workflows on more than two
repositories. What if you want to make a change? What if you have had
a good idea to improve your CI on your latest project? You don't want
to loose any time to propagate change by replication and
copy/paste. That's the job of a machine, so let a machine do it.

## Installation

Refer the `main` branch to automatically upgrade this action across
all your repositories. This makes the most of Walter, otherwise you
lose the benefits of it.

``` yaml
- name: Walter CI
  uses: piotr-yuxuan/walter-ci@main # use branch main
```

If you are unsure about how to do it, in your public GitHub repository
create a file `.github/workflows/walter-ci.yml` with the following
content:

``` yaml
name: Walter CI
on:
  push:
    branches: '*'
  pull_request:
    branches: '*'
  schedule:
    - cron: "0 0 1 * *"
jobs:
  piotr-yuxuan/walter-ci:
    runs-on: ubuntu-latest
    steps:
    - uses: piotr-yuxuan/walter-ci@main # use branch main
```

## How to write a good README?

See it later, once there is actually something to tell about.
