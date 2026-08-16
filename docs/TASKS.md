# TASKS

현재 Phase에서 무엇을 할지 정의한다.

요구사항의 정본은 `docs/REQUIREMENTS.md`,
현재 구조의 정본은 `docs/DESIGN.md`,
문제 확인 순서는 `docs/ROADMAP.md`다.

이 문서는 그 세 문서를 다시 쓰지 않는다.

---

# 1. 현재 Phase

```text
Phase 2
Model별 Baseline 특성 측정
```

상태:

```text
IN PROGRESS
```

목표:

Router를 평가하기 전에 각 Model이 현재 Dataset에서
실제로 어떤 Quality / Latency / Token / Cost 특성을 가지는지 측정한다.

이 Phase에서는 Router를 개선하지 않는다.

---

# 2. Phase 1 결과

```text
Baseline Router 존재
strategy = BASELINE_DEFAULT
모든 일반 Request → Configured Default Model
```

Phase 2는 이 Router를 바꾸지 않는다.

---

# 3. 핵심 질문

```text
Simple Request에서 Model별 품질 차이가 있는가?

General Request에서는 차이가 있는가?

Reasoning Request에서는 차이가 커지는가?

Model별 Latency 차이는 어느 정도인가?

Token 사용량은 어떻게 다른가?

Cost 차이는 어느 정도인가?
```

이름이나 가격만 보고 성능을 가정하지 않는다.

---

# 4. Phase 2에서 하지 않을 것

```text
Routing Policy 변경

Semantic Routing

LLM-based Routing

Fallback / Retry

Capability 기반 후보 제외

Quality Judge를 Runtime Routing에 사용

Long Context / Capability / Cost-sensitive Case를 Dataset에 미리 확장

실패 Case를 Dataset에서 삭제

측정 없이 큰 Model이 더 좋다고 단정
```

---

# 5. Human Gate

다음을 바꾸려면 구현 전에 Human Gate를 연다.

```text
Routing Policy 변경

Quality 기준을 Runtime Routing에 사용

LLM Judge를 Runtime에 도입

Evaluation Dataset Expected를 결과 맞춰 수정
```

Phase 2 Quality 기준은 Evaluation 전용이다.

```text
Deterministic Checklist
PASS / FAIL
Runtime Router와 분리
```

이 기준은 현재 Dataset의 요구사항 충족 여부를 확인하기 위한 것이다.
Answer의 미묘한 품질 차이를 완전히 측정한다고 주장하지 않는다.

---

# 6. 작업 목록

---

## T2-01. Evaluation Dataset 확장

상태: `DONE`

목적:

Simple / General / Reasoning Case를 동일 실험에 사용할 수 있게 한다.

구현 범위:

```text
evaluation/dataset.json

각 유형 2개 이상 Case

expectedCondition
```

하지 않을 것:

```text
Long Context

Capability Required

Provider Failure Case

Expected Model 하나를 정답으로 고정
```

완료 조건:

```text
Simple / General / Reasoning Case가 있다.

각 Case에 확인 가능한 expectedCondition이 있다.
```

관련:

```text
REQUIREMENTS 5.1-5.3, 10.1-10.3
ROADMAP Phase 2 Evaluation Dataset
```

---

## T2-02. Quality Evaluator

상태: `DONE`

목적:

Answer Quality를 Routing 결과와 분리해서 PASS / FAIL로 확인한다.

구현 범위:

```text
요청 충족 여부 Checklist

mustInclude

형식 제약
```

하지 않을 것:

```text
Runtime Router에 Quality 결과 사용

LLM Judge 도입

Quality / Cost / Latency를 한 Score로 합치기
```

완료 조건:

```text
Case의 expectedCondition으로 PASS / FAIL을 판정할 수 있다.

판정 이유를 확인할 수 있다.

Unit Test가 있다.
```

관련:

```text
REQUIREMENTS FR-16
DESIGN 20
```

---

## T2-03. Direct Model Runner

상태: `DONE`

목적:

Router를 거치지 않고 동일 Request를 각 Model에 직접 실행한다.

구현 범위:

```text
Enabled Model 목록

Case × Model 실행

Quality

Model Latency

End-to-End Latency

Input / Output Token

Estimated Cost

결과 파일 저장
```

하지 않을 것:

```text
Router 경로와 결과 섞기

실패한 Model을 다른 Model로 대체
```

완료 조건:

```text
동일 Dataset을 두 Model 이상에 실행할 수 있다.

Router를 사용하지 않는다.

결과가 evaluation/results에 저장된다.

Test Double로 Runner Test가 있다.
```

관련:

```text
REQUIREMENTS FR-15
DESIGN 19.2
```

---

## T2-04. Model Baseline 측정 실행

상태: `BLOCKED`

목적:

실제 Model 호출로 Phase 2 핵심 질문에 답할 측정값을 얻는다.

완료 조건:

```text
각 Model을 동일 Dataset으로 실행했다.

실행 결과를 파일로 남겼다.

측정값과 추측을 구분한다.
```

차단 이유:

```text
Provider 호출은 되었으나 14 / 14가 credit_balance_exhausted 로 실패했다.

크레딧 부족을 Model Quality 결과로 기록하지 않는다.
```

가짜 측정값을 Experiment에 쓰지 않는다.

---

## T2-05. Experiment 기록

상태: `DONE`

목적:

측정 결과를 문제 중심으로 남긴다.

파일:

```text
docs/experiments/001-model-baseline.md
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
Quality / Latency / Token / Cost를 따로 기록했다.

실패 Case를 삭제하지 않았다.

Router를 바꾸지 않기로 한 결정을 명시했다.
```

관련:

```text
ROADMAP Phase 2 Experiment
```

---

## T2-06. DESIGN / README / TASKS 동기화

상태: `DONE`

목적:

현재 코드와 실행 방법을 문서에 맞춘다.

완료 조건:

```text
DESIGN에 Direct Model Runner와 Quality Evaluator가 현재 구조로 기록된다.

README에서 Direct Model Evaluation 실행 방법을 확인할 수 있다.

없는 미래 구조를 DESIGN에 넣지 않는다.
```

---

# 7. 권장 구현 순서

```text
T2-01 Dataset
      ↓
T2-02 Quality Evaluator
      ↓
T2-03 Direct Model Runner
      ↓
T2-04 실제 측정
      ↓
T2-05 Experiment
      ↓
T2-06 문서 동기화
```

---

# 8. Phase 2 완료 조건

ROADMAP Phase 2 완료 조건과 같다.

```text
각 Model을 동일 Dataset으로 실행했다.

Model별 Quality 결과를 확인할 수 있다.

Model별 Latency를 확인할 수 있다.

Model별 Token / Cost를 확인할 수 있다.

Model 특성 차이를 Experiment에 기록했다.
```

이 단계에서는 Router를 개선하지 않는다.

---

# 9. Git Checkpoint

```text
experiment: measure model baseline
```

---

# 10. 다음 Phase

Phase 2가 완료된 뒤에만 연다.

```text
Phase 3
Baseline Routing 평가
```
