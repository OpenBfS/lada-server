FROM centos:centos7

ENV jetty_version=9.4.14.v20181114 \
    BASE_DIR=/usr/local/lada_shib \
    JETTY_HOME=/usr/local/lada_shib/jetty-home \
    JETTY_BASE=/usr/local/lada_shib/jetty-base \
    shib_idp_version=3.4.3 \
    JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk


ADD . ${BASE_DIR}/sources

RUN yum -y update \
    && yum -y install wget tar which java-1.8.0-openjdk-devel\
    && yum -y clean all

RUN mkdir -p  ${BASE_DIR}

WORKDIR ${BASE_DIR}

#Jetty
RUN wget https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/$jetty_version/jetty-distribution-$jetty_version.tar.gz \
    && tar -xvzf jetty-distribution-$jetty_version.tar.gz \
    && mv jetty-distribution-${jetty_version} $JETTY_HOME \
    && mkdir ${JETTY_BASE} \
    && java -jar $JETTY_HOME/start.jar --create-startd --add-to-start=http,https,deploy,ext,annotations,jstl,rewrite

RUN rm ${BASE_DIR}/sources/jetty-ssl-context.xml && ln -s ${BASE_DIR}/sources/jetty-ssl-context.xml ${JETTY_HOME}/etc/

RUN cp ${BASE_DIR}/sources/start.ini ${JETTY_BASE}

#Shibboleth IDP
RUN wget https://shibboleth.net/downloads/identity-provider/latest/shibboleth-identity-provider-$shib_idp_version.tar.gz \
    && tar -xvzf shibboleth-identity-provider-$shib_idp_version.tar.gz \
    && mv shibboleth-identity-provider-$shib_idp_version/ ${BASE_DIR}/shibboleth-sources

#Jetty user
RUN useradd jetty -U -s /bin/false \
    && chown -R jetty:jetty ${BASE_DIR} \
    && chown -R jetty:jetty ${JETTY_BASE} \
    && chmod -R 0750 ${BASE_DIR} \
    && chmod 0755 /usr/bin/java \
    && chmod -R 750 ${JETTY_BASE} \
    && chmod -R 0775 ${BASE_DIR}/shibboleth-sources/bin \
    && sh ${BASE_DIR}/shibboleth-sources/bin/install.sh \
        -Didp.sealer.password="password" -Didp.keystore.password="password" -Didp.target.dir="${BASE_DIR}/shibboleth-idp"  -Didp.host.name="lada-idp:8080"\
    && chown -R jetty:jetty ${BASE_DIR}/shibboleth-idp \
    && ln -s ${BASE_DIR}/shibboleth-idp /opt/shibboleth-idp

RUN echo \
        "*******************************************\
        * This image is for testing purposes only.*\
        * Do not use in productive enviroments!   *\
        *******************************************"
USER jetty
RUN cp ${BASE_DIR}/sources/idp.xml ${JETTY_HOME}/webapps
#CMD java -jar  ${JETTY_HOME}/start.jar
CMD tail -f /dev/null