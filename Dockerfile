FROM azul/zulu-openjdk:8

LABEL VENDOR="Angus Hamill" MAINTAINER="angus.hamill@r3.com"

EXPOSE 8080

WORKDIR /opt/cortex

RUN mkdir -p /opt/cortex /home/cortex \
  && groupadd -g 1000 -r cortex \
  && useradd -r -u 1000 -g cortex cortex \
  && chgrp -R 0 /opt/cortex \
  && chmod -R g=u /opt/cortex \
  && chown -R cortex:cortex /opt/cortex /home/cortex

USER cortex

VOLUME /opt/cortex/cordapps

COPY build/libs/cortex-server.jar /opt/cortex/cortex-server.jar

CMD ["java", "-jar", "/opt/cortex/cortex-server.jar"]
