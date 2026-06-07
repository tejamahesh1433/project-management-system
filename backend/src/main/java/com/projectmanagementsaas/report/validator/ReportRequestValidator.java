package com.projectmanagementsaas.report.validator;

import com.projectmanagementsaas.common.exception.BadRequestException;
import com.projectmanagementsaas.report.dto.GenerateReportRequest;
import org.springframework.stereotype.Component;

@Component
public class ReportRequestValidator {
    public void validate(GenerateReportRequest request) {
        switch (request.type()) {
            case PROJECT, TEAM -> {
                if (request.projectId() == null) {
                    throw new BadRequestException(request.type() + " reports require projectId");
                }
            }
            case SPRINT -> {
                if (request.sprintId() == null) {
                    throw new BadRequestException("SPRINT reports require sprintId");
                }
            }
            case WORKSPACE -> {
                if (request.workspaceId() == null) {
                    throw new BadRequestException("WORKSPACE reports require workspaceId");
                }
            }
        }
    }
}
