# ========================
# 1) Build Stage
# ========================
# Gradle과 JDK가 포함된 빌드 이미지를 사용
FROM gradle:8.8-jdk21 AS build

# 작업 디렉터리 설정
WORKDIR /app


# ------------------------
# Gradle 캐시를 최대한 활용하기 위해
# gradle 관련 파일만 먼저 복사
# ------------------------
# 컨텍스트가 dg-auth-server 이므로, 상위 경로의 파일을 복사하도록 경로 수정
COPY ../gradle ./gradle
COPY ../gradlew ../settings.gradle ../build.gradle ./
RUN chmod +x ./gradlew
# Gradle 의존성만 다운로드 (캐시용)
RUN ./gradlew --no-daemon dependencies || true

# ------------------------
# 이후 소스 전체 복사 → JAR 빌드
# ------------------------
COPY . .
RUN ./gradlew --no-daemon :dg-auth-server:bootJar




# ========================
# 2) Runtime Stage
# ========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# build stage에서 생성한 JAR만 복사
COPY --from=build /app/dg-auth-server/build/libs/*.jar app.jar

# 컨테이너 시작 명령
ENTRYPOINT ["java","-jar","/app/app.jar"]
