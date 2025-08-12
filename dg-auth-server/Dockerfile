# ========================
# 1) Build Stage
# ========================
# Gradle과 JDK가 포함된 빌드 이미지를 사용 (버전 명시)
FROM gradle:8.8-jdk21 AS build

# 작업 디렉터리 설정
WORKDIR /app

# Gradle Wrapper 복사
COPY gradle ./gradle
COPY gradlew .
RUN chmod +x ./gradlew

# ------------------------
# 1. 의존성 레이어 캐싱
# build.gradle, settings.gradle을 먼저 복사하여 의존성만 다운로드
# 소스코드 변경과 무관하게 이 레이어는 캐시를 재사용할 가능성이 높음
# ------------------------
COPY build.gradle settings.gradle ./
RUN ./gradlew --no-daemon --build-cache dependencies

# ------------------------
# 2. 소스 코드 복사 및 빌드
# 소스 코드는 의존성보다 자주 변경되므로 나중에 복사
# 필요한 모듈의 소스만 복사하여 불필요한 캐시 무효화 방지
# ------------------------
COPY dg-auth-server/src ./dg-auth-server/src
# bootJar 태스크를 실행하여 layered jar 생성
RUN ./gradlew --no-daemon --build-cache :dg-auth-server:bootJar

# 생성된 JAR에서 레이어 추출 (dependencies, application 등)
RUN java -Djarmode=layertools -jar dg-auth-server/build/libs/*.jar extract


# ========================
# 2) Runtime Stage
# ========================
# 더 구체적이고 작은 JRE 이미지 사용 (예: Ubuntu Jammy 기반)
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 보안 강화를 위해 non-root 사용자 생성
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser

# Build stage에서 추출한 레이어를 순서대로 복사
# 의존성 -> 스프링 부트 로더 -> 애플리케이션 코드 순으로 복사하여 캐시 효율 극대화
# --chown으로 파일 소유자를 non-root 사용자로 지정
COPY --from=build --chown=appuser:appgroup /app/dependencies/ ./
COPY --from=build --chown=appuser:appgroup /app/spring-boot-loader/ ./
COPY --from=build --chown=appuser:appgroup /app/snapshot-dependencies/ ./
COPY --from=build --chown=appuser:appgroup /app/application/ ./

# non-root 사용자로 전환
USER appuser

# 컨테이너 시작 명령 (Layered JAR 실행을 위해 JarLauncher 사용)
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
