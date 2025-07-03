#!/bin/bash
set -Eeo pipefail

LADA_DB_SRV=${LADA_DB_SRV:-db}
LADA_DB_NAME=${LADA_DB_NAME:-lada}
LADA_DB_USER=${LADA_DB_USER:-lada}
LADA_DB_PW=${LADA_DB_PW:-lada}
LADA_DB_PORT=${LADA_DB_PORT:-5432}
LADA_JBOSS_HOME=${LADA_JBOSS_HOME:-/opt/jboss/wildfly}
LADA_JBOSS_ADMIN=${LADA_JBOSS_ADMIN:-ADMIN}
LADA_JBOSS_PW=${LADA_JBOSS_PW:-secret}

$JBOSS_HOME/bin/jboss-cli.sh <<EOF
embed-server
batch
/subsystem=datasources/data-source=lada:write-attribute(name=connection-url,value=jdbc:postgresql_postGIS://${LADA_DB_SRV}:${LADA_DB_PORT}/${LADA_DB_NAME})
/subsystem=datasources/data-source=lada:write-attribute(name=user-name,value=${LADA_DB_USER})
/subsystem=datasources/data-source=lada:write-attribute(name=password,value=${LADA_DB_PW})
run-batch
stop-embedded-server
EOF

chown -R jboss:jboss $JBOSS_HOME/standalone/log /var/log/wildfly

su - jboss -c "export JAVA_OPTS='${LADA_JAVA_OPTS}' ; /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0"
