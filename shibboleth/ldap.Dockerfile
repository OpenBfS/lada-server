FROM debian:bullseye
LABEL maintainer tom.gottfried@intevation.de

RUN apt-get -y update
RUN apt-get -y install debconf-utils curl
RUN echo "slapd slapd/password1 password secret" | debconf-set-selections && \
    echo "slapd slapd/password2 password secret" | debconf-set-selections && \
    echo "slapd slapd/domain string ladaidp" | debconf-set-selections && \
    apt-get -y install slapd ldap-utils
RUN apt-get -y clean

ENV BASE_DIR=/usr/local/lada_ldap

ADD . ${BASE_DIR}

RUN slapadd -n0 -l ${BASE_DIR}/memberOf.ldif
RUN service slapd stop && \
    slapd -h "ldapi:// ldap://" && \
    while ! curl -s ldap://localhost:389 > /dev/null; do \
    echo waiting for ldap to start; sleep 1; done; \
    ldapadd -H ldap:/// -f ${BASE_DIR}/users.ldif -w secret -D "cn=admin,dc=ladaidp"

CMD ["/usr/sbin/slapd", "-d", "0"]
