FROM ubuntu:latest
COPY walter-ci.sh /walter-ci.sh
ENTRYPOINT ["/walter-ci.sh"]
