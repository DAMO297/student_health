package org.example.report;

import org.example.common.BizException;
import org.example.common.PageResult;
import org.example.exam.ExamMetricEntity;
import org.example.exam.ExamRecordEntity;
import org.example.exam.ExamService;
import org.example.student.StudentEntity;
import org.example.student.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.io.OutputStream;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.example.report.dto.ReportExcelModel;

@Service
public class ReportService {

    private final ReportMapper reportMapper;
    private final ExamService examService;
    private final StudentMapper studentMapper;

    public ReportService(ReportMapper reportMapper, ExamService examService, StudentMapper studentMapper) {
        this.reportMapper = reportMapper;
        this.examService = examService;
        this.studentMapper = studentMapper;
    }

    public ReportEntity get(Long id) {
        ReportEntity r = reportMapper.selectById(id);
        if (r == null)
            throw BizException.notFound("报告不存在");
        return r;
    }

    public PageResult<ReportEntity> page(Long recordId, Long studentId, Integer status, int page, int pageSize) {
        int offset = Math.max(0, page - 1) * pageSize;
        long total = reportMapper.count(recordId, studentId, status);
        return new PageResult<>(total, reportMapper.selectPage(recordId, studentId, status, offset, pageSize));
    }

    @Transactional
    public ReportEntity generate(Long recordId, String operator) {
        // 若已有报告，直接返回（幂等）
        ReportEntity exist = reportMapper.selectByRecordId(recordId);
        if (exist != null)
            return exist;

        // 获取学生姓名用于生成报告编号
        ExamRecordEntity record = examService.getRecord(recordId);
        StudentEntity student = studentMapper.selectById(record.getStudentId());
        String studentName = student != null ? student.getName() : "未知";

        // 基于体检指标生成简化 summary（后续可扩展规则库）
        List<ExamMetricEntity> metrics = examService.listMetrics(recordId);
        String summary = buildSummary(metrics);

        ReportEntity r = new ReportEntity();
        r.setRecordId(recordId);
        r.setReportNo(genReportNo(studentName));
        r.setVersion(1);
        r.setStatus(1); // 1=待审核，等待医生填写建议
        r.setSummary(summary);
        r.setDoctorAdvice(null);
        r.setCreatedBy(operator);
        r.setUpdatedBy(operator);
        reportMapper.insert(r);
        return reportMapper.selectByRecordId(recordId);
    }

    @Transactional
    public ReportEntity updateAdvice(Long id, String summary, String doctorAdvice, String operator) {
        ReportEntity report = get(id);
        if (report.getStatus() != null && report.getStatus() == 3) {
            throw new BizException(400, "报告已归档，无法修改");
        }
        int n = reportMapper.updateAdvice(id, doctorAdvice, summary, operator);
        if (n == 0)
            throw BizException.notFound("报告不存在或已删除");
        return get(id);
    }

    private String genReportNo(String studentName) {
        return "R" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + studentName;
    }

    private String buildSummary(List<ExamMetricEntity> metrics) {
        BigDecimal heightCm = findDecimal(metrics, "height");
        BigDecimal weightKg = findDecimal(metrics, "weight");
        BigDecimal sbp = findDecimal(metrics, "sbp");
        BigDecimal dbp = findDecimal(metrics, "dbp");

        StringBuilder sb = new StringBuilder();
        if (heightCm != null && weightKg != null && heightCm.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal h = heightCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            BigDecimal bmi = weightKg.divide(h.multiply(h), 1, RoundingMode.HALF_UP);
            sb.append("BMI=").append(bmi).append("；");
            if (bmi.compareTo(new BigDecimal("18.5")) < 0)
                sb.append("偏瘦；");
            else if (bmi.compareTo(new BigDecimal("24")) < 0)
                sb.append("正常；");
            else if (bmi.compareTo(new BigDecimal("28")) < 0)
                sb.append("超重；");
            else
                sb.append("肥胖；");
        }
        if (sbp != null && dbp != null) {
            sb.append("血压=").append(sbp).append("/").append(dbp).append("mmHg；");
            if (sbp.compareTo(new BigDecimal("140")) >= 0 || dbp.compareTo(new BigDecimal("90")) >= 0) {
                sb.append("高血压风险；");
            } else if (sbp.compareTo(new BigDecimal("120")) >= 0 || dbp.compareTo(new BigDecimal("80")) >= 0) {
                sb.append("血压偏高；");
            } else {
                sb.append("血压正常；");
            }
        }
        if (sb.length() == 0)
            return "指标不足，无法生成综合评估";
        return sb.toString();
    }

    private BigDecimal findDecimal(List<ExamMetricEntity> metrics, String key) {
        if (metrics == null)
            return null;
        for (ExamMetricEntity m : metrics) {
            if (key.equals(m.getMetricKey()))
                return m.getValueDecimal();
        }
        return null;
    }

    public List<ReportExcelModel> exportList(Long recordId, Integer status) {
        return reportMapper.selectExportList(recordId, status);
    }

