# 单人技术仓库：极简功能清单与实现思路（QLExpress + TDD）

## 1. 目标

在保持极简架构前提下，以 **TDD（测试驱动开发）** 方式实现可用平台：

1. 不引入过多中间件（仅 PostgreSQL）。
2. 执行与规则引擎自定义实现，表达式统一使用 QLExpress。
3. 所有核心能力遵循 **Red -> Green -> Refactor**。

---

## 2. 技术选型（极简）

### 2.1 运行时依赖

- Java 21
- Spring Boot 3.x（Web、Validation、Actuator、Scheduling）
- PostgreSQL
- QLExpress
- JDK HttpClient

### 2.2 测试依赖（TDD 必选）

- JUnit 5
- AssertJ
- Mockito
- Spring Boot Test（接口层）

> 不上 Redis、MQ、对象存储，避免早期运维复杂度。

---

## 3. TDD 开发约束（强制）

## 3.1 开发流程

1. **Red**：先写失败测试（最小场景）。  
2. **Green**：写最少实现让测试通过。  
3. **Refactor**：重构代码并保持测试全绿。  

## 3.2 合并门禁

- 新增业务逻辑必须同时新增测试。
- 未出现过 “失败测试 -> 通过测试” 记录的功能不算完成。
- 关键引擎改动必须补回归测试（防止表达式兼容性回归）。

## 3.3 测试分层

1. **单元测试（主力）**  
   覆盖 `ContextEngine / RequestRenderEngine / AssertEngine / ExtractEngine`。
2. **组件测试**  
   覆盖 `OrchestratorEngine` 的编排链路（使用 mock HTTP client）。
3. **API 测试**  
   覆盖 `run` 触发与报告查询接口（SpringBootTest）。

---

## 4. 功能清单（最小实现）

## 4.1 P0（必须）

1. 环境与变量管理（环境、变量覆盖）
2. 用例管理（`case_definition`，含 steps）
3. JSON/YAML 数据集管理
4. 套件管理（顺序执行）
5. 手动触发执行（Run）
6. 执行报告查询（通过率、失败步骤、失败表达式）
7. 基础健康检查与日志

## 4.2 P1（后续）

1. 定时执行（`@Scheduled` + cron）
2. CI 触发接口（token 鉴权）
3. 失败重试（仅网络异常）

## 4.3 P2（再后续）

1. OpenAPI 导入 case
2. 报告对比（与上一次 run）
3. Webhook 通知

---

## 5. 自定义引擎设计（QLExpress 核心）

1. **ContextEngine**  
   合并环境变量 + 数据集 + 运行时变量，生成统一上下文。

2. **RequestRenderEngine**  
   用 QLExpress 渲染 URL/Headers/Body 表达式。

3. **HttpExecuteEngine**  
   发起 HTTP 请求，返回标准响应对象（status/body/elapsed）。

4. **AssertEngine（QLExpress）**  
   执行断言表达式，输出 pass/fail 与失败表达式。

5. **ExtractEngine（QLExpress）**  
   从响应提取变量回写上下文。

6. **OrchestratorEngine**  
   编排 step 流程并落库 run/result。

### 5.1 QLExpress 约定

- 断言表达式：`status == 200 && body.code == 0`
- 提取表达式：`token = body.data.token`
- 条件跳过：`skipWhen = env != 'test'`

---

## 6. 最小 API 清单

### 6.1 环境与变量
- `POST /api/environments`
- `GET /api/environments`
- `POST /api/variables`
- `GET /api/variables?environmentId=`

### 6.2 用例与数据集
- `POST /api/cases`
- `PUT /api/cases/{id}`
- `POST /api/cases/{id}/datasets`
- `GET /api/cases/{id}/datasets`

### 6.3 套件与执行
- `POST /api/suites`
- `POST /api/suites/{id}/cases`
- `POST /api/suites/{id}/run`
- `GET /api/runs/{id}`
- `GET /api/runs/{id}/report`

### 6.4 计划（P1）
- `POST /api/plans`
- `PATCH /api/plans/{id}/enable`

---

## 7. Mermaid 架构图

完整图表见：`docs/architecture-mermaid.md`

---

## 8. 本周直接开工建议（TDD）

1. 先写 `AssertEngine` 的失败测试：表达式为 false 时返回 failedExpr。  
2. 再写最小实现使测试通过。  
3. 按同样方式推进 `ContextEngine -> RequestRenderEngine -> OrchestratorEngine`。  
4. 最后补 API 层测试，打通 run 主链路。  
