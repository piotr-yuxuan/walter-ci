FROM ubuntu:latest
RUN mkdir /.m2
ENV M2_HOME=/.m2
COPY settings.xml /.m2/settings.xml
COPY walter-ci.sh /walter-ci.sh
ENTRYPOINT ["/walter-ci.sh"]
