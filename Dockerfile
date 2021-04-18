FROM clojure:latest
COPY walter-ci.sh /walter-ci.sh
ENV JAVA_HOME=/usr/local/openjdk-11/bin
ENTRYPOINT ["/walter-ci.sh"]
