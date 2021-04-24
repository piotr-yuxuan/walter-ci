FROM ubuntu:latest

MAINTAINER Piotr Yuxuan <piotr-yuxuan@telecom-paris-alumni.fr>

ARG DEBIAN_FRONTEND="noninteractive"

# Fundations
RUN apt-get update
RUN apt-get install  -y curl
RUN apt-get install  -y gnupg
RUN apt-get install  -y lsb-release

# Installing some recent Java
RUN curl -fsSL https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | gpg --dearmor -o /usr/share/keyrings/adoptopenjdk-keyring.gpg
RUN echo "deb [arch=amd64 signed-by=/usr/share/keyrings/adoptopenjdk-keyring.gpg] https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/ $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/adoptopenjdk.list
RUN apt-get update
RUN apt-get install  -y $(apt-cache search openjdk-..+-jdk$ | sort -r | head -n1 | cut -d " " -f1)

# Install some recent Leiningen
RUN curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein
RUN chmod a+x /usr/local/bin/lein
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG /home/walter-ci/.m2

# Configure user
RUN adduser --disabled-password --gecos '' walter-ci
COPY ./resources/settings.xml /home/walter-ci/.m2/
COPY ./resources/profiles.clj /home/walter-ci/.lein/
RUN chown -R walter-ci:walter-ci /home/walter-ci
RUN chmod -R 755 /home/walter-ci

# Tools used by Walter
RUN apt-get install -y git
RUN apt-get install -y clojure

USER walter-ci

# Update embedded profile jars
RUN lein ancient upgrade-profiles

# Runtime execution
ENV HOME=/home/walter-ci
WORKDIR /home/walter-ci
COPY ./target/*-standalone.jar /opt/walter-ci.jar

CMD exec /bin/java -jar /opt/walter-ci.jar
