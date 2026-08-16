# TASKS

현재 Phase에서 무엇을 할지 정의한다.

요구사항의 정본은 `docs/REQUIREMENTS.md`,
현재 구조의 정본은 `docs/DESIGN.md`,
문제 확인 순서는 `docs/ROADMAP.md`다.

이 문서는 그 세 문서를 다시 쓰지 않는다.

---

# 1. 현재 Phase

```text
Phase 8
Routing 구조 재평가
```

상태:

```text
DONE
```

목표:

지금까지의 Experiment로
현재 Router가 REQUIREMENTS를 어느 정도 만족하는지 확인한다.

추가 기술을 넣지 않는다.

---

# 2. Phase 7 결과

```text
CAPABILITY_MISMATCH는 Production Catalog / Dataset에서 없다.

Router는 Capability / Context Limit을 쓰지 않는다.

Provider Failure는 ErrorCode로 반환한다. Retry / Fallback 없음.

Retry / Fallback / Capability Filter는 사용하지 않았다.
```

Phase 8은 이 결과와 Phase 1~6을 함께 본다.

---

# 3. 핵심 질문

```text
Routing Failure는 줄었는가?

Quality가 실제로 좋아졌는가?

Cost가 줄었는가?

Latency가 악화되지는 않았는가?

새 Complexity가 실제 결과로 정당화되는가?

아직 해결되지 않은 Requirement는 무엇인가?
```

---

# 4. Phase 8에서 하지 않을 것

```text
Routing Policy 변경

Semantic Routing

LLM-based Routing

Cascade

Fallback / Retry

Capability Filter

Dataset Expected 수정

새 live Provider 호출을 완료 조건으로 두기
```

---

# 5. Human Gate

다음을 바꾸려면 구현 전에 Human Gate를 연다.

```text
Routing Policy 변경

새 Routing 구조 도입

Fallback / Retry

Capability 기반 Routing
```

재평가 결과가 바로 Policy 변경은 아니다.

---

# 6. 작업 목록

---

## T8-01. Baseline vs Current 비교

상태: `DONE`

목적:

동일 Dataset에서 Routing Policy가 바뀌었는지,
Metric이 달라졌는지 확인한다.

비교 축:

```text
Routing

Quality

Cost

Latency

Capability

Failure Handling
```

하지 않을 것:

```text
Router 변경

종합 Score
```

완료 조건:

```text
Baseline과 현재 Router를 동일 조건에서 비교했다.

좋아진 Metric / 나빠진 Metric을 구분했다.

미충족 Requirement를 기록했다.
```

관련:

```text
REQUIREMENTS 15
ROADMAP Phase 8
```

---

## T8-02. Experiment 기록

상태: `DONE`

파일:

```text
docs/experiments/008-routing-re-evaluation.md
```

완료 조건:

```text
사용하지 않은 기술과 이유를 정리했다.

남아 있는 한계를 정리했다.

측정하지 않은 개선을 주장하지 않았다.
```

---

## T8-03. DESIGN / README / TASKS 동기화

상태: `DONE`

완료 조건:

```text
DESIGN이 현재 Baseline 구조만 기록한다.

README에서 Experiment 008을 확인할 수 있다.
```

---

# 7. 권장 구현 순서

```text
T8-01 비교
      ↓
T8-02 Experiment
      ↓
T8-03 문서 동기화
```

---

# 8. Phase 8 완료 조건

ROADMAP Phase 8 완료 조건과 같다.

```text
Baseline과 현재 Router를 동일 조건에서 비교했다.

좋아진 Metric을 확인했다.

나빠진 Metric도 확인했다.

미충족 Requirement를 확인했다.

사용하지 않은 기술과 이유를 정리했다.

현재 남아 있는 한계를 정리했다.
```

---

# 9. Git Checkpoint

```text
experiment: compare baseline and current routing
```

---

# 10. 다음 Phase

Phase 8가 완료된 뒤에만 연다.

```text
Phase 9
최종 비교 및 회고
```
