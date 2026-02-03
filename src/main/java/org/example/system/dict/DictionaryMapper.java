package org.example.system.dict;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DictionaryMapper {
    List<DictionaryEntity> selectList(@Param("typeCode") String typeCode, @Param("status") Integer status);

    DictionaryEntity selectById(Long id);

    int insert(DictionaryEntity e);

    int update(DictionaryEntity e);

    int deleteById(Long id);
}
