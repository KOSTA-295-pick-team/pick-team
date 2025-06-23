# 워크스페이스 기능 구현 완료

## 개요
팀플메이트 프로젝트의 워크스페이스 관련 기능이 구현되었습니다.

## 구현된 기능
- ✅ 워크스페이스 생성
- ✅ 초대링크 생성
- ✅ 워크스페이스 멤버 내보내기
- ✅ 영구 밴
- ✅ 비밀번호 설정하기
- ✅ 초대 링크로 워크스페이스 참여하기
- ✅ 워크스페이스 멤버 목록 조회
- ✅ 워크스페이스 삭제

## 환경 설정

### .env 파일 생성
프로젝트 루트에 `.env` 파일을 생성하고 다음 내용을 추가하세요:

```env
# Server Configuration
PORT_NO=8080

# Database Configuration
DATABASE_TYPE=mysql
DB_DRIVER=com.mysql.cj.jdbc.Driver
DB_URL=localhost:3306
DB_NAME=pickteam_db
DB_USER=root
DB_PASSWORD=your_password_here
TZ=Asia/Seoul
DB_CHARACTER_ENCODING=UTF-8
DB_MYSQL_CHARACTER_SET_SERVER=utf8mb4

# JPA Configuration
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
JPA_OPEN_IN_VIEW=false

# Logging Configuration
LOG_LEVEL_ROOT=info
LOG_LEVEL_SQL=debug
LOG_LEVEL_TX=debug
LOG_LEVEL_INTERCEPTOR=trace
```

### 데이터베이스 설정
1. MySQL 서버 실행
2. `pickteam_db` 데이터베이스 생성
3. .env 파일의 DB_PASSWORD를 실제 비밀번호로 변경

## API 엔드포인트

### 워크스페이스 생성
```http
POST /api/workspaces
Headers: User-Id: {userId}
Content-Type: application/json

{
  "name": "워크스페이스 이름",
  "iconUrl": "아이콘 URL (선택사항)",
  "password": "비밀번호 (선택사항)"
}
```

### 워크스페이스 참여
```http
POST /api/workspaces/join
Headers: User-Id: {userId}
Content-Type: application/json

{
  "inviteCode": "초대코드",
  "password": "비밀번호 (필요시)"
}
```

### 사용자 워크스페이스 목록 조회
```http
GET /api/workspaces/my
Headers: User-Id: {userId}
```

### 워크스페이스 상세 조회
```http
GET /api/workspaces/{workspaceId}
Headers: User-Id: {userId}
```

### 워크스페이스 멤버 목록 조회
```http
GET /api/workspaces/{workspaceId}/members
Headers: User-Id: {userId}
```

### 워크스페이스 설정 업데이트
```http
PUT /api/workspaces/{workspaceId}
Headers: User-Id: {userId}
Content-Type: application/json

{
  "name": "새 이름 (선택사항)",
  "iconUrl": "새 아이콘 URL (선택사항)",
  "password": "새 비밀번호 (선택사항)"
}
```

### 새 초대 링크 생성
```http
POST /api/workspaces/{workspaceId}/invite-code/regenerate
Headers: User-Id: {userId}
```

### 멤버 내보내기
```http
DELETE /api/workspaces/{workspaceId}/members/{targetUserId}/kick
Headers: User-Id: {userId}
```

### 멤버 영구 밴
```http
DELETE /api/workspaces/{workspaceId}/members/{targetUserId}/ban
Headers: User-Id: {userId}
```

### 워크스페이스 삭제
```http
DELETE /api/workspaces/{workspaceId}
Headers: User-Id: {userId}
```

## 실행 방법

1. 환경 설정 완료 후 다음 명령어로 실행:
```bash
cd PickTeam
./mvnw spring-boot:run
```

2. 애플리케이션이 시작되면 `http://localhost:8080`에서 API 사용 가능

## 주의사항

- 현재 개발용으로 Spring Security가 비활성화되어 있습니다
- 실제 배포시에는 적절한 인증/인가 시스템을 구현해야 합니다
- User-Id 헤더는 임시로 사용하는 방식이며, 실제로는 JWT 토큰 등을 사용해야 합니다

## 데이터베이스 테이블

- `tb_user`: 사용자 정보
- `tb_workspace`: 워크스페이스 정보  
- `tb_workspace_member`: 워크스페이스 멤버십 관계 