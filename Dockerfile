FROM ubuntu:latest

MAINTAINER Piotr Yuxuan <piotr-yuxuan@telecom-paris-alumni.fr>

ARG DEBIAN_FRONTEND="noninteractive"

# Fundations
RUN apt-get update
RUN apt-get install -y curl
RUN apt-get install -y gnupg

# Install some recent Docker
RUN apt-get install -y lsb-release
RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
RUN echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list
RUN apt-get update
RUN apt-get install -y docker-ce docker-ce-cli
# Docker daemon must be started at runtime, and user added to group

# Tools used by Walter
RUN apt-get install -y git
RUN apt-get install -y clojure

# Installing some recent Java
RUN curl -fsSL https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | gpg --dearmor -o /usr/share/keyrings/adoptopenjdk-keyring.gpg
RUN echo "deb [arch=amd64 signed-by=/usr/share/keyrings/adoptopenjdk-keyring.gpg] https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/ $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/adoptopenjdk.list
RUN apt-get update
RUN apt-get install -y $(apt-cache search openjdk-..+-jdk$ | sort -r | head -n1 | cut -d " " -f1)

# Prepare runtime execution
COPY ./target/*-standalone.jar /opt/walter-ci.jar
USER nobody
# This is not a standard GitHub Action path
WORKDIR /walter-ci
CMD exec $JAVA_HOME/bin/java $JAVA_OPTS -jar /opt/walter-ci.jar
