set -xe
WALTER_CI_VERSION=$(awk '{$1=$1};1' < "${GITHUB_ACTION_PATH}/resources/walter-ci.version")

cp "${GITHUB_ACTION_PATH}/profiles.clj" "${LEIN_HOME}/profiles.clj"

# Shockingly bad. See history for better attempts.
cd "${GITHUB_ACTION_PATH}"
lein install

cd "${GITHUB_WORKSPACE}"
lein help
clojure -Sdeps "{:aliases {:walter-ci {:replace-deps {com.github.piotr-yuxuan/walter-ci {:mvn/version \"${WALTER_CI_VERSION}\"}}}}}" -M:walter-ci -m piotr-yuxuan.walter-ci.main
