# 极简架构设计（Mermaid）- QLExpress 自定义引擎版

## 1. 设计前提

- 单体应用：Spring Boot
- 单数据库：PostgreSQL
- 无 Redis / MQ / 对象存储
- 规则与断言统一用 QLExpress
- 用例配置支持 YAML / JSON 双格式

---

## 2. 系统组件图

```mermaid
flowchart LR
    U[用户 / Swagger] --> API[API Controller]

    subgraph APP[Spring Boot Monolith]
      API --> ORCH[OrchestratorEngine]
      SCH[Scheduler] --> ORCH

      ORCH --> PARSER[ConfigParserEngine]
      ORCH --> CTX[ContextEngine]
      ORCH --> RENDER[RequestRenderEngine]
      ORCH --> HTTP[HttpExecuteEngine]
      ORCH --> ASSERT[AssertEngine]
      ORCH --> EXTRACT[ExtractEngine]
      ORCH --> REPORT[ReportAggregator]

      QL[QLExpressEngine] -.表达式求值.-> RENDER
      QL -.断言判定.-> ASSERT
      QL -.变量提取.-> EXTRACT
    end

    APP --> DB[(PostgreSQL)]
```

---

## 3. 执行时序图（手动触发）

```mermaid
sequenceDiagram
    participant C as Client
    participant A as Run API
    participant O as OrchestratorEngine
    participant P as ConfigParserEngine
    participant D as PostgreSQL
    participant Q as QLExpressEngine
    participant H as HttpExecuteEngine

    C->>A: POST /api/suites/{id}/run
    A->>D: create t_test_run(status=QUEUED)
    A->>O: start(runId)

    O->>D: load suite/cases/dataset/env
    O->>P: parse case_config(JSON|YAML)
    P-->>O: normalized definition
    O->>D: update t_test_run(status=RUNNING)

    loop each step
      O->>Q: render request expressions
      Q-->>O: rendered request
      O->>H: send http request
      H-->>O: response
      O->>Q: eval assert expressions
      Q-->>O: pass/fail + failedExpr
      O->>Q: eval extract expressions
      Q-->>O: extracted variables
      O->>D: insert t_test_case_result
    end

    O->>D: update t_test_run(status=SUCCESS/FAILED)
    A-->>C: runId
```

---

## 4. 用例结构图（Case Config）

```mermaid
classDiagram
    class CaseDefinition {
      +String caseCode
      +String caseName
      +String configFormat
      +List~Step~ steps
    }

    class Step {
      +String name
      +Request request
      +List~String~ assertions
      +Map~String,String~ extracts
      +String skipWhen
    }

    class Request {
      +String method
      +String urlExpr
      +Map~String,String~ headersExpr
      +String bodyExpr
      +Integer timeoutMs
    }

    CaseDefinition "1" *-- "*" Step
    Step "1" *-- "1" Request
```

其中 `configFormat` 允许值为 `JSON` 或 `YAML`。

---

## 5. 运行状态机

```mermaid
stateDiagram-v2
    [*] --> QUEUED
    QUEUED --> RUNNING
    RUNNING --> SUCCESS
    RUNNING --> FAILED
    RUNNING --> CANCELLED
    SUCCESS --> [*]
    FAILED --> [*]
    CANCELLED --> [*]
```

---

## 6. 引擎边界说明

1. **ConfigParserEngine** 负责 YAML/JSON 解析与统一化，不参与 HTTP 执行。  
2. **QLExpressEngine** 只做表达式计算，不负责 HTTP 发送。  
3. **HttpExecuteEngine** 只做请求发送，不做断言。  
4. **Assert/Extract** 依赖 QLExpressEngine，保持规则统一。  
5. **OrchestratorEngine** 负责流程编排和落库，是唯一流程入口。  

---

## 7. 执行策略说明（固定）

1. step 失败：fail-fast，终止当前 case。  
2. case 失败：suite 继续执行。  
3. 访问模式：无登录，仅内网 + IP 白名单。  
