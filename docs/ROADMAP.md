# LLM Router - Roadmap

## 1. ROADMAP 목적

이 문서는 LLM Router 프로젝트에서 **어떤 문제를 어떤 순서로 확인할 것인지** 정의한다.

ROADMAP은 기술 적용 목록이 아니다.

예를 들어:

```text
Phase 6
Semantic Routing 구현
```

처럼 해결 방법을 미리 정하지 않는다.

대신:

```text
Phase 6
기존 Routing 기준의 한계 확인
```

처럼 문제를 기준으로 Phase를 정의한다.

각 Phase에서는 가능한 한 다음 흐름을 유지한다.

```text
현재 상태
   ↓
문제 확인
   ↓
측정 / Evaluation
   ↓
결과 기록
   ↓
원인 분석
   ↓
필요하면 Human Gate
   ↓
다음 Phase
```

다음 원칙을 항상 유지한다.

```text
Phase 도달
≠
특정 기술 반드시 도입
```

실제 Failure가 확인되지 않으면 해당 기술을 사용하지 않을 수 있다.

---

# 2. 전체 Phase

```text
Phase 1
Baseline Router 구현
        ↓
Phase 2
Model별 Baseline 특성 측정
        ↓
Phase 3
Baseline Routing 평가
        ↓
Phase 4
Routing Failure 분석
        ↓
Phase 5
Quality / Cost / Latency Trade-off 확인
        ↓
Phase 6
Routing 기준의 한계 확인
        ↓
Phase 7
Capability / Provider Failure 확인
        ↓
Phase 8
Routing 구조 재평가
        ↓
Phase 9
최종 비교 및 회고
```

---

# Phase 1. Baseline Router 구현

## 목표

가장 단순한 LLM Router를 구현하고
이후 모든 Evaluation의 기준이 되는 Baseline을 만든다.

이 단계에서는 Router 품질을 높이는 것이 목표가 아니다.

**측정 가능한 Router가 존재하는 것**이 목표다.

---

## 기본 흐름

```text
POST /api/v1/chat
        ↓
Request Validation
        ↓
Baseline Router
        ↓
Model Selection
        ↓
LLM Client
        ↓
Selected Model
        ↓
Answer
```

---

## 구현 범위

### Request API

```text
자연어 Request 수신

빈 Request Validation
```

### Model Catalog

```text
최소 2개 이상의 Model 등록

Provider

Model ID

Enabled

Context Limit

Tool Calling 지원 여부

Structured Output 지원 여부

Input Cost

Output Cost
```

### Router

```text
Request
↓
Model 하나 선택
```

초기 Routing 기준은 최대한 단순하게 유지한다.

### LLM Client

```text
Selected Model
↓
Provider 호출
↓
Response
```

### Observability

최소한 다음 값을 확인할 수 있어야 한다.

```text
Selected Model

Provider

Routing Reason

Model Latency

End-to-End Latency

Input Token

Output Token

Estimated Cost
```

---

## Baseline에서 사용하지 않을 기능

```text
Semantic Routing

LLM-based Routing

Model Cascade

Routing Cache

Dynamic Threshold

Automatic Fallback

Retry Orchestration

LangGraph

Multi-Agent
```

---

## Test

```text
Request Validation Test

Model Catalog Test

Model Selection Test

LLM Client Test

Router Integration Test
```

---

## README

Phase 1 완료 시점부터 README에서 최소한 다음 내용을 확인할 수 있어야 한다.

```text
프로젝트 실행 방법

필요한 환경 변수

Provider / Model 설정 방법

Chat API 호출 방법

Test 실행 방법

Evaluation 실행 방법
```

Evaluation 구현이 아직 최소 수준이라면 실행 예정 구조라도 명확하게 표시한다.

---

## 완료 조건

```text
Request를 받을 수 있다.

최소 2개 이상의 Model을 관리할 수 있다.

Router가 Model 하나를 선택할 수 있다.

선택된 Model을 실제 호출할 수 있다.

응답을 반환할 수 있다.

Routing 결과와 기본 Metric을 확인할 수 있다.

README에서 실행 / 테스트 방법을 확인할 수 있다.
```

