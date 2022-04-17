# `piotr-yuxuan/walter-ci`

![](./doc/helmut-kohl-1.jpg)

`<scherz>`Walter Kohl is the younger son of Helmut Kohl. Like his father
he likes to break down walls and reunify friends under one common
fundamental law.`</scherz>`

[![Build status](https://img.shields.io/github/workflow/status/piotr-yuxuan/walter-ci/Walter%20CD)](https://github.com/piotr-yuxuan/walter-ci/actions/workflows/walter-cd.yml)
[![Clojars badge](https://img.shields.io/clojars/v/com.github.piotr-yuxuan/walter-ci.svg)](https://clojars.org/com.github.piotr-yuxuan/walter-ci)
[![Clojars downloads](https://img.shields.io/clojars/dt/com.github.piotr-yuxuan/walter-ci)](https://clojars.org/com.github.piotr-yuxuan/walter-ci)
[![cljdoc badge](https://cljdoc.org/badge/com.github.piotr-yuxuan/walter-ci)](https://cljdoc.org/d/com.github.piotr-yuxuan/walter-ci/CURRENT)
[![GitHub license](https://img.shields.io/github/license/piotr-yuxuan/walter-ci)](https://github.com/piotr-yuxuan/walter-ci/blob/main/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/piotr-yuxuan/walter-ci)](https://github.com/piotr-yuxuan/walter-ci/issues)

**Problem:** It's extremely tedious to maintain workflows on more than two
repositories. What if you want to make a change? What if you have had
a different idea to improve CI on some later project? I think that
losing all my time propagating changes by replication and copy/paste
is an hindrance. That's the job of a machine, so let a machine do it.


**Goal:** The goal of Walter is to automate and standardise code
grooming as much as possible so that an individual contributor can
scale to more than two projects on GitHub.

**Solution:** Walter is an open-source Clojure-oriented CICD
system. It intends to remove YAML boilerplate as much as possible
while not impeding user freedom. It contains:

- A GitHub action `"piotr-yuxuan/walter-ci@main"` that brings useful
  commands like `clojure` CLI, `leiningen`, Babashka, some Leiningen
  profiles, the amazing Practicalli's
  [deps.edn](https://github.com/practicalli/clojure-deps-edn) and
  Walter's own executable: `walter`.
- Walter's own executable contains code that is just difficult enough
  but not complex. For example, forwarding secrets or updating
  workflow files from Walter to other managed repositories.
- Predefined, opinionated workflows, expressed in edn. They are the
  same for all managed repositories. They will all behave the same
  way, as controlled by Walter. They will be maintained in sync so you
  may avoid the hell of Jenkinsfile that diverges over time.

It uses frugal means to yield vast power:

- An action that provides standard and community-maintained tools;
- No configuration for managed repositories. It's fine if Walter can't
  manage your edge-case repository, just write and maintain your own
  workflows. Walter's action will still provide a convenient base
  though.
- Leverage community tools as much as possible. « Not invented here »
  is a feature, not a bug so Walter code footprint is quite small.
- A small set of edn->yaml reader macros and predefined _DRY_ steps to
  avoid repetition where possible;
- Some `GIT_*` environment variables;
- A runtime executable for operations that can not easily be expressed
  in bash.
- Favour `bash` as much as possible to express simple, composable
  steps.

## Supported operations

- Performance tests, unit tests with great expressive power
- Report vulnerabilities
- Create and deploy releases
- User-defined release version
- Conditional checks and human approval before a release
- Distinguish between Walter action and `walter` cli tool, allow the
  use of different/fixed versions
- Upgrade dependencies
- (not yet) Enforce and fix reverse-domain-based project group name if
  deploying to Clojars (mandatory)
- Generate list of licenses
- (not yet) Add .java-version
- Run quality scan
- Lint files
- Sort namespace forms

It doesn't require any addition to the project code, except the
installation step. It is a goal of this project to stay free from any
project-level configuration duplicate hell.
