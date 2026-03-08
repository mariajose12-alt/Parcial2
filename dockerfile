# ---------- BUILD STAGE ----------
FROM gradle:9.2.0-jdk25 AS builder

WORKDIR /app
COPY . .

RUN gradle shadowJar --no-daemon


# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY --from=builder /app/build/libs/*-all.jar app.jar

EXPOSE 7000

CMD ["java", "-jar", "app.jar"]