---

## Git Checkpoint

예:

```text
feat: implement baseline llm router
```

---

# Phase 2. Model별 Baseline 특성 측정

## 목표

Router를 평가하기 전에
각 Model이 실제로 어떤 특성을 가지는지 먼저 측정한다.

Router가 Model을 잘 선택했는지를 판단하려면
먼저 Model 간 차이를 알아야 한다.

---

## 핵심 질문

```text
Simple Request에서 Model별 품질 차이가 있는가?

General Request에서는 차이가 있는가?

Reasoning Request에서는 차이가 커지는가?

Model별 Latency 차이는 어느 정도인가?

Token 사용량은 어떻게 다른가?

Cost 차이는 어느 정도인가?
```

---

## Evaluation Dataset

최소한 다음 유형을 포함한다.

```text
Simple

General

Reasoning
```

가능하면 각 유형에 여러 Case를 둔다.

예:

```text
Simple
- 번역
- 짧은 요약
- 형식 변환

General
- 기술 개념 설명
- 일반 비교

Reasoning
- Architecture 분석
- 복잡한 Debugging
- Trade-off 비교
```

---

## 실험 방식

Router를 거치지 않고
동일 Request를 각 Model에 직접 실행한다.

```text
Request
   ├─ Model A
   ├─ Model B
   └─ Model C
```

다음 값을 비교한다.

```text
Quality

Model Latency

End-to-End Latency

Input Token

Output Token

Estimated Cost
```

---

## 중요한 원칙

Model 이름이나 가격만 보고 성능을 가정하지 않는다.

예:

```text
큰 Model
=
항상 체감 품질이 크게 좋음
```

이라고 미리 판단하지 않는다.

반드시 현재 Dataset에서 직접 확인한다.

---

## Experiment

예:

```text
docs/experiments/
└── 001-model-baseline.md
```

Experiment에는 최소한 다음을 기록한다.

```text
Dataset

Model

Prompt

Generation Parameter

Quality 기준

Latency

Token

Cost

실패 Case
```

---

## 완료 조건

```text
각 Model을 동일 Dataset으로 실행했다.

Model별 Quality 결과를 확인할 수 있다.

Model별 Latency를 확인할 수 있다.

Model별 Token / Cost를 확인할 수 있다.

Model 특성 차이를 Experiment에 기록했다.
```

이 단계에서는 Router를 개선하지 않는다.

---

## Git Checkpoint

예:

```text
experiment: measure model baseline
```

---

# Phase 3. Baseline Routing 평가

## 목표

Phase 1에서 만든 단순 Router를
실제 Evaluation Dataset으로 평가한다.

이 단계의 목적은 **문제를 찾는 것**이다.

발견된 문제를 바로 해결하지 않는다.

---

## 핵심 질문

```text
Router는 어떤 Model을 선택하는가?

Simple Request에서 불필요하게 큰 Model을 선택하는가?

Reasoning Request에서 충분한 Model을 선택하는가?

Capability가 필요한 Request를 처리할 수 있는가?

Routing 결과가 Quality / Cost / Latency 조건을 만족하는가?
```

---

## Evaluation 흐름

```text
Evaluation Dataset
        ↓
Baseline Router
        ↓
Selected Model
        ↓
LLM
        ↓
Result
        ↓
Expected Condition 비교
```

---

## 평가 기준

특정 Model 하나를 무조건 정답으로 두지 않는다.

가능하면 Case별로 다음 조건을 사용한다.

```text
Allowed Models

Required Capability

Minimum Quality

Maximum Latency

Maximum Cost
```

예:

```text
routing-001

Type
Simple

Allowed Models
Model Small
Model Medium

Minimum Quality
PASS

Maximum Cost
...

Maximum Latency
...
```

---

## Failure 후보

```text
ROUTING_FAILURE

MODEL_QUALITY_FAILURE

CAPABILITY_MISMATCH

LATENCY_FAILURE

COST_INEFFICIENCY

PROVIDER_FAILURE
```

이 단계에서는 정확한 원인 분류가 끝나지 않아도 된다.

먼저 실패 Case를 수집한다.

---

## Experiment

