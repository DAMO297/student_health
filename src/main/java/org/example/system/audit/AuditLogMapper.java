package org.example.system.audit;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AuditLogMapper {
        int insert(AuditLogEntity e);

        List<AuditLogEntity> selectPage(
                        @Param("userId") Long userId,
                        @Param("action") String action,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        List<AuditLogEntity> selectList(
                        @Param("username") String username,
                        @Param("action") String action,
                        @Param("resource") String resource,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        int count(
                        @Param("username") String username,
                        @Param("action") String action,
                        @Param("resource") String resource);
}
