#
# Dockerfile for jboss wildfly application server cutomized for usage in the
# BfS-Lada project
#

FROM debian:trixie
LABEL maintainer=tom@intevation.de

#
# install packages
#
RUN apt-get update -y && \
    apt-get install -y --no-install-recommends \
            libxml2-utils curl ca-certificates-java openjdk-21-jdk-headless \
            libpostgis-java \
            git maven

#
# Set up Wildfly
#
RUN mkdir /opt/jboss

ENV SRC=/usr/src/lada-server

ADD pom.xml $SRC/

ENV JBOSS_HOME=/opt/jboss/wildfly

EXPOSE 8080 9990 80

# Download dependencies before adding sources to leverage build cache
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -f $SRC/pom.xml dependency:go-offline

#
# Add LADA-server repo
#
ADD . $SRC
WORKDIR $SRC

#
# Build and deploy LADA-server
#
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -Dwildfly.provisioning.dir=$JBOSS_HOME package

#
# Wildfly setup specific for LADA
#
RUN ln -fs $PWD/wildfly/standalone.conf $JBOSS_HOME/bin/

HEALTHCHECK CMD [ $(curl -sfw '%{http_code}' http://localhost:8080/lada-server/rest/version) = 401 ] || exit 1

ENTRYPOINT ["/usr/src/lada-server/docker-entrypoint.sh"]
