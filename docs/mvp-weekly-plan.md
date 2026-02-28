# Java + QLExpress 接口测试平台 MVP（极简版排期）

## 1. 原则

- 单人维护，优先“能跑起来”。
- 单体架构，不拆服务，不引入额外中间件。
- 依赖仅保留 PostgreSQL（+ 应用本身）。
- 执行链路采用**自定义引擎**，表达式统一用 QLExpress。

## 2. MVP 范围（P0）

1. 环境与变量管理
2. 用例（case_definition）与数据集（JSON/YAML）管理
3. 套件编排（顺序执行）
4. 手动触发执行
5. 执行报告查询（失败步骤、失败表达式）

## 3. 非目标（本期不做）

- Redis / RabbitMQ / MinIO
- 分布式 Runner
- 复杂权限体系
- 企业通知与审批

## 4. 5 周排期（最短路径）

## Week 1：工程初始化 + 引擎骨架 + 最小 DDL

### 目标
- 起一个可运行的 Spring Boot 服务。
- 建立自定义引擎接口与最小数据表。

### 动作
- 建立分层结构（controller/service/repository）。
- 设计引擎接口：`ContextEngine`、`HttpExecuteEngine`、`AssertEngine`、`ExtractEngine`、`OrchestratorEngine`。
- 落地 `docs/platform-mvp-ddl.sql`。
- 健康检查、统一异常处理。

### 验收
- 服务可启动，数据库可读写。
- 引擎接口可编译，具备基础单测骨架。

---

## Week 2：Case 与 DataSet 管理（QLExpress 结构）

### 目标
- 完成用例和数据集 CRUD。

### 动作
- 实现 `t_test_case`、`t_test_case_data_set` API。
- 约束 case_definition 结构：step/request/assert/extract。
- JSON/YAML 入库前语法校验。

### 验收
- 能创建/修改 case，并绑定数据集。
- case_definition 校验失败能给出明确错误。

---

## Week 3：QLExpress 执行链路打通

### 目标
- 从套件触发执行并写入结果。

### 动作
- 实现 `t_test_suite`、`t_test_suite_case`。
- 实现 `POST /api/suites/{id}/run`。
- 按顺序执行 step：渲染请求 -> 发请求 -> 断言 -> 提取变量。
- 断言与提取统一通过 QLExpress。

### 验收
- 一次 run 可完整执行多个 case 并保存状态。
- 报错时可定位到失败 step 与失败表达式。

---

## Week 4：报告查询

### 目标
- 能看 run 概览和失败详情。

### 动作
- 实现 `GET /api/runs/{id}`。
- 实现 `GET /api/runs/{id}/report`。
- 报告展示失败表达式、错误信息、响应摘要。

### 验收
- 能通过报告快速定位失败原因。

---

## Week 5：定时任务（P1 轻量）

### 目标
- 支持最基础定时回归。

### 动作
- 增加 `t_test_plan`。
- `@Scheduled` 扫描可执行计划并触发 run。
- 防重入：同计划同一时间只跑一个实例。

### 验收
- 可按 cron 自动触发并产生 run 记录。

## 5. 最小交付标准

- 能创建 case + dataset + suite。
- 能手动触发执行并查看报告。
- 报告能显示失败 step 与失败 QL 表达式。
- 能配置并执行至少一个定时计划。
