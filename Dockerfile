FROM openjdk:jdk
COPY ./target/*-standalone.jar /opt/walter-ci.jar
USER nobody
CMD exec $JAVA_HOME/bin/java $JAVA_OPTS -jar /opt/walter-ci.jar
