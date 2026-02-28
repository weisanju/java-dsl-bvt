# Case 配置规范（YAML / JSON）

## 1. 目标

定义统一的用例配置结构，支持 YAML 与 JSON 两种输入格式。  
服务端会把两种格式解析为同一内部结构（`normalized_definition`）执行。

---

## 2. 顶层结构

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| meta | object | 是 | 用例基础信息 |
| steps | array | 是 | 执行步骤列表（按顺序） |

### 2.1 meta

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| caseCode | string | 是 | 用例编码（项目内唯一） |
| caseName | string | 是 | 用例名称 |
| version | integer | 否 | 配置版本，默认 1 |
| tags | array[string] | 否 | 标签 |

### 2.2 step

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| name | string | 是 | 步骤名 |
| request | object | 是 | 请求定义 |
| assertions | array[string] | 否 | QLExpress 断言表达式列表 |
| extracts | object | 否 | 变量提取表达式映射 |
| skipWhen | string | 否 | 条件跳过表达式（QLExpress） |

### 2.3 request

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| method | string | 是 | GET/POST/PUT/DELETE/PATCH |
| urlExpr | string | 是 | URL 表达式 |
| headersExpr | object | 否 | Header 表达式映射 |
| bodyExpr | string/object | 否 | Body 表达式或对象 |
| timeoutMs | integer | 否 | 超时毫秒，默认 5000 |

---

## 3. JSON 示例

```json
{
  "meta": {
    "caseCode": "LOGIN_001",
    "caseName": "登录成功",
    "version": 1,
    "tags": ["smoke", "auth"]
  },
  "steps": [
    {
      "name": "login",
      "request": {
        "method": "POST",
        "urlExpr": "baseUrl + '/api/login'",
        "headersExpr": {
          "Content-Type": "'application/json'"
        },
        "bodyExpr": "{'username': username, 'password': password}",
        "timeoutMs": 5000
      },
      "assertions": [
        "status == 200",
        "body.code == 0"
      ],
      "extracts": {
        "token": "body.data.token"
      }
    },
    {
      "name": "get-profile",
      "request": {
        "method": "GET",
        "urlExpr": "baseUrl + '/api/profile'",
        "headersExpr": {
          "Authorization": "'Bearer ' + token"
        }
      },
      "assertions": [
        "status == 200",
        "body.data.userId != null"
      ]
    }
  ]
}
```

---

## 4. YAML 示例

```yaml
meta:
  caseCode: LOGIN_001
  caseName: 登录成功
  version: 1
  tags: [smoke, auth]

steps:
  - name: login
    request:
      method: POST
      urlExpr: "baseUrl + '/api/login'"
      headersExpr:
        Content-Type: "'application/json'"
      bodyExpr: "{'username': username, 'password': password}"
      timeoutMs: 5000
    assertions:
      - "status == 200"
      - "body.code == 0"
    extracts:
      token: "body.data.token"

  - name: get-profile
    request:
      method: GET
      urlExpr: "baseUrl + '/api/profile'"
      headersExpr:
        Authorization: "'Bearer ' + token"
    assertions:
      - "status == 200"
      - "body.data.userId != null"
```

---

## 5. 解析与归一化规则

1. `configFormat` 为 `JSON` 时按 JSON 解析；为 `YAML` 时按 YAML 解析。  
2. 解析后都转换为同一结构（`normalized_definition`）。  
3. 执行引擎只读取 `normalized_definition`。  
4. 保存原始文本 `config_content` 用于回显与导出。  

---

## 6. 校验规则（最小集）

1. `meta.caseCode`、`meta.caseName` 必填。  
2. `steps` 至少 1 个。  
3. 每个 step 必须包含 `name` 和 `request.method`、`request.urlExpr`。  
4. `assertions`、`extracts`、`skipWhen` 表达式语法错误时拒绝保存。  
5. `method` 必须是允许值（GET/POST/PUT/DELETE/PATCH）。  
6. YAML 不允许 anchor/alias 与多文档语法。  

---

## 6.1 执行默认策略（固定）

1. step 失败采用 fail-fast：终止当前 case。  
2. case 失败不终止 suite：继续执行后续 case。  

---

## 7. API 请求示例

### 7.1 保存 YAML 配置

```json
{
  "caseCode": "LOGIN_001",
  "caseName": "登录成功",
  "configFormat": "YAML",
  "configContent": "meta:\n  caseCode: LOGIN_001\n  ..."
}
```

### 7.2 保存 JSON 配置

```json
{
  "caseCode": "LOGIN_001",
  "caseName": "登录成功",
  "configFormat": "JSON",
  "configContent": "{\"meta\":{\"caseCode\":\"LOGIN_001\"},\"steps\":[...]}"
}
```
