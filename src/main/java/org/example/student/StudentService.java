package org.example.student;

import org.example.common.BizException;
import org.example.common.PageResult;
import org.example.student.dto.StudentSaveRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.io.InputStream;
import com.alibaba.excel.EasyExcel;
import org.example.student.dto.StudentExcelModel;
import org.example.student.dto.StudentImportResult;

@Service
public class StudentService {

    private final StudentMapper studentMapper;

    public StudentService(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    public StudentEntity get(Long id) {
        StudentEntity e = studentMapper.selectById(id);
        if (e == null)
            throw BizException.notFound("学生不存在");
        return e;
    }

    public PageResult<StudentEntity> page(String keyword, String studentNo, String name, String college, String grade,
            String clazz,
            Integer status, int page, int pageSize) {
        int offset = Math.max(0, page - 1) * pageSize;
        long total = studentMapper.count(keyword, studentNo, name, college, grade, clazz, status);
        return new PageResult<>(total,
                studentMapper.selectPage(keyword, studentNo, name, college, grade, clazz, status, offset, pageSize));
    }

    @Transactional
    public StudentEntity create(StudentSaveRequest req, String operator) {
        StudentEntity exist = studentMapper.selectByStudentNo(req.getStudentNo());
        if (exist != null)
            throw BizException.conflict("学号已存在");

        StudentEntity e = new StudentEntity();
        e.setStudentNo(req.getStudentNo());
        e.setName(req.getName());
        e.setGender(req.getGender());
        e.setCollege(req.getCollege());
        e.setGrade(req.getGrade());
        e.setClazz(req.getClazz());
        e.setIdCard(req.getIdCard());
        e.setPhone(req.getPhone());
        e.setEmail(req.getEmail());
        if (req.getBirthday() != null && !req.getBirthday().trim().isEmpty()) {
            e.setBirthday(LocalDate.parse(req.getBirthday().trim()));
        }
        e.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        e.setVersion(0);
        e.setDeleted(0);
        e.setCreatedBy(operator);
        e.setUpdatedBy(operator);

        studentMapper.insert(e);
        return e;
    }

    @Transactional
    public StudentEntity update(Long id, StudentSaveRequest req, String operator) {
        StudentEntity e = get(id);

        // 学号不允许修改（保持业务唯一性）；如需要支持改学号，应单独接口并做级联影响评估
        e.setName(req.getName());
        e.setGender(req.getGender());
        e.setCollege(req.getCollege());
        e.setGrade(req.getGrade());
        e.setClazz(req.getClazz());
        e.setIdCard(req.getIdCard());
        e.setPhone(req.getPhone());
        e.setEmail(req.getEmail());
        if (req.getBirthday() != null && !req.getBirthday().trim().isEmpty()) {
            e.setBirthday(LocalDate.parse(req.getBirthday().trim()));
        } else {
            e.setBirthday(null);
        }
        e.setStatus(req.getStatus() == null ? e.getStatus() : req.getStatus());
        e.setUpdatedBy(operator);

        studentMapper.update(e);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        int n = studentMapper.softDelete(id);
        if (n == 0)
            throw BizException.notFound("学生不存在或已删除");
    }

    public List<StudentExcelModel> exportStudents(String keyword, String studentNo, String name, String college,
            String grade,
            String clazz, Integer status) {
        List<StudentEntity> list = studentMapper.selectList(keyword, studentNo, name, college, grade, clazz, status);
        return list.stream().map(e -> {
            StudentExcelModel m = new StudentExcelModel();
            m.setStudentNo(e.getStudentNo());
            m.setName(e.getName());
            m.setGender(e.getGender() == 1 ? "男" : (e.getGender() == 2 ? "女" : "未知"));
            m.setCollege(e.getCollege());
            m.setGrade(e.getGrade());
            m.setClazz(e.getClazz());
            m.setPhone(e.getPhone());
            m.setEmail(e.getEmail());
            return m;
        }).collect(Collectors.toList());
    }

    @Transactional
    public StudentImportResult importStudents(InputStream is, String operator) {
        StudentImportResult result = new StudentImportResult();
        List<StudentExcelModel> list;
        try {
            list = EasyExcel.read(is).head(StudentExcelModel.class).sheet().doReadSync();
        } catch (Exception e) {
            throw new BizException(400, "读取Excel文件失败: " + e.getMessage());
        }

        for (int i = 0; i < list.size(); i++) {
            StudentExcelModel row = list.get(i);
            int lineNum = i + 2; // Excel row index + 2 (header + 0-index)
            try {
                processImportRow(row, operator);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.setFailCount(result.getFailCount() + 1);
                result.getErrorMessages().add("第" + lineNum + "行导入失败: " + e.getMessage());
            }
        }
        return result;
    }

    private void processImportRow(StudentExcelModel row, String operator) {
        if (row.getStudentNo() == null || row.getStudentNo().isEmpty())
            throw new RuntimeException("学号为空");
        if (row.getName() == null || row.getName().isEmpty())
            throw new RuntimeException("姓名为空");

        StudentEntity exist = studentMapper.selectByStudentNo(row.getStudentNo());

        StudentEntity e = exist != null ? exist : new StudentEntity();
        e.setStudentNo(row.getStudentNo());
        e.setName(row.getName());
        e.setCollege(row.getCollege() == null ? "" : row.getCollege());
        e.setGrade(row.getGrade() == null ? "" : row.getGrade());
        e.setClazz(row.getClazz() == null ? "" : row.getClazz());

        int gender = 0;
        if ("男".equals(row.getGender()))
            gender = 1;
        else if ("女".equals(row.getGender()))
            gender = 2;
        e.setGender(gender);

        e.setPhone(row.getPhone());
        e.setEmail(row.getEmail());

        // Default status
        if (e.getStatus() == null)
            e.setStatus(1);
        if (e.getDeleted() == null)
            e.setDeleted(0);

        e.setUpdatedBy(operator);
        e.setVersion(exist == null ? 0 : e.getVersion() + 1);

        if (exist == null) {
            e.setCreatedBy(operator);
            studentMapper.insert(e);
        } else {
            studentMapper.update(e);
        }
    }
}
