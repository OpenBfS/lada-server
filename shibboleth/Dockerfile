FROM jetty:9.3
ENV DEBIAN_FRONTEND noninteractive
USER root
EXPOSE 80 8080 443
ADD . /opt/shibboleth/
WORKDIR /opt/shibboleth
RUN echo \
"*******************************************\n\
* This image is for testing purposes only.*\n\
* Do not use in productive enviroments!   *\n\
*******************************************"
RUN sh install_shib_idp.sh

WORKDIR $JETTY_BASE
RUN cp /opt/shibboleth/start.ini $JETTY_HOME/start.ini \
    && cp /opt/shibboleth/http.ini $JETTY_BASE/start.d/ \
    && cp /opt/shibboleth/idp.xml $JETTY_HOME/webapps/
#USER jetty
RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,stats
