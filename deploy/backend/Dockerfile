FROM gradle:8.7-jdk21 AS builder
WORKDIR /workspace

COPY settings.gradle.kts build.gradle.kts /workspace/
COPY apps/backend /workspace/apps/backend

RUN gradle :apps:backend:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/apps/backend/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