```text
docs/experiments/
└── 002-baseline-routing.md
```

---

## 완료 조건

```text
Baseline Router를 전체 Dataset으로 실행했다.

Case별 Selected Model을 확인할 수 있다.

Quality / Cost / Latency를 확인할 수 있다.

Expected Condition과 Actual Result를 비교할 수 있다.

실패 Case를 목록으로 남겼다.
```

**실패를 수정하지 않고도 Phase 3은 완료될 수 있다.**

---

## Git Checkpoint

```text
experiment: evaluate baseline routing
```

---

# Phase 4. Routing Failure 분석

## 목표

Phase 3에서 발견한 실패가
실제로 Router 문제인지 다른 계층의 문제인지 분리한다.

최종 Answer가 좋지 않다는 이유로 바로 Routing Policy를 변경하지 않는다.

---

## Failure 분리

```text
Answer Quality 낮음
        ↓
Router가 부적절한 Model을 선택했는가?
        ↓
YES
ROUTING_FAILURE
```

```text
의도한 Model 선택
        ↓
Answer Quality 낮음
        ↓
MODEL_QUALITY_FAILURE
또는
PROMPT_FAILURE
```

```text
필요 Capability 있음
        ↓
지원하지 않는 Model 선택
        ↓
CAPABILITY_MISMATCH
```

```text
품질 조건 만족
        ↓
하지만 불필요하게 고비용 Model 사용
        ↓
COST_INEFFICIENCY
```

---

## 분석 대상

Case별로 최소한 다음을 확인한다.

```text
Request Type

Selected Model

Model별 직접 실행 결과

Expected Condition

Routing Reason

Quality

Latency

Cost

Failure Type
```

---

## 이 Phase에서 하지 않을 것

Failure 원인을 확인하기 전에:

```text
Semantic Routing 추가

LLM Router 추가

Cascade 추가

Threshold 조정

Prompt 대규모 수정
```

을 하지 않는다.

---

## Experiment

```text
docs/experiments/
└── 003-routing-failure-analysis.md
```

---

## 완료 조건

```text
Baseline 실패 Case를 분류했다.

Routing Failure와 Model Quality Failure를 구분했다.

Cost / Latency Failure를 별도로 확인했다.

실패 유형별 실제 Case가 존재하는지 확인했다.

다음 Phase에서 검토할 문제를 정리했다.
```

---

## Git Checkpoint

```text
experiment: analyze routing failures
```

문제 재현 상태 자체를 해결 코드와 별도 Commit으로 남긴다.

---

# Phase 5. Quality / Cost / Latency Trade-off 확인

## 목표

Router가 단순히 가장 좋은 Model을 고르는 시스템이 아니라
**요구 품질을 만족하면서 비용과 Latency를 어떻게 균형 잡을지** 확인한다.

---

## 핵심 질문

```text
작은 Model로 충분한 Request는 무엇인가?

큰 Model을 사용했을 때 Quality가 실제로 얼마나 증가하는가?

Quality 증가에 비해 Cost 증가는 어느 정도인가?

Latency 증가는 정당화되는가?

같은 품질을 더 낮은 비용으로 처리할 수 있는 Case가 있는가?
```

---

## 비교

예:

```text
Request A

Model Small
Quality PASS
Latency  ...
Cost     ...

Model Large
Quality PASS
Latency  ...
Cost     ...
```

이 경우 단순히 Large Model이 더 좋은 답을 냈다고 해서
Large Model 선택을 정답으로 두지 않는다.

---

## Metric

별도로 기록한다.

```text
Quality

Latency

Cost
```

하나의 종합 Score로 합치지 않는다.

종합 Score가 필요하다고 판단되면 Evaluation 기준 변경이므로 Human Gate를 연다.

---

## Human Gate 후보

다음과 같은 결정이 필요할 수 있다.

```text
Request 유형별 Model 역할 정의

Quality 최소 기준

Cost 상한

Latency 상한

Routing Policy 변경
```

Human Gate 형식:

```text
현재 Failure Case
        ↓
Metric
        ↓
원인
        ↓
후보
        ↓
장단점
        ↓
추천안
        ↓
STOP
        ↓
Developer Decision
```

