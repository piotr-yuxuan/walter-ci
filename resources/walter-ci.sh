set -xe
# Unfathomably bad. See history for better attempts. Instead of
# downloading and installing dependencies every time, we should
# download a small, compact binary and run it directly.
cp "${GITHUB_ACTION_PATH}/resources/profiles.clj" "${LEIN_HOME}/profiles.clj"
export WALTER_CI_JAR="${GITHUB_ACTION_PATH}/walter-ci-standalone.jar"
# FIXME We should take advantage of GitHub action caching.
wget --quiet https://github.com/piotr-yuxuan/walter-ci/releases/latest/download/walter-ci-standalone.jar -O "${WALTER_CI_JAR}"
cd "${GITHUB_WORKSPACE}"
java -jar "${WALTER_CI_JAR}"
