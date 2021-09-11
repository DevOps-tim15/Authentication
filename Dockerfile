FROM kaca97/maven-repo:0.1.0 AS appServerTest
ARG STAGE=test
WORKDIR /usr/src/userauth
COPY . .

FROM maven:3.8.2-jdk-11 AS appServerBuild
ARG STAGE=dev
WORKDIR /usr/src/userauth
COPY . .
RUN mvn package -P${STAGE} -DskipTests

FROM kaca97/maven-repo:0.1.0 AS appServerRuntime
WORKDIR /app
COPY --from=appServerBuild /usr/src/userauth/target/userauth.jar ./
EXPOSE 8080
CMD java -jar userauth.jar