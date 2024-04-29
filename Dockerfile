FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 as builder

WORKDIR /app

COPY . .

RUN sbt assembly

FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=builder /app/target/scala-2.13/utm-assembly-0.1.0-SNAPSHOT.jar ./your-app.jar

RUN apt update && \
    apt install --yes --assume-yes python3 && \
    apt install --yes --assume-yes python3-pip && \
    apt install --yes --assume-yes unzip

EXPOSE 8080

CMD ["java", "-jar", "./your-app.jar"]
