FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy build output (expects: mvn -DskipTests package)
COPY target/*.jar app.jar

ENV JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
