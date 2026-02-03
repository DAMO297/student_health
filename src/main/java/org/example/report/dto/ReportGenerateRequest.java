package org.example.report.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReportGenerateRequest {
    @NotNull
    private Long recordId;
}





