# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# ---- Runtime stage (Red Hat UBI, OpenShift-friendly) ----
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.21

ENV LANGUAGE='en_US:en' \
    JAVA_OPTS_APPEND='-Dserver.address=0.0.0.0' \
    JAVA_APP_JAR='/deployments/app.jar'

# Copy fat jar; chown to default jboss user (185) + root group so
# OpenShift's arbitrary UID (member of root group) can read it.
COPY --from=build --chown=185:0 /app/target/*.jar /deployments/app.jar
RUN chmod 664 /deployments/app.jar

EXPOSE 8080
USER 185

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS_APPEND -jar /deployments/app.jar"]
