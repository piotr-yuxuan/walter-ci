set -x

env
pwd
id
lein deps 1> /dev/null
ls -hal
export DEBUG=true
WALTER_CI_VERSION=$(awk '{$1=$1};1' < "${GITHUB_ACTION_PATH}/resources/walter-ci.version")
export WALTER_CI_VERSION
echo "WALTER_CI_VERSION=$WALTER_CI_VERSION"
clojure -Sdeps "{:aliases {:walter-ci {:replace-deps {com.github.piotr-yuxuan/walter-ci {:mvn/version \"${WALTER_CI_VERSION}\"}}}}}" -M:walter-ci -m piotr-yuxuan.walter-ci.main
git status
lein deploy clojars
lein deploy github
