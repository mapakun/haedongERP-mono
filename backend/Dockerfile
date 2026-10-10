# ---------- 1단계: 빌드 (JDK + Gradle 로 jar 파일 만들기) ----------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# 의존성 목록 파일을 먼저 복사해서 라이브러리를 내려받는다.
# 소스만 바뀌었을 때는 이 단계를 다시 하지 않아서 빌드가 빨라진다 (Docker 캐시)
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ---------- 2단계: 실행 (JRE 만 있는 가벼운 이미지) ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV TZ=Asia/Seoul

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60", "-jar", "app.jar"]