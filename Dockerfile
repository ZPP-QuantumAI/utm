FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 as build

WORKDIR /app

COPY . .

RUN sbt clean compile

FROM openjdk:17-slim

WORKDIR /app

COPY --from=build /app/target/scala-2.13/*.jar ./app.jar

CMD ["java", "-jar", "app.jar"]
