# TASKS

현재 Phase에서 무엇을 할지 정의한다.

요구사항의 정본은 `docs/REQUIREMENTS.md`,
현재 구조의 정본은 `docs/DESIGN.md`,
문제 확인 순서는 `docs/ROADMAP.md`다.

이 문서는 그 세 문서를 다시 쓰지 않는다.

---

# 1. 현재 Phase

```text
Phase 7
Capability / Provider Failure 확인
```

상태:

```text
DONE
```

목표:

Model Capability와 Provider Failure가
현재 Router 동작에 어떤 영향을 주는지 확인한다.

Fallback / Retry를 먼저 넣지 않는다.
Routing Policy를 바꾸지 않는다.

---

# 2. Phase 6 결과

```text
현재 정책 위반 ROUTING_FAILURE는 없다.

Semantic / LLM Routing은 현재 문제에서 사용하지 않았다.

Baseline Dataset 7 Case는 그대로 둔다.
```

Phase 7은 기존 Dataset Expected를 바꾸지 않는다.
Capability / Long Context는 별도 확인이다.

---

# 3. 핵심 질문

```text
현재 Catalog에서 Capability Mismatch가 발생하는가?

Router는 Capability / Context Limit을 보는가?

Long Context Request에서 현재 동작은 무엇인가?

Provider Failure 시 현재 시스템은 무엇을 반환하는가?

Retry / Fallback이 필요한가?
```

---

# 4. Phase 7에서 하지 않을 것

```text
Fallback 구현

Retry 구현

Capability 기반 후보 제외 구현

Routing Policy 변경

Semantic / LLM Routing

Dataset.json Expected 수정

기존 7 Case 삭제

Model 추가 / 제거

Catalog Capability 값을 실험용으로 바꾸기
```

---

# 5. Human Gate

다음을 바꾸려면 구현 전에 Human Gate를 연다.

```text
Fallback / Retry Policy

Capability 기반 Routing

Model 추가 / 제거 / 역할 변경

Routing Policy 변경

evaluation/dataset.json Expected 변경
```

현재 동작을 테스트로 재현하는 것은 Human Gate가 아니다.

---

# 6. 작업 목록

---

## T7-01. Capability / Long Context 확인

상태: `DONE`

목적:

현재 Catalog와 Router가 Capability / Context Limit을
어떻게 다루는지 확인한다.

확인 범위:

```text
Catalog Capability 값

Router가 toolCalling / structuredOutput / contextLimit를 쓰는지

미지원 Model을 고를 수 있는지

Long Context에서 현재 선택
```

하지 않을 것:

```text
Capability Filter 구현

evaluation/dataset.json에 Case 추가
```

완료 조건:

```text
Capability Mismatch 존재 여부를 기록했다.

Long Context에서 현재 동작을 기록했다.

Test로 Router가 Capability를 무시함을 재현했다.
```

관련:

```text
REQUIREMENTS FR-08, FR-09
ROADMAP Phase 7.1, 7.2
DESIGN 5, 6
```

---

## T7-02. Provider Failure 재현

상태: `DONE`

목적:

Provider Failure 시 현재 시스템이 실패를 숨기지 않는지 확인한다.

확인 범위:

```text
RATE_LIMIT

PROVIDER_TIMEOUT

PROVIDER_ERROR

호출 횟수 (Retry 없음)
```

하지 않을 것:

```text
Retry

Fallback

새 live Provider 호출을 완료 조건으로 두기
```

완료 조건:

```text
Provider Failure를 재현했다.

현재 실패 동작을 기록했다.

Fallback / Retry 필요성을 근거로 판단했다.
```

관련:

```text
REQUIREMENTS FR-18, FR-19, NFR-10
ROADMAP Phase 7.3
```

---

## T7-03. Experiment 기록

상태: `DONE`

파일:

```text
docs/experiments/006-capability.md
docs/experiments/007-provider-failure.md
```

완료 조건:

```text
측정값과 해석을 구분했다.

도입하지 않았다면 이유를 적었다.
```

---

## T7-04. DESIGN / README / TASKS 동기화

상태: `DONE`

완료 조건:

```text
DESIGN에 Fallback 구조를 현재 구현처럼 넣지 않는다.

README에서 Experiment 006 / 007을 확인할 수 있다.
```

---

# 7. 권장 구현 순서

```text
T7-01 Capability / Long Context
      ↓
T7-02 Provider Failure
      ↓
T7-03 Experiment
      ↓
T7-04 문서 동기화
```

---

# 8. Phase 7 완료 조건

ROADMAP Phase 7 완료 조건과 같다.

```text
Capability Mismatch Case를 확인했다.

Long Context Case를 확인했다.

Provider Failure를 재현했다.

현재 실패 동작을 기록했다.

Fallback / Retry 필요성을 근거로 판단했다.

적용했다면 Failure Case를 다시 실행했다.
```

적용하지 않고도 Phase 7은 완료될 수 있다.

---

# 9. Git Checkpoint

```text
experiment: verify model capability routing

experiment: reproduce provider failures
```

---

# 10. 다음 Phase

Phase 7가 완료된 뒤에만 연다.

```text
Phase 8
Routing 구조 재평가
```
