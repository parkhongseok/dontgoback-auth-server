# ========================
# 1) Build Stage
# ========================
# Gradle과 JDK가 포함된 빌드 이미지를 사용 (버전 명시)
FROM gradle:8.8-jdk21 AS build

# 작업 디렉터리 설정
WORKDIR /app

# ------------------------
# 1. 의존성 레이어 캐싱 (가장 안 바뀌는 부분)
# build.gradle 등 설정 파일을 먼저 복사하여 의존성만 다운로드합니다.
# 소스 코드가 변경되어도 이 레이어는 캐시를 재사용할 가능성이 높습니다.
# ------------------------
# 빌드 컨텍스트(dg-auth-server) 내의 파일들을 복사합니다.
COPY gradle ./gradle
COPY gradlew settings.gradle build.gradle ./
RUN chmod +x ./gradlew
# --build-cache 옵션으로 Gradle 빌드 캐시도 활용합니다.
RUN ./gradlew --no-daemon --build-cache dependencies

# ------------------------
# 2. 소스 코드 복사 및 빌드 (자주 바뀌는 부분)
# ------------------------
# 소스 코드만 복사하여 불필요한 캐시 무효화를 방지합니다.
COPY src ./src 
RUN ./gradlew --no-daemon --build-cache bootJar

# ------------------------
# 3. 빌드된 Layered JAR에서 레이어 추출
# ------------------------
RUN java -Djarmode=layertools -jar build/libs/*.jar extract

# ========================
# 2) Runtime Stage
# ========================
# 더 작고 안정적인 JRE 이미지 사용 (예: Ubuntu Jammy 기반)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 보안 강화를 위해 non-root 사용자 생성
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

# Build stage에서 추출한 레이어를 순서대로 복사합니다.
# 의존성 -> 스프링 부트 로더 -> 애플리케이션 코드 순으로 복사하여 캐시 효율을 극대화합니다.
# --chown으로 파일 소유자를 non-root 사용자로 지정합니다.
COPY --from=build --chown=appuser:appgroup /app/dependencies/ ./
COPY --from=build --chown=appuser:appgroup /app/spring-boot-loader/ ./
COPY --from=build --chown=appuser:appgroup /app/snapshot-dependencies/ ./
COPY --from=build --chown=appuser:appgroup /app/application/ ./

# non-root 사용자로 전환
USER appuser

# 컨테이너 시작 명령 (Layered JAR 실행을 위해 JarLauncher 사용)
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
