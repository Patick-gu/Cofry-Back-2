FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM tomcat:9.0-jdk21-temurin

ENV CATALINA_HOME=/usr/local/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH

RUN rm -rf $CATALINA_HOME/webapps/*

COPY --from=build /app/target/Cofry-Backend2.war $CATALINA_HOME/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]

