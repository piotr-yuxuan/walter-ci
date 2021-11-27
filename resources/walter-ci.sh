set -xe
# Shockingly bad. See history for better attempts. Instead of
# downloading and installing dependencies every time, we should
# download a small, compact binary and run it directly.
cd "${GITHUB_ACTION_PATH}"
lein install

cd "${GITHUB_WORKSPACE}"
export LEIN_SNAPSHOTS_IN_RELEASE=true
WALTER_CI_VERSION=$(awk '{$1=$1};1' < "${GITHUB_ACTION_PATH}/resources/walter-ci.version")
clojure -Sdeps "{:aliases {:walter-ci {:replace-deps {com.github.piotr-yuxuan/walter-ci {:mvn/version \"${WALTER_CI_VERSION}\"}}}}}" -M:walter-ci -m piotr-yuxuan.walter-ci.main
