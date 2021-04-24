set -xe
cd "${GITHUB_ACTION_PATH}"
lein uberjar
cp ./target/*-standalone.jar "${GITHUB_ACTION_PATH}/walter-ci.jar"

cd "${GTIHUB_WORKSPACE}"
java -jar "${GITHUB_ACTION_PATH}/walter-ci.jar"
