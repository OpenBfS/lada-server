FROM ubuntu:22.04

ENV jetty_version=9.4.14.v20181114 \
    BASE_DIR=/usr/local/lada_shib \
    JETTY_HOME=/usr/local/lada_shib/jetty-home \
    JETTY_BASE=/usr/local/lada_shib/jetty-base \
    shib_idp_version=3.4.8 \
    JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64 \
    DEBIAN_FRONTEND=noninteractive

RUN apt -y update && \
    apt -y install openjdk-8-jdk curl wget

ADD . ${BASE_DIR}/sources

WORKDIR ${BASE_DIR}

# Install Jetty
RUN wget https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/$jetty_version/jetty-distribution-$jetty_version.tar.gz \
    && tar -xvzf jetty-distribution-$jetty_version.tar.gz \
    && mv jetty-distribution-${jetty_version} $JETTY_HOME \
    && mkdir ${JETTY_BASE} \
    && cd $JETTY_HOME \
    && keytool -genkey -noprompt -alias jetty \
               -dname "CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown" \
               -keystore keystore -storepass password -keypass password \
               -keyalg RSA \
    && java -jar $JETTY_HOME/start.jar --create-startd \
            --add-to-start=http,https,deploy,ext,annotations,jstl,rewrite,ssl

# Download Shibboleth IDP
RUN wget https://shibboleth.net/downloads/identity-provider/$shib_idp_version/shibboleth-identity-provider-$shib_idp_version.tar.gz \
    && tar -xvzf shibboleth-identity-provider-$shib_idp_version.tar.gz \
    && mv shibboleth-identity-provider-$shib_idp_version/ \
          ${BASE_DIR}/shibboleth-sources

# Copy modified web.xml and dependencies
RUN cp ${BASE_DIR}/sources/web.xml ${BASE_DIR}/shibboleth-sources/webapp/WEB-INF/ \
    && cp ${JETTY_HOME}/lib/jetty-servlets-${jetty_version}.jar ${BASE_DIR}/shibboleth-sources/webapp/WEB-INF/lib \
    && cp ${JETTY_HOME}/lib/jetty-util-${jetty_version}.jar ${BASE_DIR}/shibboleth-sources/webapp/WEB-INF/lib


# Configure Jetty
RUN cp ${BASE_DIR}/sources/ssl.ini ${JETTY_HOME}/start.d/

#Jetty user
RUN useradd jetty -U -s /bin/false \
    && chown -R jetty:jetty ${BASE_DIR} \
    && chown -R jetty:jetty ${JETTY_BASE} \
    && chmod -R 0750 ${BASE_DIR} \
    && chmod 0755 /usr/bin/java \
    && chmod -R 750 ${JETTY_BASE} \
    && chmod -R 0775 ${BASE_DIR}/shibboleth-sources/bin \
    && sh ${BASE_DIR}/shibboleth-sources/bin/install.sh \
        -Didp.merge.properties="${BASE_DIR}/sources/idp.properties" \
        -Didp.noprompt="true" \
        -Didp.no.tidy="true" \
        -Didp.sealer.password="password" \
        -Didp.keystore.password="password" \
        -Didp.src.dir="${BASE_DIR}/shibboleth-sources" \
        -Didp.target.dir="${BASE_DIR}/shibboleth-idp" \
        -Didp.host.name="lada-idp" \
    && chown -R jetty:jetty ${BASE_DIR}/shibboleth-idp \
    && ln -s ${BASE_DIR}/shibboleth-idp /opt/shibboleth-idp \
    && rm ${BASE_DIR}/shibboleth-idp/conf/metadata-providers.xml \
    && ln -s ${BASE_DIR}/sources/metadata-providers.xml ${BASE_DIR}/shibboleth-idp/conf/metadata-providers.xml \
    && ln -s ${BASE_DIR}/sources/sp-metadata.xml ${BASE_DIR}/shibboleth-idp/metadata/sp-metadata.xml \
    && rm ${BASE_DIR}/shibboleth-idp/metadata/idp-metadata.xml \
    && ln -s ${BASE_DIR}/sources/idp-metadata.xml ${BASE_DIR}/shibboleth-idp/metadata/idp-metadata.xml \
    && rm ${BASE_DIR}/shibboleth-idp/conf/ldap.properties \
    && ln -s ${BASE_DIR}/sources/ldap.properties ${BASE_DIR}/shibboleth-idp/conf/ldap.properties \
    && rm ${BASE_DIR}/shibboleth-idp/conf/attribute-resolver.xml \
    && ln -s ${BASE_DIR}/sources/attribute-resolver.xml ${BASE_DIR}/shibboleth-idp/conf/attribute-resolver.xml \
    && rm ${BASE_DIR}/shibboleth-idp/conf/attribute-filter.xml \
    && ln -s ${BASE_DIR}/sources/attribute-filter.xml ${BASE_DIR}/shibboleth-idp/conf/attribute-filter.xml \
    && rm ${BASE_DIR}/shibboleth-idp/conf/idp.properties \
    && ln -s ${BASE_DIR}/sources/idp.properties ${BASE_DIR}/shibboleth-idp/conf/idp.properties \
    && rm -rf ${BASE_DIR}/shibboleth-idp/credentials \
    && cp -r ${BASE_DIR}/sources/credentials ${BASE_DIR}/shibboleth-idp/ \
    && chown -R jetty:jetty ${BASE_DIR} \
    && chown -R jetty:jetty ${JETTY_BASE} \
    && chmod -R 0750 ${BASE_DIR} \
    && chmod 0755 /usr/bin/java \
    && chmod -R 750 ${JETTY_BASE} \
    && chmod -R 0775 ${BASE_DIR}/shibboleth-sources/bin


RUN echo \
        "*******************************************\
        * This image is for testing purposes only.*\
        * Do not use in productive enviroments!   *\
        *******************************************"

RUN cp ${BASE_DIR}/sources/idp.xml ${JETTY_HOME}/webapps
WORKDIR ${JETTY_HOME}
CMD java -jar ./start.jar
