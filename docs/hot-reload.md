# 개발 중 소스 자동 반영(Hot Reload) 설정

개발 환경에서 코드 수정 시 자동 반영되도록 다음처럼 설정했습니다.

## 적용 내용
- `spring-boot-devtools` 의존성 추가
- `local` 프로필에서 `devtools.restart`, `devtools.livereload` 활성화

## 실행 방법
아래처럼 `bootRun --continuous` 로 실행하면, 소스 변경 시 Gradle이 재컴파일하고 DevTools가 애플리케이션을 재시작합니다.

```bash
./gradlew bootRun --continuous --args='--spring.profiles.active=local'
```

## 참고
- 단순 리소스 변경(템플릿/정적 파일)은 브라우저 새로고침만으로 반영될 수 있습니다.
- Java 코드 변경은 재컴파일 후 DevTools 재시작이 필요하므로 `--continuous` 모드를 권장합니다.
