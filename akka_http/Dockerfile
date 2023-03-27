FROM openjdk:8-jdk-alpine

COPY target/scala-2.12/akka_2.12-0.1.jar /app/akka_2.12-0.1.jar

WORKDIR /app

CMD ["java", "-jar", "akka_2.12-0.1.jar"]