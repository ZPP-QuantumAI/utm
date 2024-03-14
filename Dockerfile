FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0

WORKDIR /app

COPY . .

RUN sbt assembly

COPY /target/scala-2.13/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
