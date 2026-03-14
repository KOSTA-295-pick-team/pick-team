# OpenJdk 17 Deprecated에 따른 베이스 이미지 교체
# 기존: FROM openjdk:17-jdk-slim
# 변경: Eclipse Temurin JDK 17 
FROM eclipse-temurin:17-jdk-alpine

# 2. 컨테이너 내에서 앱이 실행될 디렉토리 지정
WORKDIR /app

# 3. 로컬의 빌드 결과물 JAR 파일을 컨테이너에 복사
#    - target 디렉토리 내의 특정 JAR 파일을 명시적으로 복사
#    - 이름이 변경되면 이 부분도 함께 수정해주는 방식이 안정적
COPY target/*-SNAPSHOT.jar app.jar

# 4. 애플리케이션 실행
#    - Asia/Seoul 시간대 설정
#    - UTF-8 문자 인코딩 설정 (한글 깨짐 방지)
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]