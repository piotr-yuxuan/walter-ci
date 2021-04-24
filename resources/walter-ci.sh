set -xe
cd "${GITHUB_ACTION_PATH}" || exit 1
lein run
#WALTER_CI_VERSION=$(awk '{$1=$1};1' < "${GITHUB_ACTION_PATH}/resources/walter-ci.version")
#clojure -Sdeps "{:aliases {:walter-ci {:replace-deps {com.github.piotr-yuxuan/walter-ci {:mvn/version \"${WALTER_CI_VERSION}\"}}}}}" -M:walter-ci -m piotr-yuxuan.walter-ci.main
