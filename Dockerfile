FROM hseeberger/scala-sbt:11.0.12_1.5.5_2.13.8

WORKDIR /app

COPY . .

RUN sbt compile

CMD ["sbt", "run"]