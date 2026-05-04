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

RUN WILDFLY_VERSION=$(xmllint --xpath "//*[local-name()='wildfly.version']/text()" $SRC/pom.xml); \
    curl -Ls \
    https://github.com/wildfly/wildfly/releases/download/${WILDFLY_VERSION}/wildfly-${WILDFLY_VERSION}.tar.gz\
    | tar zx && mv wildfly-${WILDFLY_VERSION} /opt/jboss/wildfly

ENV JBOSS_HOME=/opt/jboss/wildfly

RUN $JBOSS_HOME/bin/add-user.sh admin secret --silent

EXPOSE 8080 9990 80

# Download dependencies before adding sources to leverage build cache
RUN mvn -q -f $SRC/pom.xml dependency:go-offline

#
# Add LADA-server repo
#
ADD . $SRC
WORKDIR $SRC

#
# Wildfly setup specific for LADA
#
RUN ln -fs $PWD/wildfly/standalone.conf $JBOSS_HOME/bin/

RUN $JBOSS_HOME/bin/jboss-cli.sh --file=wildfly/commands.cli

#
# Build and deploy LADA-server
#
RUN mvn -q package && \
    mv target/lada-server-*.war \
       $JBOSS_HOME/standalone/deployments/lada-server.war && \
    touch $JBOSS_HOME/standalone/deployments/lada-server.war.dodeploy

HEALTHCHECK CMD [ $(curl -sfw '%{http_code}' http://localhost:8080/lada-server/rest/version) = 401 ] || exit 1

ENTRYPOINT ["/usr/src/lada-server/docker-entrypoint.sh"]
