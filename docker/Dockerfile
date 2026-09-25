####
# Build with (from repo root):
#   docker build -f docker/Dockerfile -t backend/image .
# Run with:
#   docker run -i --rm -p 8080:8080 backend/image
####
FROM registry.access.redhat.com/ubi9/openjdk-21:1.21 AS builder

RUN mkdir project
WORKDIR /home/jboss/project
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src src
RUN mvn package -Dmaven.test.skip=true
# Find the built jar regardless of version/artifactId and normalize its name
RUN grep '^version=' target/maven-archiver/pom.properties | cut -d '=' -f2 >.env-version
RUN grep '^artifactId=' target/maven-archiver/pom.properties | cut -d '=' -f2 >.env-id
RUN mv target/$(cat .env-id)-$(cat .env-version).jar target/export-run-artifact.jar

FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.21
COPY --from=builder /home/jboss/project/target/export-run-artifact.jar /deployments/export-run-artifact.jar
EXPOSE 8080
ENTRYPOINT ["/opt/jboss/container/java/run/run-java.sh", "--server.address=0.0.0.0", "--server.port=8080"]