---

## Experiment

```text
docs/experiments/
└── 004-quality-cost-latency.md
```

---

## 완료 조건

```text
Model별 Quality / Cost / Latency Trade-off를 비교했다.

작은 Model로 충분한 Case를 확인했다.

큰 Model이 필요한 Case가 실제로 존재하는지 확인했다.

Routing 기준 변경이 필요한지 판단할 수 있다.
```

---

## Git Checkpoint

```text
experiment: analyze quality cost latency tradeoff
```

Human Gate 결정으로 Routing Policy가 변경될 경우
변경 Commit은 Experiment Commit과 분리한다.

예:

```text
feat: adjust routing policy
```

---

# Phase 6. Routing 기준의 한계 확인

## 목표

현재 Routing Policy가 어떤 Request에서 실패하는지 확인하고
더 복잡한 Routing 방식이 필요한지 판단한다.

이 Phase에서 특정 기술 도입을 목표로 하지 않는다.

---

## 핵심 질문

```text
현재 Rule만으로 충분한가?

어떤 Request에서 반복적으로 잘못 Routing되는가?

Request 분류 기준이 너무 단순한가?

고정 Rule로 표현하기 어려운 Case가 실제로 존재하는가?

복잡한 Routing을 추가했을 때 얻을 수 있는 이점이 충분한가?
```

---

## 후보

실제 Failure에 따라 다음과 같은 후보를 검토할 수 있다.

```text
Rule 기준 수정

Capability 기반 Filter 강화

Threshold 조정

Semantic Routing

LLM-based Routing
```

후보 목록에 있다는 이유만으로 구현하지 않는다.

---

## Human Gate

새 Routing 구조 도입은 Human Gate 대상이다.

예:

```text
Failure Case
복합 Request에서 Rule 기반 분류 반복 실패

후보 A
기존 Rule 세분화

후보 B
Semantic Routing

후보 C
LLM-based Routing
```

비교 항목:

```text
예상 Quality

Routing Latency

추가 Cost

구현 복잡도

관측 가능성

실패 가능성
```

---

## 적용 후 재평가

특정 후보를 선택했다면:

```text
Baseline Routing
        ↓
동일 Evaluation Dataset

vs

변경 Routing
        ↓
동일 Evaluation Dataset
```

으로 비교한다.

---

## Experiment

문제 중심 이름을 사용한다.

```text
docs/experiments/
└── 005-routing-policy-limit.md
```

특정 기술을 선택한 경우에도 Experiment 이름을:

```text
005-semantic-routing.md
```

처럼 바꾸지 않는다.

---

## 완료 조건

```text
현재 Routing Policy의 반복 Failure를 확인했다.

더 복잡한 Routing이 필요한지 근거를 확보했다.

필요한 경우 후보를 Human Gate에서 비교했다.

적용했다면 동일 Dataset으로 재평가했다.

도입하지 않았다면 그 이유를 기록했다.
```

---

# Phase 7. Capability / Provider Failure 확인

## 목표

정상 응답 품질뿐 아니라
Model Capability와 Provider Failure가 Routing에 어떤 영향을 주는지 확인한다.

---

## 7.1 Capability

확인할 수 있는 예:

```text
Context Limit

Tool Calling

Structured Output
```

예:

```text
Request
Tool Calling 필요

Model A
지원

Model B
미지원
```

Router가 미지원 Model을 선택하지 않는지 확인한다.

---

## 7.2 Long Context

긴 Request를 통해 다음을 확인한다.

```text
Context Limit 초과

Provider Error

Model별 Context 처리 차이
```

---

## 7.3 Provider Failure

의도적으로 Failure를 재현한다.

예:

```text
Timeout

Rate Limit

Model Unavailable

Provider Error
```

먼저 현재 동작을 확인한다.

```text
Provider Failure
      ↓
현재 Router
      ↓
어떤 결과가 발생하는가?
```

---

## Fallback

다음과 같이 먼저 구현하지 않는다.

```text
Provider는 실패할 수 있음
↓
Fallback 구현
```

실제 Failure 영향이 확인된 뒤 후보로 검토한다.

