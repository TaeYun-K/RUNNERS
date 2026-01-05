# --- Runtime only (CI에서 Gradle 빌드 후 jar만 복사해서 이미지 생성) ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# GitHub Actions에서 build/libs/*.jar가 생성된 상태라고 가정
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
