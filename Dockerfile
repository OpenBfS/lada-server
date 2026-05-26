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

# Add system user for building and running LADA server
ENV LADA_HOME=/opt/lada LADA_UID=999 LADA_USER=lada
RUN useradd -rmd $LADA_HOME -u $LADA_UID $LADA_USER
USER $LADA_USER

# Download dependencies before adding sources to leverage build cache
ENV SRC=$LADA_HOME/lada-server
ADD --chown=$LADA_UID pom.xml $SRC/
RUN --mount=type=cache,target=$LADA_HOME/.m2,uid=$LADA_UID \
    mvn -q -f $SRC/pom.xml dependency:go-offline

#
# Add LADA-server repo
#
ADD --chown=$LADA_UID . $SRC
WORKDIR $SRC

#
# Build and deploy LADA-server
#
ENV JBOSS_HOME=$LADA_HOME/wildfly
RUN --mount=type=cache,target=$LADA_HOME/.m2,uid=$LADA_UID \
    mvn -q -Dwildfly.provisioning.dir=$JBOSS_HOME package

# Persist local Maven repository in image (speed up mvn commands in container)
RUN --mount=type=cache,target=$LADA_HOME/.m2,uid=$LADA_UID \
    cp -a $LADA_HOME/.m2 $LADA_HOME/m2
RUN mv $LADA_HOME/m2 $LADA_HOME/.m2

#
# Wildfly setup specific for LADA
#
RUN ln -fs $PWD/wildfly/standalone.conf $JBOSS_HOME/bin/

HEALTHCHECK CMD [ $(curl -sfw '%{http_code}' http://localhost:8080/lada-server/rest/version) = 401 ] || exit 1

ENTRYPOINT ["/opt/lada/lada-server/docker-entrypoint.sh"]