후보 예:

```text
Failure 그대로 반환

동일 Provider 다른 Model

다른 Provider Model

Retry

Fallback
```

---

## Human Gate

Fallback / Retry Policy 변경은 Human Gate 대상이다.

다음 항목을 비교한다.

```text
복구 가능성

추가 Latency

추가 Cost

중복 호출 위험

구현 복잡도

Provider 종속성
```

---

## Experiment

```text
docs/experiments/
├── 006-capability.md
└── 007-provider-failure.md
```

---

## 완료 조건

```text
Capability Mismatch Case를 확인했다.

Long Context Case를 확인했다.

Provider Failure를 재현했다.

현재 실패 동작을 기록했다.

Fallback / Retry 필요성을 근거로 판단했다.

적용했다면 Failure Case를 다시 실행했다.
```

---

## Git Checkpoint

예:

```text
experiment: verify model capability routing

experiment: reproduce provider failures
```

해결책을 적용한다면 별도 Commit으로 남긴다.

---

# Phase 8. Routing 구조 재평가

## 목표

지금까지의 Experiment 결과를 기준으로
현재 Router가 REQUIREMENTS를 어느 정도 만족하는지 다시 확인한다.

추가 기술을 넣는 Phase가 아니다.

**현재 구조가 충분한지 검증하는 Phase**다.

---

## 재평가 대상

```text
Routing

Capability

Quality

Cost

Latency

Failure Handling
```

---

## 동일 Dataset 비교

가능하면 Baseline과 동일한 Dataset을 사용한다.

```text
Baseline
vs
Current
```

비교 항목:

```text
Routing Success

Quality

Latency

Cost

Capability Failure

Provider Failure 처리
```

---

## 확인할 질문

```text
Routing Failure는 줄었는가?

Quality가 실제로 좋아졌는가?

Cost가 줄었는가?

Latency가 악화되지는 않았는가?

새 Complexity가 실제 결과로 정당화되는가?

아직 해결되지 않은 Requirement는 무엇인가?
```

---

## 중요한 원칙

다음과 같이 판단하지 않는다.

```text
새 기술 추가
=
Final 개선
```

예:

```text
Routing Failure 감소

하지만

Latency 크게 증가
Cost 증가
```

라면 Trade-off를 그대로 기록한다.

---

## Experiment

```text
docs/experiments/
└── 008-routing-re-evaluation.md
```

---

## 완료 조건

```text
Baseline과 현재 Router를 동일 조건에서 비교했다.

좋아진 Metric을 확인했다.

나빠진 Metric도 확인했다.

미충족 Requirement를 확인했다.

사용하지 않은 기술과 이유를 정리했다.

현재 남아 있는 한계를 정리했다.
```

---

## Git Checkpoint

```text
experiment: compare baseline and current routing
```

---

# Phase 9. 최종 비교 및 회고

## 목표

최종 구조를 정리하고
프로젝트의 학습 목표와 REQUIREMENTS를 실제 결과 기준으로 평가한다.

---

## 최종 구조

실제 구현된 구조만 DESIGN.md에 반영한다.

예를 들어 최종적으로 단순 Rule Router만 남았다면:

```text
Request
   ↓
Rule Router
   ↓
Selected Model
   ↓
LLM
   ↓
Response
```

만 기록한다.

실제로 사용하지 않은:

```text
Semantic Routing

Cascade

Fallback
```

을 미래 Architecture처럼 DESIGN에 넣지 않는다.

---

## REQUIREMENTS 평가

각 FR / NFR을 다음과 같이 확인한다.

```text
충족

부분 충족

미충족

해당 없음
```

Requirement를 충족하지 못했다고 Dataset이나 기준을 바꾸지 않는다.

---

## 최종 비교

최소한 다음 질문에 답할 수 있어야 한다.

```text
Baseline Router는 어떻게 동작했는가?

Model별 차이는 실제로 무엇이었는가?

어떤 Routing Failure가 있었는가?

Failure 원인은 무엇이었는가?

어떤 후보를 비교했는가?

무엇을 선택했는가?

왜 선택했는가?

Quality는 어떻게 달라졌는가?

Latency는 어떻게 달라졌는가?

Cost는 어떻게 달라졌는가?

어떤 기술을 사용하지 않았는가?

왜 사용하지 않았는가?

현재 남은 한계는 무엇인가?
```

