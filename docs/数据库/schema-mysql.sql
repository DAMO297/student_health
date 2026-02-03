-- 学生健康体检管理系统 - MySQL 8.0 建库建表脚本
-- 编码：utf8mb4
-- 引擎：InnoDB
-- 说明：按开题报告需求覆盖 学生/体检/报告/权限/审计/任务调度 等模块

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) 建库（可按需修改库名）
CREATE DATABASE IF NOT EXISTS student_health
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE student_health;

-- 2) 清理旧表（注意外键依赖顺序）
DROP TABLE IF EXISTS t_dashboard_stat_daily;
DROP TABLE IF EXISTS t_scheduler_log;
DROP TABLE IF EXISTS t_scheduler_job;
DROP TABLE IF EXISTS t_audit_log;
DROP TABLE IF EXISTS t_report_file;
DROP TABLE IF EXISTS t_report;
DROP TABLE IF EXISTS t_metric_threshold;
DROP TABLE IF EXISTS t_metric_dict;
DROP TABLE IF EXISTS t_exam_metric;
DROP TABLE IF EXISTS t_exam_record;
DROP TABLE IF EXISTS t_exam_batch;
DROP TABLE IF EXISTS t_role_permission;
DROP TABLE IF EXISTS t_user_role;
DROP TABLE IF EXISTS t_permission;
DROP TABLE IF EXISTS t_role;
DROP TABLE IF EXISTS t_user;
DROP TABLE IF EXISTS t_student;

-- 3) 学生主档
CREATE TABLE t_student (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  student_no VARCHAR(20) NOT NULL COMMENT '学号（业务唯一）',
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  gender TINYINT NOT NULL DEFAULT 0 COMMENT '性别：0未知 1男 2女',
  college VARCHAR(100) NOT NULL COMMENT '学院',
  grade VARCHAR(10) NOT NULL COMMENT '年级（如2022）',
  clazz VARCHAR(100) NOT NULL COMMENT '班级',
  id_card VARCHAR(32) NULL COMMENT '身份证（建议加密/脱敏存储）',
  phone VARCHAR(20) NULL COMMENT '手机号（建议脱敏）',
  email VARCHAR(100) NULL COMMENT '邮箱',
  birthday DATE NULL COMMENT '生日',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 2冻结 3毕业 4删除',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  created_by VARCHAR(50) NULL COMMENT '创建人',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  updated_by VARCHAR(50) NULL COMMENT '更新人',
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_no (student_no),
  KEY idx_college_grade_clazz (college, grade, clazz),
  KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- 4) 用户/角色/权限（RBAC）
