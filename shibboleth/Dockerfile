FROM centos:centos7

EXPOSE 80 8080 443 8443 389

ENV jetty_version=9.4.14.v20181114 \
    BASE_DIR=/usr/local/lada_shib \
    JETTY_HOME=/usr/local/lada_shib/jetty-home \
    JETTY_BASE=/usr/local/lada_shib/jetty-base \
    shib_idp_version=3.4.3 \
    JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk


ADD . ${BASE_DIR}/sources

RUN yum -y update \
    && yum -y install wget tar which java-1.8.0-openjdk-devel epel-release 389-ds-base 389-adminutil\
    && yum -y clean all

RUN mkdir -p  ${BASE_DIR}

WORKDIR ${BASE_DIR}

#LDAP, see https://github.com/UniconLabs/dockerized-idp-testbed/tree/master/ldap
RUN useradd ldapadmin \
    && rm -fr /var/lock /usr/lib/systemd/system \
    # The 389-ds setup will fail because the hostname can't reliable be determined, so we'll bypass it and then install. \
    && sed -i 's/checkHostname {/checkHostname {\nreturn();/g' /usr/lib64/dirsrv/perl/DSUtil.pm \
    # Not doing SELinux \
    && sed -i 's/updateSelinuxPolicy($inf);//g' /usr/lib64/dirsrv/perl/* \
    # Do not restart at the end \
    && sed -i '/if (@errs = startServer($inf))/,/}/d' /usr/lib64/dirsrv/perl/* \
    && setup-ds.pl --silent --file ${BASE_DIR}/sources/ds-setup.inf \
    && /usr/sbin/ns-slapd -D /etc/dirsrv/slapd-dir \
    && while ! curl -s ldap://localhost:389 > /dev/null; do echo waiting for ldap to start; sleep 1; done; \
    ldapadd -H ldap:/// -f ${BASE_DIR}/sources/users.ldif -x -D "cn=Directory Manager" -w password


#Jetty
RUN wget https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/$jetty_version/jetty-distribution-$jetty_version.tar.gz \
    && tar -xvzf jetty-distribution-$jetty_version.tar.gz \
    && mv jetty-distribution-${jetty_version} $JETTY_HOME \
    && mkdir ${JETTY_BASE} \
    && cd $JETTY_HOME \
    && keytool -genkey -noprompt -alias jetty -dname "CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown" -keystore keystore -storepass password -keypass password -keyalg RSA\
    && java -jar $JETTY_HOME/start.jar --create-startd --add-to-start=http,https,deploy,ext,annotations,jstl,rewrite,ssl

#RUN rm ${JETTY_HOME}/etc/jetty-ssl-context.xml && cp ${BASE_DIR}/sources/jetty-ssl-context.xml ${JETTY_HOME}/etc/

RUN cp ${BASE_DIR}/sources/ssl.ini ${JETTY_HOME}/start.d/

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
        -Didp.sealer.password="password" -Didp.keystore.password="password" -Didp.target.dir="${BASE_DIR}/shibboleth-idp"  -Didp.host.name="lada-idp"\
    && chown -R jetty:jetty ${BASE_DIR}/shibboleth-idp \
    && ln -s ${BASE_DIR}/shibboleth-idp /opt/shibboleth-idp \
    && rm ${BASE_DIR}/shibboleth-idp/conf/metadata-providers.xml \
    && ln -s ${BASE_DIR}/sources/metadata-providers.xml ${BASE_DIR}/shibboleth-idp/conf/metadata-providers.xml \
    && ln -s ${BASE_DIR}/sources/sp-metadata.xml ${BASE_DIR}/shibboleth-idp/metadata/sp-metadata.xml \
    && rm ${BASE_DIR}/shibboleth-idp/metadata/idp-metadata.xml \
    && ln -s ${BASE_DIR}/sources/idp-metadata.xml ${BASE_DIR}/shibboleth-idp/metadata/idp-metadata.xml \
    && rm ${BASE_DIR}/shibboleth-idp/conf/ldap.properties \
    && ln -s ${BASE_DIR}/sources/ldap.properties ${BASE_DIR}/shibboleth-idp/conf/ldap.properties \
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
RUN /usr/sbin/ns-slapd -D /etc/dirsrv/slapd-dir
USER jetty
RUN cp ${BASE_DIR}/sources/idp.xml ${JETTY_HOME}/webapps
CMD cd ${JETTY_HOME} && java -jar  ${JETTY_HOME}/start.jar
#CMD tail -f /dev/null