---

## Harness 회고

이번 프로젝트에서는 기술뿐 아니라 개발 방식도 평가한다.

```text
Rule이 Phase 선제 구현을 막았는가?

Evaluation Dataset을 보호했는가?

Test와 Evaluation을 분리했는가?

Failure를 먼저 재현했는가?

Human Gate가 중요한 결정에서만 사용됐는가?

문제 재현과 해결 Commit을 분리했는가?

README를 Phase 1부터 유지했는가?

반복 Workflow가 실제 Skill 후보로 나타났는가?
```

---

## Skill 후보 확인

개발 과정에서 다음 Workflow가 반복되었는지 확인한다.

```text
Evaluation 실행
      ↓
Expected / Actual 비교
      ↓
Quality / Cost / Latency 집계
      ↓
Failure 분류
      ↓
Experiment Markdown 작성
```

실제로 반복되었다면
다음 프로젝트에서 재사용 가능한 Skill로 추출할지 판단한다.

```text
Rule
↓
반복 Workflow 발견
↓
Skill 추출
```

---

## 최종 문서

최소한 다음 문서를 현재 코드와 일치시킨다.

```text
README.md

docs/
├── REQUIREMENTS.md
├── DESIGN.md
├── ROADMAP.md
├── TASKS.md
├── adr/
└── experiments/
```

---

## 최종 Git Checkpoint

예:

```text
docs: complete llm router experiments
```

---

# 3. Phase별 Experiment 정리

예상 Experiment 흐름은 다음과 같다.

```text
docs/experiments/

001-model-baseline.md

002-baseline-routing.md

003-routing-failure-analysis.md

004-quality-cost-latency.md

005-routing-policy-limit.md

006-capability.md

007-provider-failure.md

008-routing-re-evaluation.md
```

Experiment 이름은 해결 기술이 아니라 **확인하는 문제**를 기준으로 작성한다.

실제 개발 결과에 따라 Experiment가 추가되거나 통합될 수 있다.

---

# 4. Phase별 Git 흐름

예상 흐름:

```text
feat: implement baseline llm router

experiment: measure model baseline

experiment: evaluate baseline routing

experiment: analyze routing failures

experiment: analyze quality cost latency tradeoff

experiment: analyze routing policy limits

experiment: verify model capability routing

experiment: reproduce provider failures

experiment: compare baseline and current routing

docs: complete llm router experiments
```

Human Gate를 통해 실제 해결 방법을 선택했다면
해결 Commit은 문제 재현 / 분석 Commit과 분리한다.

예:

```text
experiment: reproduce routing failures

feat: adjust routing policy

experiment: re-evaluate routing policy
```

---

# 5. 전체 개발 Loop

```text
REQUIREMENTS
      ↓
ROADMAP
      ↓
TASK
      ↓
PLAN
      ↓
IMPLEMENT
      ↓
TEST
      ↓
EVALUATE
      ↓
FAILURE CASE
      ↓
ANALYZE
      ↓
CANDIDATES
      ↓
HUMAN GATE
      ↓
DECIDE
      ↓
IMPLEMENT
      ↓
RE-EVALUATE
      ↓
ADR / DESIGN
      ↓
COMMIT
      ↓
NEXT TASK
```

모든 Phase에서 위 절차를 기계적으로 전부 실행할 필요는 없다.

하지만 다음 원칙은 유지한다.

```text
문제를 확인하기 전에 해결책부터 구현하지 않는다.

Phase에 도달했다는 이유로 특정 기술을 도입하지 않는다.

Test 통과를 AI 품질 성공으로 간주하지 않는다.

실패 Case를 Dataset에서 지우지 않는다.

측정 없이 Quality / Cost / Latency가 개선됐다고 주장하지 않는다.

중요한 Routing Policy 변경은 Human Gate 없이 진행하지 않는다.

문제 재현 상태는 해결 코드와 별도 Git Checkpoint로 남긴다.
```
