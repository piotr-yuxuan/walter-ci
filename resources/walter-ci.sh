set -x

env
pwd
id
lein deps
ls -hal
export DEBUG=true

cd "${GITHUB_ACTION_PATH}" || exit 1
pwd
lein run
