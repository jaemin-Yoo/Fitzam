# 저장소 가이드라인

## 프로젝트 구조 및 모듈 구성
- `app/`: 메인 Android 앱 모듈(Compose UI, data, DI, resources).
- `app/src/main/java/com/jaemin/fitzam/`: Kotlin 소스(`ui/`, `data/`, `model/`, `di/`).
- `app/src/main/res/`: Android 리소스(drawables, values, XML).
- `app/src/test/`: 로컬 유닛 테스트(JUnit).
- `app/src/androidTest/`: 계측 테스트(AndroidX).
- Flavors: `dev`, `staging`, `prod`는 `app/src/<flavor>/` 하위.
- `docs/`: README에 사용하는 프로젝트 이미지.

## 빌드, 테스트 및 개발 명령
저장소 루트에서 실행:
- `./gradlew assembleDevDebug`: dev 디버그 APK 빌드.
- `./gradlew testDevDebugUnitTest`: dev 디버그 로컬 유닛 테스트 실행.
- `./gradlew connectedDevDebugAndroidTest`: 디바이스/에뮬레이터에서 계측 테스트 실행.
- `./gradlew assembleProdRelease`: 프로덕션 릴리스 빌드(서명 환경 변수 필요).

릴리스 서명 환경 변수: `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

## 코딩 스타일 및 네이밍 규칙
- Kotlin + Jetpack Compose, 표준 Kotlin 스타일 준수.
- 들여쓰기: 공백 4칸, 탭 금지.
- Compose: 컴포저블은 `PascalCase`, 함수/변수는 `camelCase`.
- 리소스: drawables/XML values는 `snake_case`.
- Android Studio 포맷터 사용(레포 전용 포맷터 없음).

## 테스트 가이드
- 유닛 테스트: `app/src/test/`의 JUnit(예: `ExampleUnitTest.kt`).
- 계측 테스트: `app/src/androidTest/`의 AndroidX(예: `ExampleInstrumentedTest.kt`).
- 테스트 이름은 `*Test` 접미사 사용, 패키지 경로는 프로덕션 코드와 정렬.

## 커밋 및 PR 가이드
- 커밋 메시지는 타입 접두사 사용(예: `Feat:`, `Chore:`, `Refactor:`, `Test`).
- 커밋은 범위를 작게 유지하고 설명적으로 작성(무관 변경 섞기 금지).
- PR에는 짧은 요약, 가능하면 이슈 링크, UI 변경 시 스크린샷 포함.

## 구성 참고
- Firebase 설정 파일은 `app/src/<flavor>/google-services.json`에 flavor별로 위치.
- Product flavors: `dev`, `staging`, `prod`(`app/build.gradle.kts` 참고).

## 에이전트 지침 (언어)
- 사용자가 다른 언어를 명시적으로 요청하지 않는 한, 모든 사용자 응답은 한국어로 작성.

## 에이전트 지침 (한글 인코딩)
- 모든 파일 저장은 UTF-8(BOM 없음)으로 한다.
- 한글이 포함된 파일을 수정할 때는 UTF-8(BOM 없음)을 반드시 유지한다.
- Codex는 UTF-8을 명시하여 읽고 쓴다(예: PowerShell `Get-Content -Encoding UTF8`, `Set-Content -Encoding UTF8`).
- 변경 후 한글이 깨지면(예: "�"), 마지막 정상 버전으로 되돌린 뒤 UTF-8로 다시 적용한다.
- 한글이 포함된 Kotlin/Gradle/리소스 파일을 다룰 때는 UTF-8로 전체 파일을 다시 쓰는 방식을 선호한다.

## 에이전트 지침 (줄바꿈)
- 모든 텍스트 파일의 줄바꿈은 CRLF로 유지한다.
- Codex는 파일 저장 전 LF를 CRLF로 변환한다.

