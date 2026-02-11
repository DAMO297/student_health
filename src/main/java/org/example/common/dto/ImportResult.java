package org.example.common.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {
    private int successCount = 0;
    private int failCount = 0;
    private List<String> errorMessages = new ArrayList<>();
}
