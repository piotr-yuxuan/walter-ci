# Development notes

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

This kind of reports really is cool!
https://app.codecov.io/gh/sicmutils/sicmutils/compare/350/changes

For projects that produce a binary, use `pandoc md roff` to generate documentation. 

## Rethink

- To add a project to Walter, add the project coordinates to a list in
  Walter repo. This triggers Walter itself. It can access its own
  token values, and set them in managed repositories. The installation
  adds `walter-ci.yml` to the target repo and when project is publis
  also adds a hook to fix commit times. This will trigger an action.

- One could also imagine to use a GitHub action that would use
  GITHUB_TOKEN to update Walter list, but in such case it wouldn't
  scale to more than one user. Opening a pull request looks the
  simplest and most direct.

- There should be separate use cases:
  1. The fastet possible path to release: build, test,
     compile/uberjar, new tag, atomic-push, reploy artifacts,
     trigger-doc
  2. Release: when commit first line is consistently named « Released
     x.y.x: abc » then draft a release with this name and description
     below, (or default to tag name) and publish it on Github. It is
     only specific to GitHub as Clojars only knows about tags. Add
     social media integrations (Twitter, Reddit) so that each released
     is advertised and there is an opportunity to get feedback.
  3. Personnal user grooming: early every day launch parallel workflow
     to update dependencies, sort namespaces, analyse vulnerabilities,
     list licenses, update-versions, conform repository, upload
     social-media preview, add sponsors, run some mutation testing
     (export result as a message under relevant commit), run code
     coverage and post it as a comment to the commit. If no README.md
     then generate a good-enough skeleton. If a fork try to rebase
     over latest upstream and open a PR if not straightforward. If
     performance tests are available (are GH workers suitable for
     that?) then run them and commit report in relevant folder. When
     running for personal README repo take latest public
     contributions, sort them by stars, commit the updated list if any
     change, and show updated timestamp.
  4. From walter-ci repo itself, upon any release, forcibly update the
     managed in-repo `walter-ci.yml`.
  5. Whenever a new file is added to
     `piotr-yuxuan/piotr-yuxuan.github.io` consider it as a blog
     article and publish a GitHub release so that the news feed is
     shown a preview. I don't really write blog articles, but I think
     perhaps it could be helpful or fun to others. Obviously as such
     blog would be advertised and hosted on GitHub, the content should
     be focus on technical matters only, otherwise substack or
     anything else would be a better medium. Also, social media
     integration would allow to publish a link on Twitter.
  6. Try to automatically merge PR from dependabot.
  
https://github.com/babashka/neil

I feel like every developers like Bruno or
https://github.com/logicblocks always reinvent different and
non-interoperable answers to the same set of issues:
- Configuration
- Logging
- CLI
- App state management
- Browser-side data retrieval
- other?

Instead of creating my own set of answers to these issues I could just
list them, and present what makes each unique.

## Small steps

- Even if it has to be run dynamically every time, get to the smallest
  clean state in which `(-main)` can be run. Don't care about config
  or anything. You'll still be able to recover code in git later.
- Then, forward dummy workflow to managed repositories. Consider that
  you will only manage repos from one user. Don't try to bootstrap
  Walter CI. Keep simple, stupid. Such dummy workflow will print the
  time and value of a public secret.
- Then for selected secrets forward them to the managed repositories.
- Now update the dummy workflow to some simple actions (form existing
  GitHub Actions in Market) on every pushes. See above for these
  simple actions.
- Expand Walter CI code so that it can commit a version update on
  every push to master (with Clojure code).

At this point it has a pretty good shape to be quickly expandable in
any direction.

Use https://github.com/phronmophobic/snowball and show you've thought
about dependencies and jar size.
