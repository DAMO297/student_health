package org.example.student.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class StudentImportResult {
    private int successCount;
    private int failCount;
    private List<String> errorMessages = new ArrayList<>();
}
