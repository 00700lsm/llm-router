# TASKS

현재 Phase에서 무엇을 할지 정의한다.

요구사항의 정본은 `docs/REQUIREMENTS.md`,
현재 구조의 정본은 `docs/DESIGN.md`,
문제 확인 순서는 `docs/ROADMAP.md`다.

이 문서는 그 세 문서를 다시 쓰지 않는다.

---

# 1. 현재 Phase

```text
Phase 9
최종 비교 및 회고
```

상태:

```text
DONE
```

목표:

최종 구조를 정리하고
학습 목표와 REQUIREMENTS를 실제 결과로 평가한다.

추가 기술을 넣지 않는다.
Dataset Expected를 바꾸지 않는다.

---

# 2. Phase 8 결과

```text
현재 Router = Phase 1 Baseline
strategy = BASELINE_DEFAULT

Routing Quality / Cost / Latency 개선을 주장하지 않는다.

Semantic / LLM / Cascade / Fallback / Retry / Capability Filter
는 현재 문제에서 사용하지 않았다.
```

---

# 3. 핵심 질문

```text
Baseline Router는 어떻게 동작했는가?

Model별 차이는 실제로 무엇이었는가?

어떤 Routing Failure가 있었는가?

무엇을 선택했고 왜인가?

어떤 기술을 사용하지 않았는가?

현재 남은 한계는 무엇인가?

Rule과 Evaluation Harness는 역할을 했는가?
```

---

# 4. Phase 9에서 하지 않을 것

```text
Routing Policy 변경

새 Routing 구조 도입

Dataset Expected 수정

실패 Case 삭제

사용하지 않은 기술을 DESIGN에 현재 구조로 넣기

반복이 약한 Workflow를 Skill로 추출
```

---

# 5. Human Gate

최종 회고에서 Policy를 바꾸려면 구현 전에 Human Gate를 연다.

이번 Phase는 회고만 한다.

---

# 6. 작업 목록

---

## T9-01. REQUIREMENTS 평가와 최종 비교

상태: `DONE`

파일:

```text
docs/experiments/009-final-comparison.md
```

완료 조건:

```text
FR / NFR을 충족 / 부분 / 미충족 / 해당 없음으로 기록했다.

최종 비교 질문에 답했다.

Dataset이나 기준을 결과에 맞춰 바꾸지 않았다.
```

---

## T9-02. ADR / Harness 회고

상태: `DONE`

파일:

```text
docs/adr/001-use-gemini-provider.md
docs/adr/002-use-gemini-3.5-flash-as-large.md
docs/adr/003-keep-baseline-default.md
```

완료 조건:

```text
실제 Decision만 ADR에 남겼다.

Harness 회고와 Skill 후보 판단을 기록했다.

Skill을 미리 추출하지 않았다.
```

---

## T9-03. DESIGN / README / TASKS 동기화

상태: `DONE`

완료 조건:

```text
DESIGN은 구현된 Baseline 구조만 기록한다.

README에서 최종 Experiment와 ADR 위치를 확인할 수 있다.

다음 Phase는 없다.
```

---

# 7. 권장 구현 순서

```text
T9-01 최종 비교
      ↓
T9-02 ADR / Harness
      ↓
T9-03 문서 동기화
```

---

# 8. Phase 9 완료 조건

```text
최종 구조가 DESIGN과 코드와 같다.

REQUIREMENTS를 결과 기준으로 평가했다.

사용하지 않은 기술과 이유를 남겼다.

남은 한계를 남겼다.

README / DESIGN / TASKS / ADR / Experiment가 현재 상태와 맞다.
```

---

# 9. Git Checkpoint

```text
docs: complete llm router experiments
```

---

# 10. 다음 Phase

없다.

Phase 9가 마지막이다.
