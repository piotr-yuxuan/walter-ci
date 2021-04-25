set -xe
WALTER_CI_VERSION=$(awk '{$1=$1};1' < "${GITHUB_ACTION_PATH}/resources/walter-ci.version")

mkdir "${HOME}/.lein"
cp "${GITHUB_ACTION_PATH}/profiles.clj" "${HOME}/.lein/profiles.clj"

mkdir "${XDG_CONFIG_HOME}/.lein"
cp "${GITHUB_ACTION_PATH}/profiles.clj" "${XDG_CONFIG_HOME}/.lein/profiles.clj"

mkdir "${RUNNER_WORKSPACE}/.lein"
cp "${GITHUB_ACTION_PATH}/profiles.clj" "${RUNNER_WORKSPACE}/.lein/profiles.clj"

# Shockingly bad. See history for better attempts.
cd "${GITHUB_ACTION_PATH}"
lein install

cd "${GITHUB_WORKSPACE}"
lein help
clojure -Sdeps "{:aliases {:walter-ci {:replace-deps {com.github.piotr-yuxuan/walter-ci {:mvn/version \"${WALTER_CI_VERSION}\"}}}}}" -M:walter-ci -m piotr-yuxuan.walter-ci.main
