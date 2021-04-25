set -xe
WALTER_CI_VERSION=$(awk '{$1=$1};1' < "${GITHUB_ACTION_PATH}/resources/walter-ci.version")
cp "${GITHUB_ACTION_PATH}./resources/profiles.clj" "${HOME}/.lein/profiles.clj"
lein ancient upgrade-profiles

# Shockingly bad. See history for better attempts.
cd "${GITHUB_ACTION_PATH}"
lein install

cd "${GITHUB_WORKSPACE}"
clojure -Sdeps "{:aliases {:walter-ci {:replace-deps {com.github.piotr-yuxuan/walter-ci {:mvn/version \"${WALTER_CI_VERSION}\"}}}}}" -M:walter-ci -m piotr-yuxuan.walter-ci.main
