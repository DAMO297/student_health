package org.example.exam;

import org.example.common.BizException;
import org.example.common.PageResult;
import org.example.exam.dto.ExamMetricSaveRequest;
import org.example.exam.dto.ExamRecordSaveRequest;
import org.example.user.UserEntity;
import org.example.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExamService {

    private final ExamBatchMapper batchMapper;
    private final ExamRecordMapper recordMapper;
    private final ExamMetricMapper metricMapper;
    private final org.example.student.StudentMapper studentMapper;
    private final UserService userService;

    public ExamService(ExamBatchMapper batchMapper, ExamRecordMapper recordMapper, ExamMetricMapper metricMapper,
            org.example.student.StudentMapper studentMapper,
            UserService userService) {
        this.batchMapper = batchMapper;
        this.recordMapper = recordMapper;
        this.metricMapper = metricMapper;
        this.studentMapper = studentMapper;
        this.userService = userService;
    }

    public List<ExamBatchEntity> listBatches(Integer status) {
        return batchMapper.selectAll(status);
    }

    public List<ExamRecordEntity> listByBatchId(Long batchId) {
        return recordMapper.selectByBatchId(batchId);
    }

    public ExamBatchEntity getBatch(Long id) {
        ExamBatchEntity e = batchMapper.selectById(id);
        if (e == null)
            throw BizException.notFound("体检批次不存在");
        return e;
    }

    @Transactional
    public ExamBatchEntity createBatch(ExamBatchEntity e, String operator) {
        e.setCreatedBy(operator);
        e.setUpdatedBy(operator);
        e.setDeleted(0);
        if (e.getStatus() == null)
            e.setStatus(1);
        batchMapper.insert(e);
        return getBatch(e.getId());
    }

    @Transactional
    public ExamBatchEntity updateBatch(Long id, ExamBatchEntity req, String operator) {
        ExamBatchEntity e = getBatch(id);
        e.setBatchName(req.getBatchName());
        e.setStartDate(req.getStartDate());
        e.setEndDate(req.getEndDate());
        e.setStatus(req.getStatus());
        e.setRemark(req.getRemark());
        e.setUpdatedBy(operator);
        batchMapper.update(e);
        return getBatch(id);
    }

    @Transactional
    public void deleteBatch(Long id) {
        int n = batchMapper.softDelete(id);
        if (n == 0)
            throw BizException.notFound("体检批次不存在或已删除");
    }

    @Transactional
    public void updateBatchStatus(Long id, Integer status, String operator) {
        ExamBatchEntity e = getBatch(id);
        e.setStatus(status);
        e.setUpdatedBy(operator);
        batchMapper.update(e);
    }

    public ExamRecordEntity getRecord(Long id) {
        ExamRecordEntity e = recordMapper.selectById(id);
        if (e == null)
            throw BizException.notFound("体检记录不存在");
        return e;
    }

    public List<ExamMetricEntity> listMetrics(Long recordId) {
        return metricMapper.selectByRecordId(recordId);
    }

    public PageResult<org.example.exam.dto.ExamRecordVO> pageRecords(Long batchId, Long studentId, Long doctorId,
            Integer abnormalFlag, int page, int pageSize) {
        int offset = Math.max(0, page - 1) * pageSize;
        long total = recordMapper.count(batchId, studentId, doctorId, abnormalFlag);
        return new PageResult<>(total,
                recordMapper.selectPageVO(batchId, studentId, doctorId, abnormalFlag, offset, pageSize));
    }

    @Transactional
    public ExamRecordEntity createRecord(ExamRecordSaveRequest req, String operatorUsername) {
        UserEntity doctor = userService.getByUsername(operatorUsername);
        if (doctor == null)
            throw BizException.unauthorized();

        ExamRecordEntity e = new ExamRecordEntity();
        e.setBatchId(req.getBatchId());
        e.setStudentId(req.getStudentId());
        e.setDoctorId(doctor.getId());
        e.setSource(req.getSource() == null ? 1 : req.getSource());
        e.setAuditStatus(req.getAuditStatus() == null ? 1 : req.getAuditStatus());
        e.setRemark(req.getRemark());
        e.setAbnormalFlag(0);
        e.setDeleted(0);
        e.setCreatedBy(operatorUsername);
        e.setUpdatedBy(operatorUsername);
        if (req.getRecordTime() == null || req.getRecordTime().trim().isEmpty()) {
            e.setRecordTime(LocalDateTime.now());
        } else {
            e.setRecordTime(LocalDateTime.parse(req.getRecordTime().trim(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        recordMapper.insert(e);

        saveMetrics(e.getId(), req.getMetrics());
        return getRecord(e.getId());
    }

    @Transactional
    public ExamRecordEntity updateRecord(Long id, ExamRecordSaveRequest req, String operatorUsername) {
        ExamRecordEntity e = getRecord(id);
        // 关键字段保持一致：允许更新批次/学生/时间/备注；doctorId 默认跟随操作者
        UserEntity doctor = userService.getByUsername(operatorUsername);
        if (doctor == null)
            throw BizException.unauthorized();

        e.setBatchId(req.getBatchId());
        e.setStudentId(req.getStudentId());
        e.setDoctorId(doctor.getId());
        e.setSource(req.getSource() == null ? e.getSource() : req.getSource());
        e.setAuditStatus(req.getAuditStatus() == null ? e.getAuditStatus() : req.getAuditStatus());
        e.setRemark(req.getRemark());
        e.setUpdatedBy(operatorUsername);
        if (req.getRecordTime() != null && !req.getRecordTime().trim().isEmpty()) {
            e.setRecordTime(LocalDateTime.parse(req.getRecordTime().trim(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        recordMapper.update(e);

        saveMetrics(id, req.getMetrics());
        return getRecord(id);
    }

    @Transactional
    public void deleteRecord(Long id) {
        int n = recordMapper.softDelete(id);
        if (n == 0)
            throw BizException.notFound("体检记录不存在或已删除");
        metricMapper.deleteByRecordId(id);
    }

    private void saveMetrics(Long recordId, List<ExamMetricSaveRequest> metrics) {
        metricMapper.deleteByRecordId(recordId);
        if (metrics == null || metrics.isEmpty())
            return;

        List<ExamMetricEntity> list = new ArrayList<>();
        for (ExamMetricSaveRequest m : metrics) {
            if (m.getMetricKey() == null || m.getMetricKey().trim().isEmpty())
                continue;
            ExamMetricEntity em = new ExamMetricEntity();
            em.setRecordId(recordId);
            em.setMetricKey(m.getMetricKey());
            em.setMetricName(m.getMetricName());
            em.setValueDecimal(m.getValueDecimal());
            em.setValueText(m.getValueText());
            em.setUnit(m.getUnit());
            em.setRefLow(m.getRefLow());
            em.setRefHigh(m.getRefHigh());
            em.setAbnormalFlag(m.getAbnormalFlag() == null ? 0 : m.getAbnormalFlag());
            list.add(em);
        }
        if (!list.isEmpty())
            metricMapper.insertBatch(list);
    }

    @Transactional
    public org.example.common.dto.ImportResult importRecords(java.io.InputStream is, Long batchId, String operator) {
        org.example.common.dto.ImportResult result = new org.example.common.dto.ImportResult();
        List<org.example.exam.dto.ExamRecordExcelModel> list;
        try {
            list = com.alibaba.excel.EasyExcel.read(is).head(org.example.exam.dto.ExamRecordExcelModel.class).sheet()
                    .doReadSync();
        } catch (Exception e) {
            throw new BizException(400, "读取Excel文件失败: " + e.getMessage());
        }

        UserEntity user = userService.getByUsername(operator);
        if (user == null)
            throw BizException.unauthorized();

        for (int i = 0; i < list.size(); i++) {
            org.example.exam.dto.ExamRecordExcelModel row = list.get(i);
            int lineNum = i + 2;
            try {
                processImportRow(row, batchId, user.getId(), operator);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.setFailCount(result.getFailCount() + 1);
                result.getErrorMessages().add("第" + lineNum + "行(" + row.getStudentNo() + ")导入失败: " + e.getMessage());
            }
        }
        return result;
    }

    private void processImportRow(org.example.exam.dto.ExamRecordExcelModel row, Long batchId, Long doctorId,
            String operator) {
        if (row.getStudentNo() == null || row.getStudentNo().isEmpty())
            throw new RuntimeException("学号为空");

        org.example.student.StudentEntity student = studentMapper.selectByStudentNo(row.getStudentNo());
        if (student == null)
            throw new RuntimeException("学生不存在");

        ExamRecordEntity e = new ExamRecordEntity();
        e.setBatchId(batchId);
        e.setStudentId(student.getId());
        e.setDoctorId(doctorId);
        e.setSource(2); // 2=Excel Import
        e.setAuditStatus(1); // 1=Pending
        e.setRemark(row.getRemark());
        e.setAbnormalFlag(0);
        e.setDeleted(0);
        e.setCreatedBy(operator);
        e.setUpdatedBy(operator);

        if (row.getRecordTime() != null && !row.getRecordTime().trim().isEmpty()) {
            try {
                e.setRecordTime(LocalDateTime.parse(row.getRecordTime().trim(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (Exception ex) {
                e.setRecordTime(LocalDateTime.now());
            }
        } else {
            e.setRecordTime(LocalDateTime.now());
        }

        recordMapper.insert(e);

        // Convert metrics
        List<ExamMetricSaveRequest> metrics = new ArrayList<>();
        addMetric(metrics, "height", "身高", row.getHeight(), "cm");
        addMetric(metrics, "weight", "体重", row.getWeight(), "kg");
        addMetric(metrics, "blood_pressure_sys", "收缩压", row.getSbp(), "mmHg");
        addMetric(metrics, "blood_pressure_dia", "舒张压", row.getDbp(), "mmHg");
        addMetric(metrics, "vision_left", "左眼视力", row.getVisionLeft(), null);
        addMetric(metrics, "vision_right", "右眼视力", row.getVisionRight(), null);

        saveMetrics(e.getId(), metrics);
    }

    private void addMetric(List<ExamMetricSaveRequest> list, String key, String name, java.math.BigDecimal value,
            String unit) {
        if (value == null)
            return;
        ExamMetricSaveRequest m = new ExamMetricSaveRequest();
        m.setMetricKey(key);
        m.setMetricName(name);
        m.setValueDecimal(value);
        m.setUnit(unit);
        list.add(m);
    }
}
