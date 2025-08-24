# dontgoback-auth-server

<br/>

<p align="center">
"DontGoBack MSA 프로젝트의 모든 서비스 간 신뢰를 보장하는 OAuth 2.0 인증 서버"
</p>
<br/>

> 이 프로젝트는 '돈고백(Dont Go Back)' MSA 프로젝트의 일부입니다.  
> 전체 프로젝트의 개요 및 성과는 **[코어 서버 README](https://github.com/parkhongseok/projectDontGoBack)** 에서 확인하실 수 있습니다.

</br>

## 1. 핵심 역할

이 서버는 MSA 환경의 단일 인증 지점(Single Point of Authentication)으로서, 다음과 같은 명확한 책임을 갖습니다.

- **OAuth 2.0 인증 서버 (Authorization Server):**
  - `Client Credentials Grant` 흐름에 따라 등록된 클라이언트(서버)에 대한 인증을 수행합니다.
- **비대칭키(RS256) 기반 JWT 서명:**
  - **개인키(Private Key)를 유일하게 소유**하며, 이를 통해 안전한 JWT(Access Token)를 서명 및 발급합니다.
- **JWT 검증용 공개키 제공 API:**
  - 다른 서비스들이 토큰을 안전하게 검증할 수 있도록 **공개키(Public Key)를 외부에 제공**하는 역할을 담당합니다.

</br>

## 2. 주요 기술 구현

#### 1. **OAuth 2.0 Token API (`/msa/auth/api/token`)**

- 요청으로 받은 `clientId`와 `clientSecret`을 검증합니다.
- 검증 성공 시, JWT Claims를 생성하고 **RS256 알고리즘과 개인키로 서명**하여 토큰을 발급합니다.

#### 2. **Public Key API (`/msa/auth/api/public-key`)**

- 서버 시작 시 `.pem` 파일에서 개인키/공개키를 로드합니다.
- 요청 시, 외부에 노출되어도 안전한 **공개키를 Base64 인코딩된 텍스트** 형태로 제공합니다.

#### 3. **견고한 테스트 전략 (Robust Testing Strategy)**

- **Unit Test (`@WebMvcTest`):** Spring Security 필터를 비활성화하고, 각 Controller의 요청/응답 및 예외 처리를 Mockito를 활용하여 테스트하며 API 명세를 검증했습니다.
- **Integration Test (`@SpringBootTest`):** 실제 토큰 발급부터, 발급된 토큰을 공개키로 검증하는 전체 인증 사이클이 정상 동작하는지 통합 테스트를 통해 시스템의 신뢰도를 확보했습니다.

</br>

## 3. 기술 스택

| 구분               | 기술                                  |
| :----------------- | :------------------------------------ |
| **Backend**        | Java 21, Spring Boot, Spring Security |
| **Authentication** | jjwt (JSON Web Token for Java)        |
| **Testing**        | JUnit5, Mockito, MockMvc              |
| **DevOps & Infra** | Docker, GitHub Actions, Raspberry Pi  |

</br>

## 4. 아키텍처

### MSA 내에서의 역할

<p align="center">
  <img src="./docs/architecture/src/msa-system-architecture-overview.png" width="55%" alt="MSA 시스템 아키텍처 요약">
</p>

- Core 서버 및 Extension 서버로부터 토큰 발급 요청을 받고, 다른 모든 서버에 공개키를 제공하는 중앙 인증 허브 역할을 수행합니다.

<br/>
<br/>

## 배포 자동화 (CI/CD on Raspberry Pi)

<p align="center">
  <img src="./docs/architecture/src/06-라즈베리파이-MSA-서버-빌드-및-배포-자동화.png" width="80%" alt="MSA 서버 아키텍처 통합">
</p>

- GitHub Actions를 통해 **ARM64 아키텍처의 라즈베리파이에 맞게 크로스 빌드** 됩니다.
- 외부 컨테이너 레지스트리 없이, **SSH 스트리밍으로 Docker 이미지를 서버에 직접 전송**하여 배포 속도와 단순성을 확보했습니다.
- 배포 후 **Smoke Test**를 통해 API가 정상 응답하는지 자동으로 검증합니다.
