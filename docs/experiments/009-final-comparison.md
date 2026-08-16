# 009. 최종 비교 및 회고

## Problem / Question

학습 목표와 REQUIREMENTS를 실제 결과로 보면
이 프로젝트는 무엇을 확인했는가?

```text
Baseline Router는 어떻게 동작했는가?

Model별 차이는 실제로 무엇이었는가?

어떤 Routing Failure가 있었는가?

Failure 원인은 무엇이었는가?

어떤 후보를 비교했는가?

무엇을 선택했는가?

왜 선택했는가?

Quality / Latency / Cost는 어떻게 달라졌는가?

어떤 기술을 사용하지 않았는가?

현재 남은 한계는 무엇인가?
```

Requirement를 맞추기 위해 Dataset이나 기준을 바꾸지 않는다.

---

## Hypothesis

확인이 필요한 가설은 다음이다.

```text
측정 가능한 Baseline과 Failure 분류가 있으면
복잡한 Routing을 넣지 않고도
REQUIREMENTS의 학습 목표를 평가할 수 있다.
```

---

## Conditions

새 Provider 호출을 하지 않는다.
Router를 바꾸지 않는다.
evaluation/dataset.json을 바꾸지 않는다.

근거:

```text
docs/experiments/001-model-baseline.md ~ 008-routing-re-evaluation.md
docs/adr/
evaluation/dataset.json
```

---

## Baseline

최종 구조는 Phase 1과 같다.

```text
Request
      ↓
Baseline Router
      ↓
Configured Default Model
      ↓
LLM
      ↓
Response
```

```text
strategy = BASELINE_DEFAULT
default-model = model-small (gemini-2.5-flash)
model-large = gemini-3.5-flash
Provider = GEMINI generateContent
```

---

## Result

### 최종 비교 질문

Baseline Router는 어떻게 동작했는가?

```text
모든 일반 Request → configured default model
Request 내용을 분류하지 않는다.
```

Model별 차이는 실제로 무엇이었는가?

```text
Checklist PASS: small 6 / 7, large 6 / 7 (Gemini 2차 Direct)
차이는 simple-003 형식 1건이다.
양쪽 성공 6 Case Cost 합: large ≈ small 13.4배
Latency 방향은 Case마다 다르다.
Reasoning에서 품질 차이가 커졌다는 측정값은 없다.
```

어떤 Routing Failure가 있었는가?

```text
정책 위반 ROUTING_FAILURE: 없음
ROUTING_FAILURE 후보: simple-003
MODEL_QUALITY_FAILURE: simple-003
PROVIDER_FAILURE: reasoning-001 / 002 Router 경로 429
```

Failure 원인은 무엇이었는가?

```text
simple-003: Default Model이 JSON 형식 제약을 못 지킴.
            같은 Prompt에서 large는 PASS.
reasoning 429: Provider quota. Routing 오분류가 아니다.
```

어떤 후보를 비교했는가?

```text
항상 large
유형별 large
JSON 형식만 다른 Model
Semantic / LLM Routing
Fallback / Retry
Capability Filter
현재 Baseline 유지
```

무엇을 선택했는가?

```text
현재 Baseline 유지
```

왜 선택했는가?

```text
정책 위반 ROUTING_FAILURE가 없다.
small로 Checklist 충분한 Case가 6 / 7이다.
복잡한 Routing의 이점을 잴 Failure가 없다.
```

Quality / Latency / Cost는 어떻게 달라졌는가?

```text
Routing Policy를 바꾸지 않았다.
Router 경로 Metric Before / After 개선을 주장하지 않는다.
```

어떤 기술을 사용하지 않았는가?

```text
Semantic Routing, LLM-based Routing, Cascade,
Routing Cache, Dynamic Threshold, Fallback, Retry,
LangGraph, Multi-Agent, Capability Filter
```

왜 사용하지 않았는가?

```text
현재 Dataset에서 그 기술을 정당화할 Failure가 없었다.
효과가 없었다고 쓰지 않는다. 비교 실험이 없다.
```

현재 남은 한계는 무엇인가?

```text
Checklist는 키워드/형식만 본다.
Router 경로 Reasoning Quality가 429로 비어 있다.
Capability / Long Context Case가 Baseline Dataset에 없다.
Max Cost / Max Latency Expected가 없다.
Provider는 Gemini 하나다.
```

### REQUIREMENTS 평가

Dataset이나 FR 문구를 결과에 맞춰 바꾸지 않았다.

