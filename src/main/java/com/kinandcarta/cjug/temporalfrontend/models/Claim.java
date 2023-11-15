package com.kinandcarta.cjug.temporalfrontend.models;

import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import lombok.Data;

@Data
public class Claim {

    private final String id;
    private final String name;
    private final String description;
    private final String status;
    private final WorkflowExecutionStatus workflowExecutionStatus;

    public boolean isPendingHumanReview() {
        return workflowExecutionStatus == WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING
            && this.status.equals("PENDING_HUMAN_REVIEW");
    }

    public String workflowStatusText() {
        return this.workflowExecutionStatus.name().replace("WORKFLOW_EXECUTION_STATUS_", "");
    }
}