CREATE TABLE t_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(50) NOT NULL COMMENT '登录名（管理员/医生）或学号（学生）',
  display_name VARCHAR(50) NOT NULL COMMENT '展示名',
  user_type TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型：1管理员 2医生 3学生',
  student_id BIGINT UNSIGNED NULL COMMENT '学生账号绑定（学生端）',
  password_hash VARCHAR(100) NOT NULL COMMENT '密码哈希（BCrypt）',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 2冻结',
  token_version INT NOT NULL DEFAULT 0 COMMENT 'Token版本（用于强制失效）',
  last_login_at DATETIME NULL COMMENT '最近登录时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  created_by VARCHAR(50) NULL COMMENT '创建人',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  updated_by VARCHAR(50) NULL COMMENT '更新人',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  KEY idx_student_id (student_id),
  CONSTRAINT fk_user_student FOREIGN KEY (student_id) REFERENCES t_student(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE t_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  code VARCHAR(50) NOT NULL COMMENT '角色编码（ROLE_ADMIN/ROLE_DOCTOR/ROLE_STUDENT）',
  name VARCHAR(50) NOT NULL COMMENT '角色名称',
  description VARCHAR(255) NULL COMMENT '描述',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE t_permission (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  code VARCHAR(100) NOT NULL COMMENT '权限点编码（resource:action）',
  name VARCHAR(100) NOT NULL COMMENT '权限名称',
  description VARCHAR(255) NULL COMMENT '描述',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_perm_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点表';

CREATE TABLE t_user_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_role_id (role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES t_user(id),
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES t_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

CREATE TABLE t_role_permission (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  role_id BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  permission_id BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_perm (role_id, permission_id),
  KEY idx_permission_id (permission_id),
  CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES t_role(id),
  CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES t_permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- 5) 体检批次
CREATE TABLE t_exam_batch (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  batch_name VARCHAR(100) NOT NULL COMMENT '批次名称',
  start_date DATE NOT NULL COMMENT '开始日期',
  end_date DATE NOT NULL COMMENT '结束日期',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1未开始 2进行中 3已结束 4归档',
  remark VARCHAR(255) NULL COMMENT '备注',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) NULL,
  PRIMARY KEY (id),
  KEY idx_status_date (status, start_date, end_date),
  KEY idx_batch_name (batch_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检批次表';

-- 6) 体检记录（主表）
CREATE TABLE t_exam_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  batch_id BIGINT UNSIGNED NOT NULL COMMENT '体检批次ID',
  student_id BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
  doctor_id BIGINT UNSIGNED NOT NULL COMMENT '录入医生用户ID',
  record_time DATETIME NOT NULL COMMENT '体检/录入时间',
  source TINYINT NOT NULL DEFAULT 1 COMMENT '来源：1手工 2批量导入',
  audit_status TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态：1待审核 2已审核 3驳回（可选）',
  abnormal_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否存在异常项：0否 1是',
  remark VARCHAR(255) NULL COMMENT '备注',
  version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) NULL,
  PRIMARY KEY (id),
  KEY idx_batch_student (batch_id, student_id),
  KEY idx_student_time (student_id, record_time),
  KEY idx_doctor_time (doctor_id, record_time),
  KEY idx_abnormal (abnormal_flag),
  CONSTRAINT fk_record_batch FOREIGN KEY (batch_id) REFERENCES t_exam_batch(id),
  CONSTRAINT fk_record_student FOREIGN KEY (student_id) REFERENCES t_student(id),
  CONSTRAINT fk_record_doctor FOREIGN KEY (doctor_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检记录主表';

-- 7) 体检指标明细（扩展型）
CREATE TABLE t_exam_metric (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  record_id BIGINT UNSIGNED NOT NULL COMMENT '体检记录ID',
  metric_key VARCHAR(50) NOT NULL COMMENT '指标Key（如 height, weight, sbp）',
  metric_name VARCHAR(100) NOT NULL COMMENT '指标名称（冗余，便于展示）',
  value_decimal DECIMAL(10,2) NULL COMMENT '数值型值',
  value_text VARCHAR(255) NULL COMMENT '文本型值（如描述/阴阳性）',
  unit VARCHAR(20) NULL COMMENT '单位',
  ref_low DECIMAL(10,2) NULL COMMENT '参考下限（用于报告展示）',
  ref_high DECIMAL(10,2) NULL COMMENT '参考上限（用于报告展示）',
  abnormal_flag TINYINT NOT NULL DEFAULT 0 COMMENT '异常：0正常 1偏低 2偏高 3异常（文本）',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_record_metric (record_id, metric_key),
  KEY idx_metric_key (metric_key),
  CONSTRAINT fk_metric_record FOREIGN KEY (record_id) REFERENCES t_exam_record(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检指标明细表';

-- 8) 指标字典与阈值（可配置）
CREATE TABLE t_metric_dict (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  metric_key VARCHAR(50) NOT NULL COMMENT '指标Key（唯一）',
  name VARCHAR(100) NOT NULL COMMENT '指标名称',
  unit VARCHAR(20) NULL COMMENT '单位',
  value_type TINYINT NOT NULL DEFAULT 1 COMMENT '类型：1数值 2文本',
  required TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填：0否 1是',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0否 1是',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序',
  remark VARCHAR(255) NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_metric_key (metric_key),
  KEY idx_enabled_sort (enabled, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检指标字典';

CREATE TABLE t_metric_threshold (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  metric_key VARCHAR(50) NOT NULL COMMENT '指标Key',
  gender TINYINT NULL COMMENT '性别：0未知 1男 2女（NULL表示不区分）',
  grade VARCHAR(10) NULL COMMENT '年级（NULL表示不区分）',
  age_min INT NULL COMMENT '最小年龄（NULL表示不限制）',
  age_max INT NULL COMMENT '最大年龄（NULL表示不限制）',
  ref_low DECIMAL(10,2) NULL COMMENT '参考下限',
  ref_high DECIMAL(10,2) NULL COMMENT '参考上限',
  remark VARCHAR(255) NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_threshold_lookup (metric_key, gender, grade, age_min, age_max),
  CONSTRAINT fk_threshold_metric FOREIGN KEY (metric_key) REFERENCES t_metric_dict(metric_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标阈值/参考范围配置';

-- 9) 体检报告与文件
CREATE TABLE t_report (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  record_id BIGINT UNSIGNED NOT NULL COMMENT '体检记录ID（建议一对一）',
  report_no VARCHAR(50) NOT NULL COMMENT '报告编号（唯一）',
  version INT NOT NULL DEFAULT 1 COMMENT '报告版本号',
  status TINYINT NOT NULL DEFAULT 2 COMMENT '状态：1草稿 2已生成 3已归档',
  summary TEXT NULL COMMENT '综合健康评估摘要',
  doctor_advice TEXT NULL COMMENT '医师建议（可编辑）',
  generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  archived_at DATETIME NULL COMMENT '归档时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_report_no (report_no),
  UNIQUE KEY uk_record_report (record_id),
  KEY idx_status_time (status, generated_at),
  CONSTRAINT fk_report_record FOREIGN KEY (record_id) REFERENCES t_exam_record(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检报告表';

CREATE TABLE t_report_file (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  report_id BIGINT UNSIGNED NOT NULL COMMENT '报告ID',
  file_type TINYINT NOT NULL COMMENT '文件类型：1PDF 2EXCEL',
  file_name VARCHAR(255) NOT NULL COMMENT '文件名',
  storage_path VARCHAR(500) NOT NULL COMMENT '存储路径/URL',
  file_size BIGINT UNSIGNED NULL COMMENT '文件大小（字节）',
  file_hash VARCHAR(64) NULL COMMENT '文件hash（可选）',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_report_type (report_id, file_type),
  CONSTRAINT fk_report_file_report FOREIGN KEY (report_id) REFERENCES t_report(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告导出文件记录';

-- 10) 审计日志
CREATE TABLE t_audit_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT UNSIGNED NULL COMMENT '操作者用户ID',
  role_code VARCHAR(50) NULL COMMENT '操作者角色（冗余）',
  action VARCHAR(50) NOT NULL COMMENT '动作（LOGIN/IMPORT/EXPORT/UPDATE...）',
  resource VARCHAR(100) NULL COMMENT '资源标识（如 student/report/exam_record）',
  resource_id VARCHAR(100) NULL COMMENT '资源ID（字符串化）',
  detail TEXT NULL COMMENT '详情摘要（需脱敏）',
  result TINYINT NOT NULL DEFAULT 1 COMMENT '结果：1成功 2失败',
  cost_ms INT NULL COMMENT '耗时ms',
  ip VARCHAR(64) NULL COMMENT '来源IP',
  user_agent VARCHAR(255) NULL COMMENT 'User-Agent',
  trace_id VARCHAR(64) NULL COMMENT '链路追踪ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY (id),
  KEY idx_audit_user_time (user_id, created_at),
  KEY idx_audit_action_time (action, created_at),
  CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- 11) 任务调度（简化版，满足“Quartz任务管理”需求）
CREATE TABLE t_scheduler_job (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  job_code VARCHAR(100) NOT NULL COMMENT '任务编码（唯一）',
  job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
  job_desc VARCHAR(255) NULL COMMENT '任务描述',
  cron_expr VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 2停用',
  last_run_at DATETIME NULL COMMENT '上次执行时间',
  next_run_at DATETIME NULL COMMENT '下次执行时间（可选预估）',
  last_result TINYINT NULL COMMENT '最近一次结果：1成功 2失败',
  last_message VARCHAR(500) NULL COMMENT '最近一次信息/错误摘要',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_job_code (job_code),
  KEY idx_job_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务配置表（简化版）';

CREATE TABLE t_scheduler_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  job_id BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
  run_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  end_at DATETIME NULL COMMENT '结束时间',
  result TINYINT NOT NULL DEFAULT 1 COMMENT '结果：1成功 2失败',
  cost_ms INT NULL COMMENT '耗时ms',
  message TEXT NULL COMMENT '执行日志/错误信息',
  PRIMARY KEY (id),
  KEY idx_job_time (job_id, run_at),
  CONSTRAINT fk_sched_log_job FOREIGN KEY (job_id) REFERENCES t_scheduler_job(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行日志表';

-- 12) 仪表盘每日统计（可选：用于预聚合展示）
CREATE TABLE t_dashboard_stat_daily (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  stat_date DATE NOT NULL COMMENT '统计日期',
  pv INT NOT NULL DEFAULT 0 COMMENT '访问量PV',
  uv INT NOT NULL DEFAULT 0 COMMENT '访问用户UV（简化口径）',
  new_exam_records INT NOT NULL DEFAULT 0 COMMENT '新增体检记录数',
  new_reports INT NOT NULL DEFAULT 0 COMMENT '新增报告数',
  import_success INT NOT NULL DEFAULT 0 COMMENT '导入成功数（汇总）',
  import_fail INT NOT NULL DEFAULT 0 COMMENT '导入失败数（汇总）',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仪表盘每日统计（可选）';

SET FOREIGN_KEY_CHECKS = 1;



