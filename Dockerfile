FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 as build

WORKDIR /app

COPY . .

RUN sbt assembly

COPY /target/scala-2.13/utm-assembly-0.1.0-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]
