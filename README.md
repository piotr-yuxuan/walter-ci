👋 You can't use version `0.0.0` of this action. See
[Installation](#installation).

# `piotr-yuxuan/walter-ci`

Walter Kohl is the younger son of Helmut Kohl. Like his father he
likes to break down walls and reunify friends under one common
fundamental law.

![](./doc/helmut-kohl-1.jpg)

This action runs an opinionated set of standard steps for all my
Clojure projects, defined by conventions and no configuration.

The goal of Walter is to automate and standardize CI maintenance jobs
as much as possible so that I can scale it to more than two public
personal projects on GitHub.

It's extremely tedious to maintain workflows on more than two
repositories. What if you want to make a change? What if you have had
a different idea to improve CI on some later project? I think that
losing all my time propagating changes by replication and copy/paste
is an hindrance. That's the job of a machine, so let a machine do it.

## Workflow

It offers standard actions for all my Clojure projects on GitHub,
depending on their type :

- Clojure library with Leiningen
- Clojure library with `deps.edn`
- Public page with `shadow-cljs` and Leiningen
- Public page with `shadow-cljs` and `deps.edn`

It is able to perform certain actions on repositories:

- Run tests
- Create and deploy new release
- Upgrade dependencies
- Report vulnerabilities
- Enforce and fix reverse-domain-based project group name if deploying
  to Clojars (mandatory)
- Generate list of licenses
- Run quality scan
- Lint files
- Sort namespaces

It doesn't require any addition to the project code, except the
installation step. It doesn't involve configuration.

## Installation

Refer the `main` branch to automatically upgrade this action across
all your repositories. This makes the most of Walter, otherwise you
lose the benefits of it.

``` yaml
- name: Walter CI
  uses: piotr-yuxuan/walter-ci@main # will download and use Docker image
```

``` yaml
- name: Walter CI
  uses: docker://piotryuxuan/walter-ci:latest # Directly use the Docker image
```

If you are unsure about how to do it, see how Walter is self-hosted
here:
[.github/workflows/walter-ci.yml](.github/workflows/walter-ci.yml).

## How to write a good README?

See it later, once there is actually something to tell about.

## License

Distributed under GNU GPL, version 3, or any later version. See
`LICENSE` and `GPL_ADDITION.md`.  For you reference, text of the
license is also available here:
https://www.gnu.org/licenses/gpl-3.0.txt".
