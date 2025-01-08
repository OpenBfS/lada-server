#
# Dockerfile for jboss wildfly application server cutomized for usage in the
# BfS-Lada project
#

FROM debian:bookworm
LABEL maintainer=raimund.renkert@intevation.de

#
# install packages
#
RUN apt-get update -y && \
    apt-get install -y --no-install-recommends \
            libxml2-utils curl ca-certificates-java openjdk-17-jdk-headless \
            libpostgis-java libjts-java \
            git maven


#
# Set ENV for pacakge versions
ENV GEOLATTE_GEOM_VERSION=1.9.0
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/

RUN echo "Building Image using GEOLATTE_GEOM_VERSION=${GEOLATTE_GEOM_VERSION}."

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

#
# Wildfly setup specific for LADA
#
RUN mkdir -p $JBOSS_HOME/modules/org/postgres/main

ENV MVN_REPO=https://repo1.maven.org/maven2
ENV WFLY_MODULES=$JBOSS_HOME/modules/system/layers/base
ENV HIBERNATE_MODULE=$WFLY_MODULES/org/hibernate/main
RUN HIBERNATE_VERSION=$(xmllint --xpath "//*[local-name()='hibernate.version']/text()" $SRC/pom.xml); \
    curl -s $MVN_REPO/org/hibernate/orm/hibernate-spatial/${HIBERNATE_VERSION}/hibernate-spatial-${HIBERNATE_VERSION}.jar >\
        $HIBERNATE_MODULE/hibernate-spatial.jar;

RUN curl -s $MVN_REPO/org/geolatte/geolatte-geom/${GEOLATTE_GEOM_VERSION}/geolatte-geom-${GEOLATTE_GEOM_VERSION}.jar >\
        $HIBERNATE_MODULE/geolatte-geom.jar

RUN ln -s /usr/share/java/postgresql.jar \
       $JBOSS_HOME/modules/org/postgres/main/
RUN ln -s /usr/share/java/postgis-jdbc.jar \
       $JBOSS_HOME/modules/org/postgres/main/
RUN ln -s /usr/share/java/postgis-geometry.jar \
       $JBOSS_HOME/modules/org/postgres/main/
RUN ln -s /usr/share/java/jts-core.jar \
       $HIBERNATE_MODULE/jts-core.jar

# Download dependencies before adding sources to leverage build cache
RUN mvn -q -f $SRC/pom.xml dependency:go-offline

#
# Add LADA-server repo
#
ADD . $SRC
WORKDIR $SRC

RUN ln -s $PWD/wildfly/postgres-module.xml \
       $JBOSS_HOME/modules/org/postgres/main/module.xml
RUN ln -fs $PWD/wildfly/hibernate-module.xml \
       $HIBERNATE_MODULE/module.xml
# The jdbcadapters need to know the postgres module to cope with PGeometry
RUN sed -i '/<\/dependencies>/i         <module name="org.postgres"/>' \
    $WFLY_MODULES/org/jboss/ironjacamar/jdbcadapters/main/module.xml
RUN ln -fs $PWD/wildfly/standalone.conf $JBOSS_HOME/bin/

RUN $JBOSS_HOME/bin/jboss-cli.sh --file=wildfly/commands.cli

#
# Build and deploy LADA-server
#
RUN mvn -q package && \
    mv target/lada-server-*.war \
       $JBOSS_HOME/standalone/deployments/lada-server.war && \
    touch $JBOSS_HOME/standalone/deployments/lada-server.war.dodeploy

#
# This will boot WildFly in the standalone mode and bind to all interface
#
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", \
     "-bmanagement=0.0.0.0"]
