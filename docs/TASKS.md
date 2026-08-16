# TASKS

현재 Phase에서 무엇을 할지 정의한다.

요구사항의 정본은 `docs/REQUIREMENTS.md`,
현재 구조의 정본은 `docs/DESIGN.md`,
문제 확인 순서는 `docs/ROADMAP.md`다.

이 문서는 그 세 문서를 다시 쓰지 않는다.

---

# 1. 현재 Phase

```text
Phase 3
Baseline Routing 평가
```

상태:

```text
DONE
```

목표:

Phase 1 Baseline Router를 현재 Dataset으로 평가하고
실패 Case를 수집한다.

이 Phase에서는 발견한 문제를 바로 고치지 않는다.

---

# 2. Phase 2 결과

```text
Direct Model Runner로 Model 특성을 측정했다.

model-small = gemini-2.5-flash
model-large = gemini-3.5-flash

Router는 바꾸지 않았다.
strategy = BASELINE_DEFAULT
```

Phase 3는 이 Router를 바꾸지 않는다.

---

# 3. 핵심 질문

```text
Router는 어떤 Model을 선택하는가?

Simple Request에서 불필요하게 큰 Model을 선택하는가?

Reasoning Request에서 충분한 Model을 선택하는가?

Routing 결과가 Quality / Cost / Latency 조건을 만족하는가?
```

현재 Dataset에는 Capability Required Case가 없다.
이 Phase에서 Capability Case를 미리 넣지 않는다.

---

# 4. Phase 3에서 하지 않을 것

```text
Routing Policy 변경

Semantic Routing

LLM-based Routing

Fallback / Retry

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

Allowed Models / Max Cost / Max Latency를 Dataset에 새로 넣어 평가 기준을 바꾸기
```

Phase 3 Quality 기준은 기존 expectedCondition Checklist다.

---

# 6. 작업 목록

---

## T3-01. Router Evaluation 결과 확장

상태: `DONE`

목적:

Router 경로 실행 결과에 Selected Model, Quality, Latency, Cost를 남긴다.

구현 범위:

```text
EvaluationRunner

Quality Evaluator 비교

결과 파일 저장
```

하지 않을 것:

```text
Router 변경

Failure Type Enum을 Runtime에 넣기
```

완료 조건:

```text
Router 경로로 Dataset을 실행할 수 있다.

Case별 Selected Model을 확인할 수 있다.

Quality / Latency / Cost를 확인할 수 있다.

결과가 evaluation/results에 저장된다.

Test Double로 Runner Test가 있다.
```

관련:

```text
REQUIREMENTS FR-07, FR-15, FR-16
ROADMAP Phase 3
DESIGN 19.1
```

---

## T3-02. Baseline Routing 측정 실행

상태: `DONE`

목적:

실제 Router 경로로 현재 Dataset을 실행한다.

완료 조건:

```text
전체 Dataset을 Baseline Router로 실행했다.

실행 결과를 파일로 남겼다.

측정값과 추측을 구분한다.
```

실행 결과:

```text
결과 파일: evaluation/results/002-baseline-routing.json

7 / 7 selectedModel = model-small
strategy = BASELINE_DEFAULT

호출 성공 5 / 7
Quality PASS 4 / 7

실패:
simple-003 Quality FAIL (JSON 형식)
reasoning-001 HTTP 429 RATE_LIMIT
reasoning-002 HTTP 429 RATE_LIMIT
```

---

## T3-03. Experiment 기록

상태: `DONE`

목적:

Router 평가 결과를 문제 중심으로 남긴다.

파일:

```text
docs/experiments/002-baseline-routing.md
```

포함할 내용:

```text
Problem / Question

Hypothesis

Conditions

Baseline

Result

Analysis

Decision

Remaining Limitation
```

완료 조건:

```text
Case별 Selected Model을 기록했다.

Quality / Cost / Latency를 따로 기록했다.

실패 Case를 삭제하지 않았다.

Router를 바꾸지 않기로 한 결정을 명시했다.
```

---

## T3-04. DESIGN / README / TASKS 동기화

상태: `DONE`

목적:

Router Evaluation 실행 방법을 문서에 맞춘다.

완료 조건:

```text
DESIGN 19.1이 현재 Router Evaluation 결과를 반영한다.

README에서 Router Evaluation 실행 방법을 확인할 수 있다.

없는 미래 구조를 DESIGN에 넣지 않는다.
```

---

# 7. 권장 구현 순서

```text
T3-01 Runner
      ↓
T3-02 실제 측정
      ↓
T3-03 Experiment
      ↓
T3-04 문서 동기화
```

---

# 8. Phase 3 완료 조건

ROADMAP Phase 3 완료 조건과 같다.

```text
Baseline Router를 전체 Dataset으로 실행했다.

Case별 Selected Model을 확인할 수 있다.

Quality / Cost / Latency를 확인할 수 있다.

Expected Condition과 Actual Result를 비교할 수 있다.

실패 Case를 목록으로 남겼다.
```

실패를 수정하지 않고도 Phase 3은 완료될 수 있다.

측정은 기록했다. 남은 한계는 Experiment Remaining Limitation에 둔다.

---

# 9. Git Checkpoint

```text
experiment: evaluate baseline routing
```

---

# 10. 다음 Phase

Phase 3가 완료된 뒤에만 연다.

```text
Phase 4
Routing Failure 분석
```
