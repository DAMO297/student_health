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
    private final UserService userService;

    public ExamService(ExamBatchMapper batchMapper, ExamRecordMapper recordMapper, ExamMetricMapper metricMapper, UserService userService) {
        this.batchMapper = batchMapper;
        this.recordMapper = recordMapper;
        this.metricMapper = metricMapper;
        this.userService = userService;
    }

    public List<ExamBatchEntity> listBatches(Integer status) {
        return batchMapper.selectAll(status);
    }

    public ExamBatchEntity getBatch(Long id) {
        ExamBatchEntity e = batchMapper.selectById(id);
        if (e == null) throw BizException.notFound("体检批次不存在");
        return e;
    }

    @Transactional
    public ExamBatchEntity createBatch(ExamBatchEntity e, String operator) {
        e.setCreatedBy(operator);
        e.setUpdatedBy(operator);
        e.setDeleted(0);
        if (e.getStatus() == null) e.setStatus(1);
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
        if (n == 0) throw BizException.notFound("体检批次不存在或已删除");
    }

    public ExamRecordEntity getRecord(Long id) {
        ExamRecordEntity e = recordMapper.selectById(id);
        if (e == null) throw BizException.notFound("体检记录不存在");
        return e;
    }

    public List<ExamMetricEntity> listMetrics(Long recordId) {
        return metricMapper.selectByRecordId(recordId);
    }

    public PageResult<ExamRecordEntity> pageRecords(Long batchId, Long studentId, Long doctorId, Integer abnormalFlag, int page, int pageSize) {
        int offset = Math.max(0, page - 1) * pageSize;
        long total = recordMapper.count(batchId, studentId, doctorId, abnormalFlag);
        return new PageResult<>(total, recordMapper.selectPage(batchId, studentId, doctorId, abnormalFlag, offset, pageSize));
    }

    @Transactional
    public ExamRecordEntity createRecord(ExamRecordSaveRequest req, String operatorUsername) {
        UserEntity doctor = userService.getByUsername(operatorUsername);
        if (doctor == null) throw BizException.unauthorized();

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
            e.setRecordTime(LocalDateTime.parse(req.getRecordTime().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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
        if (doctor == null) throw BizException.unauthorized();

        e.setBatchId(req.getBatchId());
        e.setStudentId(req.getStudentId());
        e.setDoctorId(doctor.getId());
        e.setSource(req.getSource() == null ? e.getSource() : req.getSource());
        e.setAuditStatus(req.getAuditStatus() == null ? e.getAuditStatus() : req.getAuditStatus());
        e.setRemark(req.getRemark());
        e.setUpdatedBy(operatorUsername);
        if (req.getRecordTime() != null && !req.getRecordTime().trim().isEmpty()) {
            e.setRecordTime(LocalDateTime.parse(req.getRecordTime().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        recordMapper.update(e);

        saveMetrics(id, req.getMetrics());
        return getRecord(id);
    }

    @Transactional
    public void deleteRecord(Long id) {
        int n = recordMapper.softDelete(id);
        if (n == 0) throw BizException.notFound("体检记录不存在或已删除");
        metricMapper.deleteByRecordId(id);
    }

    private void saveMetrics(Long recordId, List<ExamMetricSaveRequest> metrics) {
        metricMapper.deleteByRecordId(recordId);
        if (metrics == null || metrics.isEmpty()) return;

        List<ExamMetricEntity> list = new ArrayList<>();
        for (ExamMetricSaveRequest m : metrics) {
            if (m.getMetricKey() == null || m.getMetricKey().trim().isEmpty()) continue;
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
        if (!list.isEmpty()) metricMapper.insertBatch(list);
    }
}