    public void exportPdf(Long id, OutputStream os) {
        ReportEntity report = get(id);
        List<ReportExcelModel> details = reportMapper.selectExportList(report.getRecordId(), null);
        if (details.isEmpty())
            throw BizException.notFound("报告数据异常");

        ReportExcelModel info = details.get(0);
        List<ExamMetricEntity> metrics = examService.listMetrics(report.getRecordId());

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, os);
            document.open();

            // Font setting for Chinese
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font textFont = new Font(bfChinese, 12, Font.NORMAL);
            Font boldFont = new Font(bfChinese, 12, Font.BOLD);

            // Title
            Paragraph title = new Paragraph("学生健康体检报告", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 1. Basic Info
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);

            addCell(infoTable, "学号", boldFont);
            addCell(infoTable, info.getStudentNo(), textFont);
            addCell(infoTable, "姓名", boldFont);
            addCell(infoTable, info.getStudentName(), textFont);

            addCell(infoTable, "学院", boldFont);
            addCell(infoTable, info.getCollege(), textFont);
            addCell(infoTable, "体检批次", boldFont);
            addCell(infoTable, info.getBatchName(), textFont);

            addCell(infoTable, "报告编号", boldFont);
            addCell(infoTable, info.getReportNo(), textFont);
            addCell(infoTable, "生成时间", boldFont);
            addCell(infoTable, info.getGeneratedAt() == null ? "" : info.getGeneratedAt().toString(), textFont);

            document.add(infoTable);

            // 2. Metrics
            Paragraph p2 = new Paragraph("体检详情：", boldFont);
            p2.setSpacingAfter(10);
            document.add(p2);

            PdfPTable metricTable = new PdfPTable(3);
            metricTable.setWidthPercentage(100);
            metricTable.setSpacingAfter(20);

            addCell(metricTable, "项目", boldFont);
            addCell(metricTable, "结果", boldFont);
            addCell(metricTable, "参考范围", boldFont);

            // Fetch student info for age/gender context
            ExamRecordEntity record = examService.getRecord(report.getRecordId());
            StudentEntity student = studentMapper.selectById(record.getStudentId());
            Integer gender = student != null ? student.getGender() : 0;
            Integer age = null;
            if (student != null && student.getBirthday() != null) {
                age = java.time.Period.between(student.getBirthday(), LocalDate.now()).getYears();
            }

            for (ExamMetricEntity m : metrics) {
                addCell(metricTable, m.getMetricName(), textFont);
                String val = m.getValueText() != null ? m.getValueText()
                        : (m.getValueDecimal() + " " + (m.getUnit() == null ? "" : m.getUnit()));
                addCell(metricTable, val, textFont);

                // 优先使用数据库中的参考范围，如果没有则使用算法计算
                String ref = "";
                if (m.getRefLow() != null && m.getRefHigh() != null) {
                    ref = m.getRefLow() + " - " + m.getRefHigh();
                } else {
                    // 使用工具类计算参考范围 (传递性别和年龄)
                    ref = org.example.util.ReferenceRangeUtil.getReferenceRange(
                            m.getMetricKey(),
                            gender,
                            age);
                }
                addCell(metricTable, ref, textFont);
            }
            document.add(metricTable);

            // 3. Summary & Advice
            document.add(new Paragraph("综合评估：", boldFont));
            document.add(new Paragraph(info.getSummary() == null ? "暂无" : info.getSummary(), textFont));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("医师建议：", boldFont));
            document.add(new Paragraph(info.getDoctorAdvice() == null ? "暂无" : info.getDoctorAdvice(), textFont));

        } catch (Exception e) {
            throw new BizException(500, "生成PDF失败: " + e.getMessage());
        } finally {
            if (document.isOpen())
                document.close();
        }
    }

    @Transactional
    public java.util.Map<String, Integer> generateBatch(Long batchId, String operator) {
        List<ExamRecordEntity> records = examService.listByBatchId(batchId);
        int success = 0;
        int exists = 0;
        for (ExamRecordEntity record : records) {
            try {
                // Check exist first to count correctly
                ReportEntity exist = reportMapper.selectByRecordId(record.getId());
                if (exist != null) {
                    exists++;
                } else {
                    generate(record.getId(), operator);
                    success++;
                }
            } catch (Exception e) {
                // log error but continue
                e.printStackTrace();
            }
        }
        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        result.put("total", records.size());
        result.put("success", success);
        result.put("exists", exists);
        return result;
    }

    @Transactional
    public void archiveBatch(Long batchId, String operator) {
        // 1. Get all records of the batch
        List<ExamRecordEntity> records = examService.listByBatchId(batchId);
        if (records.isEmpty()) {
            // Even if no records, maybe we still want to archive the batch itself?
            // Yes, user wants to see "Archived" on the batch list.
        }

        // 2. Update status of reports
        reportMapper.archiveByBatchId(batchId, operator);

        // 3. Update status of the batch itself to 4 (Archived)
        examService.updateBatchStatus(batchId, 4, operator);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }
}
