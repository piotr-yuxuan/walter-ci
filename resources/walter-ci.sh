set -xe
env

ls -hal /home/runner/work/_temp/_github_workflow/event.json
cat /home/runner/work/_temp/_github_workflow/event.json

cd "${GITHUB_ACTION_PATH}"
# Shockingly bad. See history for better attempts.
WALTER_CI_VERSION=$(awk '{$1=$1};1' < "./resources/walter-ci.version")
lein install

cd "${GITHUB_WORKSPACE}"
clojure -Sdeps "{:aliases {:walter-ci {:replace-deps {com.github.piotr-yuxuan/walter-ci {:mvn/version \"${WALTER_CI_VERSION}\"}}}}}" -M:walter-ci -m piotr-yuxuan.walter-ci.main
