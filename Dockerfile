FROM clojure:latest
RUN mkdir /.m2
ENV M2_HOME=/.m2
COPY walter-ci.sh /walter-ci.sh
ENTRYPOINT ["/walter-ci.sh"]
