#docker build -t ah_serviceregistry:4.0 -f ./serviceregistry_sql/Dockerfile .
#docker run -it -d --name test ah_serviceregistry:4.0
#docker exec -it test /bin/bash
FROM openjdk:8-jdk-alpine
LABEL maintainer="zumlauf@aitia.ai"

# Install Maven
RUN apk add --no-cache curl tar bash
ARG MAVEN_VERSION=3.5.4
ARG USER_HOME_DIR="/root"
RUN mkdir -p /usr/share/maven && \
curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar -xzC /usr/share/maven --strip-components=1 && \
ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
#ENTRYPOINT ["/usr/bin/mvn"]

# Create arrowhead folder for project
RUN mkdir -p /usr/src/arrowhead
WORKDIR /usr/src/arrowhead

# Copy all the necessary files for the packaging
# Copy the common module
COPY pom.xml /usr/src/arrowhead
COPY common /usr/src/arrowhead/common
# Copy the service registry module (src + config folder)
COPY serviceregistry_sql /usr/src/arrowhead/serviceregistry

# Run maven install
WORKDIR /usr/src/arrowhead/common
RUN mvn clean install -X

WORKDIR /usr/src/arrowhead/serviceregistry
RUN mvn clean install -X #fails to find common-4.0.jar :(

#WORKDIR /usr/src/arrowhead/serviceregistry/target
#CMD ["java", "-jar", "serviceregistry_sql-4.0", "-d", "-daemon", ">", "insecure_eventhandler.log", "&"]
#RUN mvn -T 1C clean install && rm -rf target