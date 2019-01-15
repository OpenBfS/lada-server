#!/bin/bash

shibVersion=3.4.3
if [ ! -f shibboleth-identity-provider-$shibVersion.tar.gz ]
    then wget https://shibboleth.net/downloads/identity-provider/latest/shibboleth-identity-provider-$shibVersion.tar.gz
fi
tar -xzf shibboleth-identity-provider-$shibVersion.tar.gz
cd shibboleth-identity-provider-$shibVersion
./bin/install.sh -Didp.sealer.password="password" -Didp.keystore.password="password"


