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
