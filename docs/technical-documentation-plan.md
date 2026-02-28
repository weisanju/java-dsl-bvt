# 单人技术仓库：功能清单与实现思路（Java + Karate）

## 1. 适用范围与目标

本规划专门面向**单人维护**的技术仓库，核心目标不是“组织协作流程”，而是：

1. 用最小成本做出可运行的接口测试平台 MVP。
2. 功能上优先覆盖“可用”，而不是一次做全“企业级”。
3. 保持后续可扩展：当你需要多人协作时，不推倒重来。

---

## 2. 单人版总体策略

### 2.1 设计原则

- **先跑通主链路，再补治理能力**（先执行，再权限/审计）。
- **单体优先**（Spring Boot 单体 + 模块化包结构）。
- **本地可运行优先**（docker compose 一键启动依赖）。
- **配置简单**（少依赖、少服务、少运维动作）。

### 2.2 技术栈（单人推荐）

- Java 21 + Spring Boot 3.x
- PostgreSQL（核心数据）
- Redis（可选，先用于任务状态缓存）
- RabbitMQ（可选；初期可用数据库轮询队列代替）
- Karate（执行引擎）
- Vue3（可选；单人阶段可先只做后端 API + Swagger）

> 建议：第 1 阶段仅做后端 + Swagger UI，第 2 阶段再补前端页面。

---

## 3. 功能清单（按优先级）

## 3.1 P0（必须做，保证 MVP 可用）

| 模块 | 功能 | 最小可交付（MVP） |
|---|---|---|
| 项目与环境 | 项目、环境、变量管理 | 支持一个项目下多环境（dev/test），变量可覆盖 |
| 用例管理 | Karate 脚本存储 | 支持创建/编辑/版本号递增 |
| 数据驱动 | JSON/YAML 数据集 | 用例可绑定数据集并执行 |
| 套件编排 | 用例集合 + 顺序 | 支持按顺序执行多个用例 |
| 执行中心 | 手动触发执行 | 创建 run、记录状态、返回结果 |
| 结果报告 | 通过/失败 + 失败详情 | 展示失败断言、响应摘要、耗时 |
| 基础运维 | 健康检查 + 日志 | `/actuator/health`、可检索执行日志 |

## 3.2 P1（建议做，提升实用性）

| 模块 | 功能 | 价值 |
|---|---|---|
| 调度执行 | Cron 定时任务 | 自动回归，减少手工触发 |
| CI 集成 | Webhook/API 触发 | 接入 GitHub Actions/Jenkins |
| 重试策略 | 失败重试（网络类） | 提升稳定性，减少误报 |
| 通知 | 企业微信/飞书 webhook | 执行结果自动通知 |

## 3.3 P2（可后置）

| 模块 | 功能 |
|---|---|
| OpenAPI 导入增强 | 自动生成 case 模板 |
| 队列优先级 | 按项目或标签优先执行 |
| 报告对比 | 与上次结果 diff |
| 多 runner | 分布式执行扩容 |

---

## 4. 实现思路（按模块）

## 4.1 用例与数据管理

### 目标
以数据库为中心管理测试资产，不依赖本地文件结构。

### 实现方式
- `t_test_case` 存 Karate 脚本（`script_content`）。
- `t_test_case_data_set` 存 JSON/YAML 文本（`data_format + data_content`）。
- 执行前将数据集注入 Karate 上下文变量。

### 关键点
- 存储时做语法校验（JSON/YAML 格式有效）。
- 用例保存时保留版本号（`version_no`）。

## 4.2 执行引擎集成（Karate）

### 目标
后端服务内触发 Karate 执行并获取结构化结果。

### 实现方式
- 运行时生成临时 feature 文件（或内存模板）。
- 使用 Karate JUnit / Runner API 执行。
- 解析 Karate 报告 JSON，回填 `t_test_case_result`。

### 关键点
- 每次执行生成唯一 `run_id`，用于全链路关联。
- 保留原始报告到对象存储或本地目录（后续迁移 MinIO）。

## 4.3 执行编排与状态机

### 目标
可追踪每次执行过程，支持排队、运行、完成、失败。

### 实现方式
- `t_test_run`：记录一次执行总览。
- `t_test_run_task`：记录每个用例执行任务。
- 状态机：`QUEUED -> RUNNING -> SUCCESS/FAILED/CANCELLED`。

### 关键点
- 状态变更必须幂等（防止重复回调覆盖）。
- 失败信息写入 `error_summary` 与 `log_excerpt`。

## 4.4 报告与可观测

### 目标
快速定位失败原因，能看趋势。

### 实现方式
- 即时报告：当前 run 的通过率、失败详情、耗时。
- 聚合报告：近 7 天/30 天成功率趋势（SQL 聚合即可）。
- 接口暴露 `/api/runs/{id}/report`。

### 关键点
- 日志脱敏（token/password）。
- 请求/响应快照可截断存储，避免无限膨胀。

## 4.5 调度与自动化（P1）

### 目标
支持定时回归和 CI 触发，做到无人值守。

### 实现方式
- 定时：Spring Scheduling + `t_test_plan` 的 cron 表达式。
- CI：提供 `POST /api/plans/{id}/trigger`。
- 通知：执行完成后按规则发送 webhook。

### 关键点
- 只重试“可重试错误”（连接超时、5xx），断言失败不重试。

---

## 5. 单人版迭代顺序（6 周）

| 周次 | 目标 | 输出 |
|---|---|---|
| Week 1 | 项目骨架 + 数据库初始化 | Spring Boot 工程、DDL 执行通过 |
| Week 2 | 用例/数据集管理 API | Case CRUD、DataSet CRUD |
| Week 3 | 套件 + 手动执行 | Run 创建、执行状态可跟踪 |
| Week 4 | 报告接口 + 基础看板 | 失败详情、成功率统计 |
| Week 5 | 定时执行 + CI 触发 | Cron 生效、Webhook 可触发 |
| Week 6 | 稳定性优化 + 文档补齐 | 重试、日志脱敏、使用手册 |

---

## 6. 最小 API 清单（直接可开发）

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

### 6.4 计划与触发（P1）
- `POST /api/plans`
- `POST /api/plans/{id}/trigger`
- `PATCH /api/plans/{id}/enable`

---

## 7. 单人仓库文档最小集合（只保留必要）

建议仅保留以下 4 份核心文档：

1. `docs/technical-documentation-plan.md`（本文：功能与实现路线）
2. `docs/mvp-weekly-plan.md`（分周执行清单）
3. `docs/platform-mvp-ddl.sql`（数据库结构）
4. `README.md`（快速启动说明）

> 其他文档可以按需补，不建议一开始铺太多模板。

---

## 8. 本周可立即执行任务（Next Actions）

1. 先实现 P0 的 `Case + DataSet + Suite + Run` 四大实体 API。  
2. 跑通“手动触发 -> Karate 执行 -> 保存结果 -> 查询报告”主链路。  
3. 在 README 增加本地启动步骤（PostgreSQL、服务启动、示例执行）。  
4. 保持每周一个可运行里程碑，避免长周期闭门开发。  

---

如果后续仓库从“单人”升级到“多人”，可在此文档基础上补回权限、审计、流程治理模块，而无需推翻当前实现。
