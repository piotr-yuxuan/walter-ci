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
not impeding user freedom. It does so with frugal means but vast powers:

- No configuration for managed repositories. It's fine if you can't
  let Walter manage your edge-case repository, just write and maintain
  your own workflows. You can still use Walter action as a base
  though!
- Leverage community tools as much as possible. « Not invented here »
  is a feature, not a bug so Walter code footprint is quite small. You
  may use the whole infrastructure of GitHub Actions, the vast
  ecosystem of read-to-use actions and awesome Clojure-minded
  command-line tools like Babashka, `deps.edn`, and lein plugins.
- A small set of edn->yaml reader macros and predefined _DRY_ steps to
  avoid repetition where possible;
- An action that provides standard and community-maintained tools;
- Some `GIT_*` environment variables;
- A runtime executable for operations that can not easily be expressed
  in bash.

It favours `bash` as much as possible to express simple steps.

``` clojure
{:name "Walter Perf"
 :on {:repository_dispatch nil
      :workflow_dispatch {:inputs {:walter-version {:description "Walter version"
                                                    :required false
                                                    :type :string}}}
      :schedule [;; Run only once a week, on Tuesday to save some money. Otherwise: "28 3 * * 2,5".
                 {:cron "28 3 * * 2"}]}
 :concurrency {:group "walter-perf"
               :cancel-in-progress true}
 :env #walter/env #{:git :walter-version}
 :jobs {:run-perf-tests
        {:runs-on "ubuntu-latest"
         :name "Run performance tests"
         :steps [{:uses "piotr-yuxuan/walter-ci@main"}
                 {:run "lein with-profile +walter/kaocha,+kaocha,+perf run -m kaocha.runner --focus-meta :perf"
                  :continue-on-error true}
                 {:run "git add ./doc/perf"
                  :continue-on-error true}
                 #step :git/diff
                 {:run "git commit --message \"Performance test report\""
                  :if "steps.diff.outcome == 'failure'"}
                 #step :git/push]}}}
```

`<scherz>`Walter Kohl is the younger son of Helmut Kohl. Like his father
he likes to break down walls and reunify friends under one common
fundamental law.`</scherz>`

![](./doc/helmut-kohl-1.jpg)

The goal of Walter is to automate and standardize CI maintenance jobs
as much as possible so that I can scale it to more than two public
personal projects on GitHub.

It's extremely tedious to maintain workflows on more than two
repositories. What if you want to make a change? What if you have had
a different idea to improve CI on some later project? I think that
losing all my time propagating changes by replication and copy/paste
is an hindrance. That's the job of a machine, so let a machine do it.

## Supported operations

- Performance tests, unit tests with great expressive power.
- Report vulnerabilities
- Create and deploy releases
- User-defined release version
- Conditional checks and human approval before a release
- Distinguish between Walter action and `walter` cli tool, allow the
  use of different/fixed versions.
- Upgrade dependencies
- (not yet) Enforce and fix reverse-domain-based project group name if
  deploying to Clojars (mandatory)
- Generate list of licenses
- (not yet) Add .java-version
- Run quality scan
- Lint files
- Sort namespace forms.

It doesn't require any addition to the project code, except the
installation step. It is a goal of this project to stay free from any
project-level configuration duplicate hell.
