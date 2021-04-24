set -x

env
pwd
id
lein deps
ls -hal
export DEBUG=true
lein uberjar
ls -hal target
echo 'blah blah' > blah.txt
cat blah.txt
rm blah.txt
docker build -t piotryuxuan/walter-ci:latest .
