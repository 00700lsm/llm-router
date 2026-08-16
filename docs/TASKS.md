# TASKS

현재 Phase에서 무엇을 할지 정의한다.

요구사항의 정본은 `docs/REQUIREMENTS.md`,
현재 구조의 정본은 `docs/DESIGN.md`,
문제 확인 순서는 `docs/ROADMAP.md`다.

이 문서는 그 세 문서를 다시 쓰지 않는다.

---

# 1. 현재 Phase

```text
Phase 4
Routing Failure 분석
```

상태:

```text
DONE
```

목표:

Phase 3에서 발견한 실패가
Router 문제인지 다른 계층의 문제인지 분리한다.

이 Phase에서는 Routing Policy를 바꾸지 않는다.

---

# 2. Phase 3 결과

```text
Router 경로로 Dataset 7 Case를 실행했다.

7 / 7 selectedModel = model-small
strategy = BASELINE_DEFAULT

호출 성공 5 / 7
Quality PASS 4 / 7

실패:
simple-003 Quality FAIL (JSON 형식)
reasoning-001 HTTP 429 RATE_LIMIT
reasoning-002 HTTP 429 RATE_LIMIT
```

Phase 4는 이 실패를 분류한다. Router는 바꾸지 않는다.

비교에 쓰는 Direct Model 결과는 Phase 2 Experiment다.

```text
docs/experiments/001-model-baseline.md
docs/experiments/002-baseline-routing.md
```

새 Provider 호출을 이 Phase의 완료 조건으로 두지 않는다.

---

# 3. 핵심 질문

```text
실패한 Case의 Failure Type은 무엇인가?

Router가 현재 정책과 다르게 Model을 선택했는가?

같은 실패가 Model Quality / Prompt / Provider 문제인가?

Cost / Latency Failure가 실제로 있는가?

Capability Mismatch Case가 현재 Dataset에 있는가?
```

---

# 4. Phase 4에서 하지 않을 것

```text
Routing Policy 변경

Semantic Routing

LLM-based Routing

Cascade

Threshold 조정

Prompt 대규모 수정

Fallback / Retry

Failure Type Enum을 Runtime에 넣기

Quality Judge를 Runtime Routing에 사용

Dataset Expected를 결과에 맞춰 수정

실패 Case 삭제

Capability / Long Context Case 선제 추가
```

---

# 5. Human Gate

다음을 바꾸려면 구현 전에 Human Gate를 연다.

```text
Routing Policy 변경

Quality 기준을 Runtime Routing에 사용

Evaluation Dataset Expected를 결과 맞춰 수정

Failure Type을 Runtime Decision에 사용

simple-003 형식 FAIL을 Routing 신호로 쓰기
```

이 Phase의 분류는 Experiment 판단이다.
분류 결과가 바로 Policy 변경은 아니다.

---

# 6. 작업 목록

---

## T4-01. 실패 Case 분류

상태: `DONE`

목적:

Phase 3 실패와 Phase 2 Direct Model 결과를 같은 Case로 맞춰
Failure Type을 분리한다.

확인 범위:

```text
Request Type

Selected Model

Routing Reason

Router 경로 Quality / errorCode

Direct Model Quality

Failure Type
```

하지 않을 것:

```text
Router 변경

새 Evaluation Runner 경로 추가

Failure Type을 Chat API 응답에 넣기
```

완료 조건:

```text
Phase 3 실패 Case를 분류했다.

Routing Failure와 Model Quality Failure를 구분했다.

Cost / Latency Failure를 별도로 확인했다.

유형별 실제 Case 존재 여부를 기록했다.
```

관련:

```text
REQUIREMENTS FR-17, FR-18
ROADMAP Phase 4
DESIGN 21
```

---

## T4-02. Experiment 기록

상태: `DONE`

목적:

분류 결과를 문제 중심으로 남긴다.

파일:

```text
docs/experiments/003-routing-failure-analysis.md
```

완료 조건:

```text
Case별 Failure Type을 기록했다.

측정값과 해석을 구분했다.

실패 Case를 삭제하지 않았다.

Router를 바꾸지 않기로 한 결정을 명시했다.
```

---

## T4-03. DESIGN / README / TASKS 동기화

상태: `DONE`

목적:

Failure 분류가 Experiment에서 이뤄진 현재 상태를 문서에 맞춘다.

완료 조건:

```text
DESIGN 21이 Runtime에 Failure Type이 없다는 점과
Experiment 분류 위치를 반영한다.

README에서 Experiment 003을 확인할 수 있다.

없는 미래 구조를 DESIGN에 넣지 않는다.
```

---

# 7. 권장 구현 순서

```text
T4-01 실패 Case 분류
      ↓
T4-02 Experiment
      ↓
T4-03 문서 동기화
```

---

# 8. Phase 4 완료 조건

ROADMAP Phase 4 완료 조건과 같다.

```text
Baseline 실패 Case를 분류했다.

Routing Failure와 Model Quality Failure를 구분했다.

Cost / Latency Failure를 별도로 확인했다.

실패 유형별 실제 Case가 존재하는지 확인했다.

다음 Phase에서 검토할 문제를 정리했다.
```

분류만으로 Phase 4는 완료될 수 있다.
Routing Policy를 바꾸지 않고도 완료될 수 있다.

---

# 9. Git Checkpoint

```text
experiment: analyze routing failures
```

---

# 10. 다음 Phase

Phase 4가 완료된 뒤에만 연다.

```text
Phase 5
Quality / Cost / Latency Trade-off 확인
```
