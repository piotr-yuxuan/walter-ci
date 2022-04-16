👋 You can't use version `0.0.0` of this action. See
[Installation](#installation).

[![Build status](https://img.shields.io/github/workflow/status/piotr-yuxuan/walter-ci/Walter%20CD)](https://github.com/piotr-yuxuan/walter-ci/actions/workflows/walter-cd.yml)
[![Clojars badge](https://img.shields.io/clojars/v/com.github.piotr-yuxuan/walter-ci.svg)](https://clojars.org/com.github.piotr-yuxuan/walter-ci)
[![Clojars downloads](https://img.shields.io/clojars/dt/com.github.piotr-yuxuan/walter-ci)](https://clojars.org/com.github.piotr-yuxuan/walter-ci)
[![cljdoc badge](https://cljdoc.org/badge/com.github.piotr-yuxuan/walter-ci)](https://cljdoc.org/d/com.github.piotr-yuxuan/walter-ci/CURRENT)
[![GitHub license](https://img.shields.io/github/license/piotr-yuxuan/walter-ci)](https://github.com/piotr-yuxuan/walter-ci/blob/main/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/piotr-yuxuan/walter-ci)](https://github.com/piotr-yuxuan/walter-ci/issues)

# `piotr-yuxuan/walter-ci`

Walter CI intends to remove YAML boilerplate as much as possible while
not impeding user freedom. It does so with frugal means:

- A small set of edn->yaml reader macros and predefined _DRY_ steps to
  avoid repetition where possible;
- An action that provides standard and community-maintained tools;
- Some `GIT_*` environment variables;
- A runtime executable for operations that can not easily be expressed
  in bash.

It favours `bash` as much as possible to express simple steps.

``` clojure
{:name "My Simple Workflow"
 ;; The edn->yaml conversion JustWorks™. No magic. You are writing a
 ;; YAML GitHub Workflow file, but as edn.
 :on {:push {:branches ["main"]}}
 ;; Some reader macros consisely insert predefined data structures,
 ;; but you are free not to use them.
 :env #walter/env #{:git}
 ;; Most jobs are the same, that is to say just a sequence of
 ;; steps. `#job/wrap` take them and output a basic job.
 :jobs {:run-test #job/wrap [;; This job has two steps.
                             {:uses "piotr-yuxuan/walter-ci@main"}
                             {:run "lein test"}]
        :sort-ns #job/wrap [;; `#step` inserts a canned step defined as
		                    ;; data in `resources/steps.edn`. It is the same as 
							;; `{:uses "piotr-yuxuan/walter-ci@main"}` above.
                            #step :walter/use
                            ;; Multi-line strings may be expressed
							;; as a vector, and joined later.
                            {:run #line/join["lein sort-ns"
                                            "git add ."
                                            "git commit --message \"Sort namespace forms\""
                                            ;; Commands too may be expressed as vectors.
                                            ;; Walter command `retry` avoids network issues.
                                            #str/join["walter" "retry" "--" "git" "push"]]}]}}
```

<scherz>Walter Kohl is the younger son of Helmut Kohl. Like his father
he likes to break down walls and reunify friends under one common
fundamental law.</scherz>

![](./doc/helmut-kohl-1.jpg)

The goal of Walter is to automate and standardize CI maintenance jobs
as much as possible so that I can scale it to more than two public
personal projects on GitHub.

It's extremely tedious to maintain workflows on more than two
repositories. What if you want to make a change? What if you have had
a different idea to improve CI on some later project? I think that
losing all my time propagating changes by replication and copy/paste
is an hindrance. That's the job of a machine, so let a machine do it.

## Supported perations

- Run tests
- (not yet) Create and deploy new release
- Upgrade dependencies
- Report vulnerabilities
- (not yet) Enforce and fix reverse-domain-based project group name if
  deploying to Clojars (mandatory)
- Generate list of licenses
- (not yet) Add .java-version
- Run quality scan
- Lint files
- Sort namespaces

It doesn't require any addition to the project code, except the
installation step. It is a goal of this project to stay free from any
project-level configuration duplicate hell.
