* Development notes

It takes time to build the image every time. On walter-ci, every push
should create a new Docker image and push it somewhere so that we can
prepare the entrypoint. Because of
https://github.community/t/docker-pull-from-public-github-package-registry-fail-with-no-basic-auth-credentials-error/16358/58
we can't use Github Package Registry for that. Too bad.

We can run a (private) action to update the Docker image version every
month.

Instead of a shell script we can just copy a jar, and put all Walter
logic in this jar.

We keep the checkout as an external action to be explicit, but
anything else will be handled inside Walter.

Use `profiles.clj`, `deps.edn`, and the great tooling from
[practicalli](https://github.com/practicalli/clojure-deps-edn) so that
`project.clj` or equivalent `deps.edn` can be reduced to the minimum.

Nice to see https://github.com/nektos/act

The way Docker layers work is a strong incentive to only append
changes at the end of the file

We should be able to use either the action, either directly the Docker
image in the workflow. But that's only syntactic sugar.

Here for the steps to undertake, just find a good topological order
for this graph. It doesn't even have to be stable. Perhaps we could
use a Clojure hierarchy? We have steps that must be followed by
something else in case is success or failure (checkout version before
git commit), and steps that may only happen if some succeded (deploy
clojars if tests pass). Should we allow exceptions to break
everything, or shall we report the step as failed?

Use babashka.process to follow logs. Right now we just wait, it's not
tenable in the long run.

It's a bit boring to add so much env vars for each project. The main
function should accept argument from the command line, a github token,
a target repo, and does its magic with secrets. Here the goal is to
stay simple, not to be easy. However, being as simple as possible at
the end should make life more comfortable.

Before deploying anything, rigorous checks should be made. If the
repository is private, something is probably wrong.

Doc and dev experience should be massively scaled up (now it's below
terrible), but first code should works reasonably fine. Also, an
action on my profile README.md and to show the most recently active
projects and display a link, the description, and the number of stars.

Cross-repo secret access are planned, but not released yet:
https://github.com/github/roadmap/issues/74

Shall we try to use #!/usr/bin/env bb as a shebang?

To push tags, commits, and so on, see: git push --atomic

Some real work should be undertaken to find the precise meaning of
work, task, run, job, step.

Put a safeguard at the beginning. Walter should not be responsible for
more than three commits in a row, or it would risk infinite loops – we
currently have max two commits

Cache all the things to avoid boil the oceans:
https://docs.github.com/en/actions/guides/caching-dependencies-to-speed-up-workflows
If we use dependency caching use parallel jobs honestly it would
probably be faster to push to GH than to run manually steps like lint,
and so on.

Also, I should create an action to replicate and sync Walter config
across different repos. This would be more elegant and more efficient
since it would run on each Walter update, and no longer needs to be
triggered by the managed repos. Also, it would allow to duplicate
Walter secrets to the manage repository. Once I can download a binary
of Walter then I can use way better the Action GUI, workflows, jobs,
and so on. That would be better :-)

As a result, just the install step could be merely:

``` yaml
name: Install Walter CI
on:
  push:
    branches: '*'
jobs:
  install-walter-ci:
    steps:
      - name: Install Walter CI
        uses: piotr-yuxuan/walter-ci@main
```

or just use the command line tool to do something like in the new
repository to manage:

``` zsh
walter-ci install
```

We should have a command

``` zsh
walter-ci --local --auto-pilot
```

to do everything locally, possibly in parallel :-)
