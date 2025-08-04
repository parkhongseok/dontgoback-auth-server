# MSA 대응을 위한 비대칭키 인증 전략

Date: 2025-08-02  
Status: Accepted

## 맥락

DontGoBack 프로젝트는 단일 백엔드에서 기능별 책임을 분리하는 **마이크로서비스 아키텍처(MSA)** 로 전환 중입니다.  
그 일환으로, **서버 간 인증 토큰 발급**과 **공개키 제공**을 전담하는 **`dontgoback-auth-server`** 를 별도 서비스로 구성하였습니다.

기존에는 사용자-서버 간 인증을 위해 **HS256 기반 JWT**를 사용해왔지만,  
**서버 간 통신**에서는 다음과 같은 이유로 보안 및 확장성 측면에서 적합하지 않았습니다:

- 대칭키 기반 구조는 키 노출 리스크가 높음
- 복수 서비스 간 신뢰 구축이 어려움

이에 따라, **RSA 기반 RS256 (비대칭키)** 방식을 도입하기로 결정하였습니다.

<br/>
<br/>

## 결정

다음과 같은 기준에 따라 `dontgoback-auth-server`의 인증 전략을 설계하였습니다:

1. **서버 간 신뢰 보장 - 비대칭키 사용**

   - `auth-server`는 사전에 등록된 client 정보를 확인한 후,
     **비공개키(private key)** 로 JWT를 서명합니다.
   - JWT를 사용하는 쪽(ex: core-server, extension-server)은  
     **공개키(public key)** 를 통해 토큰을 검증할 수 있습니다.

2. **공개/비공개 키의 안전한 분리**

   - `.pem` 파일로 RSA 키를 관리하며, private key는 외부에 노출되지 않도록 합니다.
   - 공개키는 `/msa/auth/public-key` 엔드포인트로 제공합니다.

3. **신뢰된 클라이언트만 요청 가능하도록 제어**

   - `clientId`, `clientSecret` 기반으로 요청자 식별
   - 인증된 client만 토큰 발급 가능
   - 해당 정보는 `.yml` 또는 `.env`를 통해 주입

4. **단일 책임의 경량 마이크로서비스 구조**

   - `dontgoback-auth-server`는 인증 토큰 발급 및 공개키 제공에만 집중
   - 도메인 DB는 없으며, 확장성에 따라 추후 상태 관리 기능 추가 예정

5. **테스트 가능한 구조 설계**

   - RSA 키, 클라이언트 정보 등은 테스트 전용 프로파일 분리
   - MockMvc + Mockito 기반 단위 테스트로 컨트롤러 검증

<br/>
<br/>

## 결과

- `auth-server`는 MSA 환경에서 **중앙 인증 인프라**로 작동합니다.
- 모든 마이크로서비스는 하나의 공개키로 JWT를 검증함으로써 **보안성과 관리 효율성**을 확보합니다.
- 핵심 보안 자산인 private-key는 절대 외부에 노출되지 않으며,  
  공개키는 안전하게 공유되어 서비스 간 인증 신뢰를 형성합니다.
- 테스트 및 배포 환경에서도 키를 안전하게 주입할 수 있도록 **`.yml + .gitignore + .env` 구조**를 적용하였습니다.

> 참고:  
> 키 로딩: `PemKeyLoader`  
> 클라이언트 검증: `ClientAuthProperties`

```yml
# application.yml 예시

jwt:
  issuer: ${JWT_ISSUER}
  private-key-path: ${JWT_PRIVATE_KEY_PATH}
  public-key-path: ${JWT_PUBLIC_KEY_PATH}

clients:
  dontgoback-core-server: ${AUTH_CLIENT_CORE_SECRET}
  dontgoback-extension-server: ${AUTH_CLIENT_EXTENSION_SECRET}
```

```shell
# .gitignore 예시

.env
application-prod.yml
/secrets/*.pem
```

