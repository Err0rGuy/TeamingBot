# Build
FROM focker.ir/maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -DskipTests -U dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# Runtime
FROM focker.ir/eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -ms /bin/bash appuser
COPY --from=build /build/target/*.jar /app/app.jar
USER appuser
ENV JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=40 -XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]