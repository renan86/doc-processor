FROM openjdk:10
MAINTAINER Renan Alberto de Souza <renan_souza@icloud.com>

VOLUME /tmp
ADD target/doc-processor.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]