set -xe
cd "${GITHUB_ACTION_PATH}" || exit 1
# Shockingly bad. See history for better attempts.
lein run
