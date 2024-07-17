FROM debian:bullseye
LABEL maintainer tom.gottfried@intevation.de

RUN apt-get -y update
RUN apt-get -y install debconf-utils curl
RUN echo "slapd slapd/password1 password secret" | debconf-set-selections && \
    echo "slapd slapd/password2 password secret" | debconf-set-selections && \
    echo "slapd slapd/domain string ladaidp" | debconf-set-selections && \
    apt-get -y install slapd
RUN apt-get -y clean

ENV BASE_DIR=/usr/local/lada_ldap

ADD . ${BASE_DIR}

RUN slapadd -l ${BASE_DIR}/users.ldif

CMD ["/usr/sbin/slapd", "-d", "0"]
