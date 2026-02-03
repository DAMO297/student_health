package org.example.system.dict;

import org.example.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DictionaryService {

    private final DictionaryMapper dictionaryMapper;

    public DictionaryService(DictionaryMapper dictionaryMapper) {
        this.dictionaryMapper = dictionaryMapper;
    }

    public List<DictionaryEntity> list(String typeCode, Integer status) {
        return dictionaryMapper.selectList(typeCode, status);
    }

    @Transactional
    public DictionaryEntity create(DictionaryEntity req, String operator) {
        req.setStatus(req.getStatus() == null ? 0 : req.getStatus());
        req.setCreatedBy(operator);
        req.setUpdatedBy(operator);
        dictionaryMapper.insert(req);
        return req;
    }

    @Transactional
    public DictionaryEntity update(Long id, DictionaryEntity req, String operator) {
        DictionaryEntity exist = dictionaryMapper.selectById(id);
        if (exist == null)
            throw BizException.notFound("字典不存在");

        exist.setTypeCode(req.getTypeCode());
        exist.setLabel(req.getLabel());
        exist.setValue(req.getValue());
        exist.setSort(req.getSort());
        exist.setStatus(req.getStatus());
        exist.setRemark(req.getRemark());
        exist.setUpdatedBy(operator);

        dictionaryMapper.update(exist);
        return exist;
    }

    @Transactional
    public void delete(Long id) {
        dictionaryMapper.deleteById(id);
    }
}
