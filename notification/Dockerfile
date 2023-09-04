FROM openjdk:11
VOLUME /tmp
EXPOSE 9082
ADD ./target/notification-0.0.1-SNAPSHOT.jar ms-notification.jar
ENTRYPOINT ["java", "-jar","/ms-notification.jar"]

