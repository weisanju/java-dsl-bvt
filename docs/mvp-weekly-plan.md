# Java + QLExpress 接口测试平台 MVP（TDD 排期）

## 1. 开发原则

- 单人维护，优先“能跑起来”。
- 单体架构，不拆服务，不引入额外中间件。
- 依赖仅保留 PostgreSQL（+ 应用本身）。
- 核心逻辑全部采用 **TDD（Red -> Green -> Refactor）**。

## 2. MVP 范围（P0）

1. 环境与变量管理
2. 用例（YAML/JSON `case_config`）与数据集（JSON/YAML）管理
3. 套件编排（顺序执行）
4. 手动触发执行
5. 报告查询（失败步骤、失败表达式）

## 3. 非目标（本期不做）

- Redis / RabbitMQ / MinIO
- 分布式 Runner
- 复杂权限体系
- 企业通知与审批

## 4. 5 周排期（TDD 驱动）

## Week 1：工程初始化 + TDD 骨架 + 配置解析器

### 目标
- 项目可启动、测试框架可运行。
- 明确引擎接口并建立测试模板。

### TDD 动作
- 建立单测基线（JUnit5 + AssertJ + Mockito）。
- 先写 `ConfigParserEngine` 失败测试（非法 YAML/JSON 必须失败）。
- 写最小实现让 YAML/JSON 都能解析通过（Green）。
- 抽取公共结构重构（Refactor）。

### 验收
- `./mvnw test` 可执行。
- 至少 1 个解析引擎测试完成 Red->Green 记录。

---

## Week 2：Case/DataSet 管理（YAML/JSON 测试先行）

### 目标
- 完成 case 与 dataset CRUD。

### TDD 动作
- 先写 API 层测试（请求校验、错误码、成功返回）。
- 实现 `t_test_case`、`t_test_case_data_set`。
- 增加 case_config 结构校验测试（非法 step 必须失败）。
- 增加“同一用例 JSON 与 YAML 解析结果一致”测试。

### 验收
- CRUD 功能可用。
- case_config 非法输入测试全通过。
- JSON/YAML 配置执行前归一化一致。

---

## Week 3：执行链路（引擎级 TDD）

### 目标
- 打通 `suite -> run -> result` 链路。

### TDD 动作
- 先写 `OrchestratorEngine` 组件测试（mock HTTP）。
- 再实现按 step 执行：解析配置 -> 渲染 -> 请求 -> 断言 -> 提取。
- 补失败路径测试：断言失败后记录 failed expression。

### 验收
- 一次 run 可执行多个 case。
- 失败能精确定位到 step + expression。

---

## Week 4：报告查询（回归优先）

### 目标
- 输出 run 概览和失败详情。

### TDD 动作
- 先写报告接口测试（字段完整性、排序、过滤）。
- 实现 `GET /api/runs/{id}`、`GET /api/runs/{id}/report`。
- 补回归测试：防止字段变更导致前端兼容问题。

### 验收
- 报告可用于问题定位。
- 报告接口回归测试通过。

---

## Week 5：定时任务（P1，测试驱动）

### 目标
- 支持 cron 定时执行。

### TDD 动作
- 先写计划触发测试（启用、禁用、防重入）。
- 实现 `t_test_plan` + `@Scheduled` 扫描执行。
- 补边界测试：空计划、非法 cron、并发触发。

### 验收
- 定时任务可稳定触发。
- 同计划同一时间不重复执行。

## 5. 交付标准（DoD）

- 每个功能模块都有对应测试，且先写测试再写实现。
- 所有核心引擎至少有成功/失败/边界三类测试。
- YAML/JSON 双格式必须有等价性测试。
- 主链路（创建 case -> 执行 run -> 查询报告）有回归测试。
- 测试全绿后才允许进入下一周功能。
