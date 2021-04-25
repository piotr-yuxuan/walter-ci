set -xe
# Shockingly bad. See history for better attempts. Instead of
# downloading and installing dependencies every time, we should
# downlowd the uberjar and run it directly. However, currently the
# deploy tasks only deploy thin jars. Could we bend that?
cd "${GITHUB_ACTION_PATH}"
lein install

cp "${GITHUB_ACTION_PATH}/profiles.clj" "${LEIN_HOME}/profiles.clj"
cd "${GITHUB_WORKSPACE}"
WALTER_CI_VERSION=$(awk '{$1=$1};1' < "${GITHUB_ACTION_PATH}/resources/walter-ci.version")
clojure -Sdeps "{:aliases {:walter-ci {:replace-deps {com.github.piotr-yuxuan/walter-ci {:mvn/version \"${WALTER_CI_VERSION}\"}}}}}" -M:walter-ci -m piotr-yuxuan.walter-ci.main
