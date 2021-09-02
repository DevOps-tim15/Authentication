FROM maven:3.8.2-jdk-11 AS appServerTest
ARG STAGE=test
WORKDIR /usr/src/userauth
COPY . .

FROM maven:3.8.2-jdk-11 AS appServerBuild
ARG STAGE=dev
WORKDIR /usr/src/userauth
COPY . .
RUN mvn package -P${STAGE} -DskipTests

FROM openjdk:11.0-jdk AS appServerRuntime
WORKDIR /app
COPY --from=appServerBuild /usr/src/userauth/target/userauth.jar ./
EXPOSE 8080
CMD java -jar userauth.jar