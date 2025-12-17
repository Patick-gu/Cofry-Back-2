FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM tomcat:9.0-jdk21-temurin

ENV CATALINA_HOME=/usr/local/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH
ENV PORT=8080

RUN rm -rf $CATALINA_HOME/webapps/*

COPY --from=build /app/target/Cofry-Backend2.war $CATALINA_HOME/webapps/ROOT.war

# Cria script para configurar porta dinamicamente (compatível com Render)
RUN echo '#!/bin/bash\n\
set -e\n\
PORT=${PORT:-8080}\n\
echo "=== Configurando Tomcat para porta: $PORT ==="\n\
if [ -f "${CATALINA_HOME}/conf/server.xml" ]; then\n\
    sed -i "s/port=\"8080\"/port=\"${PORT}\"/g" ${CATALINA_HOME}/conf/server.xml\n\
    echo "Server.xml atualizado: porta ${PORT}"\n\
else\n\
    echo "ERRO: server.xml não encontrado!"\n\
    exit 1\n\
fi\n\
echo "=== Iniciando Tomcat na porta $PORT ==="\n\
exec catalina.sh run' > /usr/local/bin/start-tomcat.sh && \
    chmod +x /usr/local/bin/start-tomcat.sh

EXPOSE 8080

CMD ["/usr/local/bin/start-tomcat.sh"]

