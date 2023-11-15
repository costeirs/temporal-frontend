package com.kinandcarta.cjug.temporalfrontend;

import com.kinandcarta.cjug.temporalfrontend.models.Claim;
import com.kinandcarta.cjug.temporalfrontend.models.ClaimInput;
import io.temporal.api.common.v1.Payload;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
// todo combine projects to share types
public class ClaimsService {

    private final WorkflowClient workflowClient;
    private final DataConverter dataConverter = DefaultDataConverter.newDefaultInstance();

    public List<Claim> getAll() {
        return workflowClient.listExecutions("")
            .map(wf -> {
                List<Payload> inputs = workflowClient
                    .fetchHistory(wf.getExecution().getWorkflowId())
                    .getHistory().getEvents(0)
                    .getWorkflowExecutionStartedEventAttributes().getInput().getPayloadsList();

                String id = wf.getWorkflowExecutionInfo().getExecution().getWorkflowId();
                String name = s(inputs.get(0));
                String description = s(inputs.get(1));
                Payload statusRaw = wf.getWorkflowExecutionInfo().getSearchAttributes().getIndexedFieldsMap().get("status");
                String status = statusRaw != null ? s(statusRaw) : "No status yet.";
                WorkflowExecutionStatus statusTemporal = wf.getStatus();

                return new Claim(id, name, description, status, statusTemporal);
            })
            .toList();
    }

    public void create(ClaimInput input) {
        log.info("Sending to Temporal {}", input);
        workflowClient.newUntypedWorkflowStub(
                "ClaimsWorkflow",
                WorkflowOptions.newBuilder()
                    .setTaskQueue("autoclaims-tasks")
                    .validateBuildWithDefaults())
            .start(input.getName(), input.getDescription());
    }

    public void review(String id, String status) {
        log.info("id {} status {}", id, status);
        workflowClient.newUntypedWorkflowStub(id).signal("review", status);
    }

    private String s(Payload p) {
        return dataConverter.fromPayload(p, String.class, String.class);
    }
}
