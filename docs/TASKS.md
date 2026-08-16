# TASKS

현재 Phase에서 무엇을 할지 정의한다.

요구사항의 정본은 `docs/REQUIREMENTS.md`,
현재 구조의 정본은 `docs/DESIGN.md`,
문제 확인 순서는 `docs/ROADMAP.md`다.

이 문서는 그 세 문서를 다시 쓰지 않는다.

---

# 1. 현재 Phase

```text
Phase 6
Routing 기준의 한계 확인
```

상태:

```text
DONE
```

목표:

현재 Routing Policy가 어떤 Request에서 한계를 보이는지 확인하고
더 복잡한 Routing이 필요한지 판단한다.

이 Phase에서 특정 기술을 도입하지 않는다.

---

# 2. Phase 5 결과

```text
Checklist 기준 small로 충분한 Case: 6 / 7

large만 Checklist PASS: simple-003 (JSON 형식)

양쪽 성공 6 Case에서 large Estimated Cost ≈ small 13.4배

현재 Baseline(default = small)을 바꿀 측정 근거는 부족했다.
```

Phase 6은 이 결과와 Phase 3 / 4 실패 분류를 함께 본다.

```text
docs/experiments/002-baseline-routing.md
docs/experiments/003-routing-failure-analysis.md
docs/experiments/004-quality-cost-latency.md
```

새 Provider 호출을 이 Phase의 완료 조건으로 두지 않는다.

---

# 3. 핵심 질문

```text
현재 Rule만으로 충분한가?

어떤 Request에서 반복적으로 잘못 Routing되는가?

Request 분류 기준이 너무 단순한가?

고정 Rule로 표현하기 어려운 Case가 실제로 존재하는가?

복잡한 Routing을 추가했을 때 얻을 수 있는 이점이 충분한가?
```

---

# 4. Phase 6에서 하지 않을 것

```text
Semantic Routing 구현

LLM-based Routing 구현

Cascade 구현

Rule 세분화 구현

Threshold 조정

Capability / Long Context Case 선제 추가

Dataset Expected 수정

실패 Case 삭제

Quality Judge를 Runtime Routing에 사용
```

후보 목록에 있다는 이유만으로 구현하지 않는다.

---

# 5. Human Gate

다음을 바꾸려면 구현 전에 Human Gate를 연다.

```text
Routing Policy 변경

새 Routing 구조 도입

Request 유형별 Model 역할 정의

simple-003 형식 FAIL을 Routing 신호로 쓰기
```

한계를 기록하는 것과 Policy를 바꾸는 것은 다르다.
도입하지 않기로 한 결정은 Human Gate 없이 기록할 수 있다.

---

# 6. 작업 목록

---

## T6-01. 현재 Policy 한계 확인

상태: `DONE`

목적:

기존 측정으로 현재 Rule의 반복 Failure와
복잡한 Routing이 필요한지 판단한다.

확인 범위:

```text
현재 Rule이 Request를 어떻게 다루는가

반복 Routing Failure 여부

고정 Rule로 표현하기 어려운 Case 존재 여부

Semantic / LLM Routing 도입 근거
```

하지 않을 것:

```text
Router 변경

새 Dataset Case 추가
```

완료 조건:

```text
현재 Policy의 반복 Failure를 확인했다.

더 복잡한 Routing이 필요한지 근거를 기록했다.

도입하지 않았다면 이유를 기록했다.
```

관련:

```text
REQUIREMENTS FR-04, FR-20
ROADMAP Phase 6
DESIGN 3
```

---

## T6-02. Experiment 기록

상태: `DONE`

목적:

한계 확인을 문제 중심으로 남긴다.

파일:

```text
docs/experiments/005-routing-policy-limit.md
```

완료 조건:

```text
측정값과 해석을 구분했다.

기술 이름을 정답처럼 쓰지 않았다.

도입하지 않은 이유를 적었다.
```

---

## T6-03. DESIGN / README / TASKS 동기화

상태: `DONE`

목적:

현재 Policy가 그대로라는 점과
한계 확인 Experiment 위치를 문서에 맞춘다.

완료 조건:

```text
DESIGN에 없는 미래 Routing 구조를 넣지 않는다.

README에서 Experiment 005를 확인할 수 있다.
```

---

# 7. 권장 구현 순서

```text
T6-01 한계 확인
      ↓
T6-02 Experiment
      ↓
T6-03 문서 동기화
```

---

# 8. Phase 6 완료 조건

ROADMAP Phase 6 완료 조건과 같다.

```text
현재 Routing Policy의 반복 Failure를 확인했다.

더 복잡한 Routing이 필요한지 근거를 확보했다.

필요한 경우 후보를 Human Gate에서 비교했다.

적용했다면 동일 Dataset으로 재평가했다.

도입하지 않았다면 그 이유를 기록했다.
```

도입하지 않고도 Phase 6은 완료될 수 있다.

---

# 9. Git Checkpoint

```text
experiment: analyze routing policy limits
```

---

# 10. 다음 Phase

Phase 6가 완료된 뒤에만 연다.

```text
Phase 7
Capability / Provider Failure 확인
```
