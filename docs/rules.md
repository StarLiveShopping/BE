# 협업 가이드

## Issue 발행

- 작업 전 내가 어떤 작업을 할 것인지에 대해 팀원들에게 공유합니다.
- e.g.

  ```markdown
  ## 공부방 생성 모달 구현

  Description

  - 생성 버튼을 클릭하면 모달이 열리고, 공부방 정보를 입력하여 새로운 공부방을 생성한다.

  Task

  - [ ] 공통 모달 컴포넌트 구현
  - [ ] 공부방 정보 입력 form 구현
  - [ ] ...
  ```

## Commit

- 작업 내용을 적절한 단위로 묶어 commit 합니다.
- Commit Message는 컨벤션을 따릅니다.

### Convention

- Type
  ```markdown
  - feat: 새로운 기능 개발
  - fix: 버그 수정
  - refactor: 코드 개선
  - rename: 단순 파일명, 변수명 수정
  - docs: 문서 수정
  - chore: 개발 환경 설정, 기타 작업
  ```
  - e.g. `[이슈 키] feat: 공부방 생성 모달 추가`

## Pull Requests

- Origin repository를 fork하여 작업을 진행합니다.
- main branch에서 branch를 생성하여 작업을 진행합니다.
- Issue를 가지고 개발한 branch를 본인의 repository에 push합니다.
- Origin repository에서 Pull Request를 생성합니다.
- 동료의 리뷰를 받고 merge합니다.

### Convention

- Branch (임시)
  ```
  {Type}/{이슈 키}-{구현 기능}
  ```
  - e.g. `feat/STAR-9-init-dev`
- Pull Request Title
  ```
  [{이슈 키}] {Type}: {제목}
  ```
  - e.g. `[STAR-9] chore: next.js tailwind 초기 설정`
