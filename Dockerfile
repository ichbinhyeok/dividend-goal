# 1. 빌드 단계 (Gradle로 자바 소스를 실행 파일로 만듦)
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY . .
# gradlew 실행 권한 부여 (중요!)
RUN chmod +x ./gradlew
# 빌드 실행 (테스트 건너뛰고 빠르게)
RUN ./gradlew clean bootJar -x test

# 2. 실행 단계 (가벼운 환경에서 실행)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# 빌드된 JAR 파일을 가져옴
COPY --from=build /app/build/libs/*.jar app.jar

# 3. 포트 설정
ENV PORT=8080
EXPOSE 8080

# 4. 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]