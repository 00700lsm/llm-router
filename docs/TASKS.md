# TASKS

현재 Phase에서 무엇을 할지 정의한다.

요구사항의 정본은 `docs/REQUIREMENTS.md`,
현재 구조의 정본은 `docs/DESIGN.md`,
문제 확인 순서는 `docs/ROADMAP.md`다.

이 문서는 그 세 문서를 다시 쓰지 않는다.

---

# 1. 현재 Phase

```text
Phase 5
Quality / Cost / Latency Trade-off 확인
```

상태:

```text
DONE
```

목표:

요구 품질을 만족하면서 Cost와 Latency가
Model 선택에 따라 어떻게 달라지는지 확인한다.

하나의 종합 Score로 합치지 않는다.
이 Phase에서 Routing Policy를 바꾸지 않는다.

---

# 2. Phase 4 결과

```text
현재 정책 위반 ROUTING_FAILURE는 없다.

simple-003 = MODEL_QUALITY_FAILURE
            + ROUTING_FAILURE 후보 (large면 Checklist PASS)

reasoning-001 / reasoning-002 = PROVIDER_FAILURE (HTTP 429)

CAPABILITY / COST_INEFFICIENCY / LATENCY_FAILURE
= 현재 Dataset에 실제 Case 없음
```

Phase 5 비교에 쓰는 측정은 Phase 2 Direct Model 결과다.

```text
docs/experiments/001-model-baseline.md
evaluation/results/001-model-baseline.json
Gemini 2차: model-small = gemini-2.5-flash
            model-large = gemini-3.5-flash
```

새 Provider 호출을 이 Phase의 완료 조건으로 두지 않는다.

Router 경로 결과와 Direct Model 결과를 같은 표에 섞지 않는다.

---

# 3. 핵심 질문

```text
작은 Model로 충분한 Request는 무엇인가?

큰 Model을 사용했을 때 Quality가 실제로 얼마나 증가하는가?

Quality 증가에 비해 Cost 증가는 어느 정도인가?

Latency 증가는 정당화되는가?

같은 품질을 더 낮은 비용으로 처리할 수 있는 Case가 있는가?
```

---

# 4. Phase 5에서 하지 않을 것

```text
Routing Policy 변경

Semantic Routing

LLM-based Routing

Cascade

Threshold 조정

Quality / Cost / Latency를 한 Score로 합치기

Max Cost / Max Latency를 Dataset Expected에 넣기

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

Request 유형별 Model 역할 정의

Quality 최소 기준 변경

Cost 상한 / Latency 상한 Dataset 추가

종합 Score 도입

simple-003 형식 FAIL을 Routing 신호로 쓰기
```

비교 결과가 바로 Policy 변경은 아니다.

---

# 6. 작업 목록

---

## T5-01. Case별 Quality / Cost / Latency 비교

상태: `DONE`

목적:

동일 Dataset에서 두 Model의 Quality, Cost, Latency를
한 축으로 합치지 않고 비교한다.

확인 범위:

```text
Case별 Quality

Case별 Latency

Case별 Estimated Cost

small로 Checklist를 만족하는 Case

large만 Checklist를 만족하는 Case
```

하지 않을 것:

```text
Router 변경

새 Evaluation Runner

종합 Score
```

완료 조건:

```text
Model별 Quality / Cost / Latency를 따로 기록했다.

작은 Model로 충분한 Case를 확인했다.

큰 Model이 필요한 Case가 실제로 있는지 기록했다.
```

관련:

```text
REQUIREMENTS FR-15, NFR-03, NFR-09
ROADMAP Phase 5
DESIGN 19.2
```

---

## T5-02. Experiment 기록

상태: `DONE`

목적:

Trade-off를 문제 중심으로 남긴다.

파일:

```text
docs/experiments/004-quality-cost-latency.md
```

완료 조건:

```text
Quality / Cost / Latency를 한 Score로 합치지 않았다.

측정값과 해석을 구분했다.

Routing 기준 변경이 필요한지 판단할 수 있다.

Router를 바꾸지 않기로 한 결정을 명시했다.
```

---

## T5-03. DESIGN / README / TASKS 동기화

상태: `DONE`

목적:

Trade-off 비교 위치를 문서에 맞춘다.

완료 조건:

```text
DESIGN이 현재 구조만 기록한다.

README에서 Experiment 004를 확인할 수 있다.

없는 미래 구조를 DESIGN에 넣지 않는다.
```

---

# 7. 권장 구현 순서

```text
T5-01 Case별 비교
      ↓
T5-02 Experiment
      ↓
T5-03 문서 동기화
```

---

# 8. Phase 5 완료 조건

ROADMAP Phase 5 완료 조건과 같다.

```text
Model별 Quality / Cost / Latency Trade-off를 비교했다.

작은 Model로 충분한 Case를 확인했다.

큰 Model이 필요한 Case가 실제로 존재하는지 확인했다.

Routing 기준 변경이 필요한지 판단할 수 있다.
```

비교만으로 Phase 5는 완료될 수 있다.
Routing Policy를 바꾸지 않고도 완료될 수 있다.

---

# 9. Git Checkpoint

```text
experiment: analyze quality cost latency tradeoff
```

---

# 10. 다음 Phase

Phase 5가 완료된 뒤에만 연다.

```text
Phase 6
Routing 기준의 한계 확인
```
