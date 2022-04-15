set -xe

mkdir "$HOME/.walter-ci"
wget --quiet "https://github.com/piotr-yuxuan/walter-ci/releases/latest/download/walter-ci-standalone.jar" -O "$HOME/.walter-ci/walter-ci-standalone.jar"
cp "${GITHUB_ACTION_PATH}/bash/askpass.sh" "$HOME/.walter-ci/askpass.sh"
cp "${GITHUB_ACTION_PATH}/bash/walter-ci.sh" "$HOME/.walter-ci/walter-ci.sh"
echo "$HOME/.walter-ci" >> $GITHUB_PATH
