# 学生健康管理系统 - API 接口文档 (新增模块)

## 1. 学生管理 (Student) - 导入导出
### 1.1 批量导入学生
- **URL**: `/api/students/import`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`
- **Permissions**: `student:import`
- **Params**:
    - `file`: Excel 文件 (.xlsx)
- **Response**:
```json
{
  "code": 200,
  "data": {
    "successCount": 10,
    "failCount": 0,
    "errorMessages": []
  }
}
```

### 1.2 导出学生列表
- **URL**: `/api/students/export`
- **Method**: `GET`
- **Permissions**: `student:export`
- **Params**: `studentNo`, `name`, `college`, `grade`, `clazz` (QueryParams)
- **Response**: Excel 文件下载

## 2. 体检报告 (Report) - 导出与打印
### 2.1 导出单一报告 (PDF)
- **URL**: `/api/reports/{id}/pdf`
- **Method**: `GET`
- **Permissions**: `report:export_pdf`
- **Response**: PDF 文件流 (Content-Type: application/pdf)

### 2.2 导出报告清单 (Excel)
- **URL**: `/api/reports/export`
- **Method**: `GET`
- **Permissions**: `report:export_excel`
- **Params**: `recordId` (体检记录ID), `status` (状态)
- **Response**: Excel 文件下载

## 3. 定时任务 (Scheduler) - 运维管理
### 3.1 创建任务
- **URL**: `/api/scheduler`
- **Method**: `POST`
- **Permissions**: `system:scheduler:create`
- **Body**:
```json
{
  "jobName": "每日数据清洗",
  "beanName": "dataCleanTask",
  "methodName": "run",
  "cronExpression": "0 0 1 * * ?"
}
```

### 3.2 立即执行一次
- **URL**: `/api/scheduler/{id}/run`
- **Method**: `POST`
- **Permissions**: `system:scheduler:run`

### 3.3 暂停/恢复任务
- **URL**: `/api/scheduler/{id}/status`
- **Method**: `PUT`
- **Params**: `status` (0:正常, 1:暂停)

## 4. 审计日志
系统自动记录关键操作（增删改、导入导出）。

## 5. 数据字典 (Dictionary)
### 5.1 获取字典列表
- **URL**: `/api/dict`
- **Method**: `GET`
- **Params**: `typeCode` (如 `gender`), `status`
- **Response**: List of DictionaryEntity

### 5.2 维护字典 (增加/修改/删除)
- **POST /api/dict**: 创建
- **PUT /api/dict/{id}**: 更新
- **DELETE /api/dict/{id}**: 删除
- **Permissions**: `system:dict:*`

## 6. 统计分析 (Analytics)
### 6.1 系统概览数据
- **URL**: `/api/analysis/overview`
- **Method**: `GET`
- **Response**:
```json
{
  "studentCount": 100,
  "batchCount": 5,
  "todayCheck": 10,
  "abnormalCount": 2
}
```

### 6.2 近7日活动趋势
- **URL**: `/api/analysis/activity`
- **Method**: `GET`
- **Response**: List of `{date: '2023-01-01', count: 5}`
