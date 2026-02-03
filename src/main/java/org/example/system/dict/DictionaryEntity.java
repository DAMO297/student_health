package org.example.system.dict;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DictionaryEntity {
    private Long id;
    private String typeCode; // 字典类型编码 (e.g., 'college', 'gender')
    private String label; // 显示名
    private String value; // 存储值
    private Integer sort; // 排序
    private Integer status; // 0 normal, 1 disabled
    private String remark;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
