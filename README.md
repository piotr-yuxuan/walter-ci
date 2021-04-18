# walter-ci

Walter Kohl is the younger son of Helmut Kohl. Like his father he
likes to break down walls and reunify friends under one common
fundamental law.

![](./doc/helmut-kohl-1.jpg)

## How to write a good README?

See it later, once there is actually something to tell about.

## Installation

Refer `main` branch to automatically upgrade this action across all
your repositories.

``` yaml
- name: piotr-yuxuan/walter-ci
  uses: piotr-yuxuan/walter-ci@main # use branch main
```

If you are unsure how to do it, in your public GitHub repository
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
  version:
    runs-on: ubuntu-latest
    steps:
    - name: Walter Ci
      uses: piotr-yuxuan/walter-ci@main # use branch main
```