| ID | 결과 | 근거 |
|---|---|---|
| FR-01 요청 수신 | 충족 | POST /api/v1/chat, 빈 요청 거절 |
| FR-02 Model 등록 | 충족 | application.yml Catalog |
| FR-03 활성화 | 충족 | enabled, MODEL_DISABLED |
| FR-04 Baseline Routing | 충족 | BASELINE_DEFAULT |
| FR-05 Model 호출 | 충족 | Gemini generateContent |
| FR-06 공통 응답 | 충족 | answer / model / provider |
| FR-07 Routing 결과 확인 | 충족 | Log, Evaluation Result |
| FR-08 Capability 확인 | 충족 | Catalog 필드 |
| FR-09 후보 제외 | 미충족 | Filter 없음. Production Case도 없음 |
| FR-10 Token | 충족 | Provider Usage |
| FR-11 Cost | 충족 | Catalog 단가 예상값 |
| FR-12 Latency | 충족 | Model / End-to-End |
| FR-13 Evaluation 실행 | 충족 | Router / Direct Runner |
| FR-14 Routing 평가 | 부분 충족 | Quality Expected는 있음. Allowed / Max Cost / Max Latency는 Dataset에 없음 |
| FR-15 직접 비교 | 충족 | Direct Model Runner |
| FR-16 Quality | 부분 충족 | Checklist. 사실 검증 / 미묘한 품질은 없음 |
| FR-17 Routing Failure 확인 | 충족 | Experiment 003 |
| FR-18 Provider Failure 확인 | 충족 | ErrorCode, Experiment 007 |
| FR-19 Fallback | 해당 없음 | 도입 근거 부족으로 사용하지 않음 |
| FR-20 전략 변경 | 해당 없음 | 반복 정책 위반 Failure 없음 |
| FR-21 Cascade | 해당 없음 | 항상 large 비효율 문제를 이 Policy에서 재현하지 않음 |
| FR-22 재평가 | 충족 | 동일 Dataset으로 비교 가능. 전략을 바꾸지 않아 Before / After는 동일 Policy |
| NFR-01 측정 가능성 | 충족 | Dataset + Experiment |
| NFR-02 Routing/Generation 분리 | 충족 | Failure Type 분리 |
| NFR-03 축 분리 | 충족 | Quality / Cost / Latency 한 Score 없음 |
| NFR-04 재현 조건 | 부분 충족 | Dataset / Model / Quality 기준은 기록. Generation Parameter / 실행 시각은 약함 |
| NFR-05 변경의 측정 | 충족 | 기술 도입을 개선으로 쓰지 않음 |
| NFR-06 Latency 관측 | 충족 | Routing Latency는 Default lookup이라 분리하지 않음 |
| NFR-07 비용 관측 | 충족 | Case / Model Estimated Cost |
| NFR-08 Capability 안전성 | 미충족 | Runtime이 Capability를 보지 않음. Production Mismatch는 없음 |
| NFR-09 고비용 관측 | 충족 | 불필요 large 선택은 관측되지 않음 |
| NFR-10 실패 안전성 | 충족 | 실패를 성공처럼 만들지 않음 |
| NFR-11 동일 조건 | 충족 | Dataset Expected를 결과에 맞추지 않음 |
| NFR-12 관측 가능성 | 충족 | Decision / Token / Cost / Latency |
| NFR-13 Dataset 보호 | 충족 | 실패 Case 유지, Expected 완화 없음 |
| NFR-14 Provider 종속성 | 부분 충족 | Gateway 분리. Provider는 Gemini 하나 |

---

## Analysis

학습 목표의 핵심은 Model을 많이 나누는 것이 아니다.

확인한 것:

```text
측정 가능한 Baseline이 있다.

이름만 보고 large가 낫다고 단정할 Checklist 차이는
현재 Dataset에서 형식 1건이다.

실패를 Router 문제로 바로 묶지 않으면
Provider Failure와 Quality Failure가 섞이지 않는다.

복잡한 Routing을 넣지 않은 것도 결과다.
```

부분 충족 / 미충족을 숨기지 않는다.

```text
FR-09, NFR-08은 메커니즘이 없다.
FR-16은 Checklist 한계가 있다.
FR-14의 Cost / Latency Expected는 Dataset에 없다.
```

이것을 성공으로 포장하지 않는다.
동시에 Dataset을 바꿔 충족한 척하지 않는다.

---

## Harness 회고

Rule이 Phase 선제 구현을 막았는가?

```text
막았다.
Semantic / Cascade / Fallback을 ROADMAP에 있다고 넣지 않았다.
```

Evaluation Dataset을 보호했는가?

```text
보호했다.
simple-003 Expected를 완화하지 않았다.
실패 Case를 삭제하지 않았다.
```

Test와 Evaluation을 분리했는가?

```text
분리했다.
Test는 코드 동작이다.
Evaluation은 Checklist / Cost / Latency다.
```

Failure를 먼저 재현했는가?

```text
재현했다.
Provider Failure는 live와 Test Double로 본 뒤
Fallback을 넣지 않기로 했다.
```

Human Gate가 중요한 결정에서만 사용됐는가?

```text
Provider / large Model 교체는 Gate였다.
Policy를 안 바꾸는 결정에는 Gate를 열지 않았다.
```

문제 재현과 해결 Commit을 분리했는가?

```text
Routing 해결 Commit은 없다. Policy를 안 바꿨다.
Capability 확인과 Provider Failure 재현은 Commit을 나눴다.
Catalog 변경(Human Gate A)은 재측정 Commit으로 남겼다.
```

README를 Phase 1부터 유지했는가?

```text
유지했다. 실행 방법과 Experiment 위치를 Phase마다 갱신했다.
```

반복 Workflow가 Skill 후보로 나타났는가?

```text
Experiment Markdown 형식은 8번 반복됐다.
live Evaluation 실행은 Direct Model과 Router 경로 두 번이다.

후보: Evaluation 실행 → 집계 → Failure 분류 → Experiment 기록

이번 프로젝트에서 Skill 파일로 추출하지 않는다.
형식은 이미 Rule 18에 있다.
live Runner가 두 종류라 하나의 Skill로 굳히기 이르다.
```

---

## Candidate

최종 구조 변경 후보는 없다.

Skill 추출 후보는 다음 프로젝트에서 다시 본다.

---

## Decision

```text
최종 구조는 Baseline Router다.

Router를 변경하지 않는다.

사용하지 않은 기술을 DESIGN에 넣지 않는다.

REQUIREMENTS 미충족 / 해당 없음을 그대로 둔다.

Skill을 추출하지 않는다.
```

---

## Remaining Limitation

008 Remaining Limitation과 같다.

추가로:

```text
Generation Parameter를 Evaluation 조건에 거의 남기지 않았다.

OpenAI와 Gemini 결과를 같은 Model 품질 Before / After로 비교하지 않는다.
```
