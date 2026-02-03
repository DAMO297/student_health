package org.example.report.dto;

import lombok.Data;

public class ReportUpdateRequest {
    @Data
    public static class Advice {
        private String doctorAdvice;
        private String summary;
    }
}





