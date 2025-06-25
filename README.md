# 백엔드-프론트엔드 연결 완료 보고서

## 📋 작업 완료 사항

### 1. API 서비스 생성 (`src/main/frontend/services/api.ts`)
- 백엔드 Spring Boot API와 통신하는 서비스 함수들 구현
- 에러 처리 및 타입 안전성 확보
- 워크스페이스 관련 모든 CRUD 기능 API 연동

### 2. AuthContext 업데이트 (`src/main/frontend/AuthContext.tsx`)
- 목업 데이터에서 실제 API 호출로 전환
- 로딩 상태 및 에러 처리 추가
- 새로운 워크스페이스 관리 함수들 추가

### 3. 프론트엔드 컴포넌트 업데이트 (`src/main/frontend/App.tsx`)
- JoinWorkspaceModal: 실제 API 연동, 초대 코드 + 비밀번호 입력 방식으로 변경
- WorkspaceSidebar: 실제 워크스페이스 목록 사용, 로딩 상태 표시
- WorkspaceSettingsModal: 실제 워크스페이스 관리 기능 연동

### 4. 폴더 구조 변경
- 기존 `teammate-app-v2.0/` → `src/main/frontend/`로 이동
- Spring Boot 프로젝트 표준 구조에 맞춰 정리

## 🔧 백엔드 API 엔드포인트

| 기능 | 메서드 | 엔드포인트 | 설명 |
|------|--------|------------|------|
| 워크스페이스 생성 | POST | `/api/workspaces` | 새 워크스페이스 생성 |
| 워크스페이스 참여 | POST | `/api/workspaces/join` | 초대코드로 참여 |
| 내 워크스페이스 목록 | GET | `/api/workspaces/my` | 사용자의 워크스페이스 목록 |
| 워크스페이스 상세 | GET | `/api/workspaces/{id}` | 특정 워크스페이스 조회 |
| 워크스페이스 멤버 | GET | `/api/workspaces/{id}/members` | 멤버 목록 조회 |
| 워크스페이스 수정 | PUT | `/api/workspaces/{id}` | 이름, 아이콘, 비밀번호 수정 |
| 초대링크 생성 | POST | `/api/workspaces/{id}/invite-code` | 새 초대코드 생성 |
| 멤버 내보내기 | DELETE | `/api/workspaces/{id}/members/{userId}/kick` | 멤버 추방 |
| 멤버 차단 | DELETE | `/api/workspaces/{id}/members/{userId}/ban` | 멤버 영구 차단 |
| 워크스페이스 삭제 | DELETE | `/api/workspaces/{id}` | 워크스페이스 삭제 |

## 🚀 실행 방법

### 1. 백엔드 실행
```bash
# PickTeam 프로젝트 루트에서
./mvnw spring-boot:run
# 또는 Windows에서
mvnw.cmd spring-boot:run
```
- 서버가 http://localhost:8080 에서 실행됩니다.

### 2. 프론트엔드 실행
```bash
# 프론트엔드 폴더로 이동
cd src/main/frontend

# 의존성 설치 (최초 한 번만)
npm install

# 개발 서버 시작
npm run dev
```
- 프론트엔드가 http://localhost:5173 에서 실행됩니다.

## 📁 폴더 구조
```
PickTeam/
├── src/main/
│   ├── java/mvc/pickteam/     # 백엔드 Spring Boot 코드
│   ├── frontend/              # 프론트엔드 React 코드 (이동됨)
│   │   ├── pages/            # React 페이지 컴포넌트들
│   │   ├── services/         # API 호출 서비스들
│   │   ├── App.tsx           # 메인 App 컴포넌트
│   │   ├── AuthContext.tsx   # 인증 상태 관리
│   │   ├── package.json      # 프론트엔드 의존성
│   │   └── vite.config.ts    # Vite 설정
│   └── resources/
└── pom.xml                   # 백엔드 의존성
```

## 🎯 테스트 가능한 기능들

### 워크스페이스 관리
- ✅ **새 워크스페이스 생성** (파란색 + 버튼)
  - 워크스페이스 이름, 아이콘, 비밀번호 설정
  - 생성자가 자동으로 소유자 권한 획득
  - 초대코드 자동 생성
- ✅ **초대코드로 워크스페이스 참여** (회색 링크 버튼)
  - 초대코드 + 비밀번호 입력 방식
  - 유효성 검증 및 에러 처리
- ✅ 워크스페이스 목록 실시간 로드
- ✅ 워크스페이스 설정 (이름, 비밀번호 변경)
- ✅ 새 초대코드 생성
- ✅ 멤버 관리 (추방/차단)

### 상태 관리
- ✅ 로딩 상태 표시
- ✅ 에러 메시지 표시
- ✅ 실시간 데이터 동기화

## 🔗 API 연동 확인 방법

1. **백엔드 로그 확인**: Spring Boot 콘솔에서 API 호출 로그 확인
2. **브라우저 개발자 도구**: Network 탭에서 HTTP 요청/응답 확인
3. **데이터베이스**: H2 콘솔 또는 MySQL에서 데이터 변경 확인

## 🎮 사용자 시나리오 테스트

### 1. 워크스페이스 생성 플로우
1. 좌측 사이드바 **파란색 + 버튼** 클릭
2. 워크스페이스 이름 입력 (필수)
3. 아이콘 설정 (선택사항: 이모지, 문자 등)
4. 비밀번호 설정 (선택사항)
5. "워크스페이스 생성" 버튼 클릭
6. 생성 성공 시 자동으로 해당 워크스페이스로 이동

### 2. 워크스페이스 참여 플로우
1. 좌측 사이드바 **회색 링크 버튼** 클릭
2. 초대 코드 입력 (백엔드에서 생성된 8자리 코드)
3. 비밀번호 입력 (워크스페이스에 설정된 경우)
4. "참여하기" 버튼 클릭
5. 참여 성공 시 자동으로 해당 워크스페이스로 이동

### 3. 워크스페이스 관리 플로우
1. 워크스페이스 선택 후 우측 상단 설정 버튼 클릭
2. **초대 탭**: 초대코드 복사, 새 코드 생성
3. **보안 탭**: 워크스페이스 비밀번호 변경
4. **멤버 탭**: 멤버 목록 보기, 추방/차단 (권한 있는 경우)

## 🐛 문제 해결

### 포트 충돌
- 백엔드: 8080 포트 사용중이면 `server.port` 변경
- 프론트엔드: 5173 포트 사용중이면 Vite가 자동으로 다른 포트 할당

### CORS 에러
- `SecurityConfig.java`에서 CORS 설정이 되어있음
- 프론트엔드 포트가 변경되면 CORS 설정도 업데이트 필요

### API 연결 실패
- `services/api.ts`의 `API_BASE_URL` 확인
- 백엔드 서버가 실행중인지 확인
- 네트워크 에러 시 폴백 데이터 사용됨
