# 패키지 정리 방안

## 문제 인식
- URL은 `/api/security/**` 인데 구현 일부가 `stock` 패키지에 남아 있으면 경계가 모호해집니다.
- `Controller`만 이동하면 DTO/Service/Config와 네이밍이 불일치해 유지보수 비용이 올라갑니다.

## 권장 원칙
1. **엔드포인트 소유 패키지와 구현 패키지를 일치**시킵니다.
2. 외부 연동(키움) 관련 객체는 같은 루트 아래로 묶습니다.
3. 설정 prefix, 클래스명, 테스트 경로를 함께 정리합니다.

## 이번 정리안(적용)
- `com.example.baseweb.stock.*` → `com.example.baseweb.security.kiwoom.*`로 이동
- `KiwoomStockService` → `KiwoomTokenService`로 변경
- `KiwoomSecurityController` → `KiwoomTokenController`로 변경
- 설정 prefix: `app.stock.kiwoom` → `app.security.kiwoom`
- 테스트 패키지도 동일하게 이동

## 대안
- 도메인 기준을 더 강하게 가져가려면 `com.example.baseweb.integration.kiwoom`로 묶고,
  `security`에서는 controller만 두는 방식도 가능합니다.
- 다만 현재 요구(`/api/security`)와 빠른 일관성 확보 목적에는
  `security.kiwoom` 일원화가 가장 단순합니다.
