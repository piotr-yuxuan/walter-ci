set -xe
WALTER_CI_VERSION=$(awk '{$1=$1};1' < "${GITHUB_ACTION_PATH}/resources/walter-ci.version")
mvn dependency:get -Dartifact=com.github.piotr-yuxuan:walter-ci:"${WALTER_CI_VERSION}":jar
mvn dependency:copy -Dartifact=com.github.piotr-yuxuan:walter-ci:"${WALTER_CI_VERSION}":jar -DoutputDirectory="${GITHUB_ACTION_PATH}"
# Doesn't work as of now, as we only send small jar and no uberjar to clojars
#java -jar "walter-ci-${WALTER_CI_VERSION}".jar
clojure -Sdeps "{:aliases {:walter-ci {:replace-deps {com.github.piotr-yuxuan/walter-ci {:mvn/version \"${WALTER_CI_VERSION}\"}}}}}" -M:walter-ci -m piotr-yuxuan.walter-ci.main
