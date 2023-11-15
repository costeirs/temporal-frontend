package com.kinandcarta.cjug.temporalfrontend;

import com.google.common.reflect.TypeToken;
import com.kinandcarta.cjug.temporalfrontend.models.Claim;
import com.kinandcarta.cjug.temporalfrontend.models.ClaimInput;
import io.temporal.api.common.v1.Payload;
import io.temporal.api.enums.v1.EventType;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.history.v1.HistoryEvent;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.converter.DefaultDataConverter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                final List<HistoryEvent> events = workflowClient
                    .fetchHistory(wf.getExecution().getWorkflowId())
                    .getEvents();
                final List<Payload> inputs = events.get(0)
                    .getWorkflowExecutionStartedEventAttributes().getInput().getPayloadsList();

                final String id = wf.getWorkflowExecutionInfo().getExecution().getWorkflowId();
                final String name = s(inputs.get(0));
                final String description = s(inputs.get(1));
                final Payload statusRaw = wf.getWorkflowExecutionInfo().getSearchAttributes()
                    .getIndexedFieldsMap().get("status");
                final String status = statusRaw != null ? s(statusRaw) : "No status yet.";
                final WorkflowExecutionStatus statusTemporal = wf.getStatus();


                // TODO store result of estimate in memo (upsertMemo not implemented yet? https://github.com/temporalio/sdk-java/issues/623)
                final Optional<HistoryEvent> estimateEvent = events.stream()
                    .filter(
                        e -> e.getEventType() == EventType.EVENT_TYPE_ACTIVITY_TASK_COMPLETED
                            && e.hasActivityTaskCompletedEventAttributes()
                    )
                    .findFirst();
                String estimate = "No estimate yet.";
                if (estimateEvent.isPresent()) {
                    @SuppressWarnings("unchecked")
                    final Map<String, String> e = dataConverter.fromPayload(
                        estimateEvent.get().getActivityTaskCompletedEventAttributes().getResult()
                            .getPayloads(0), HashMap.class, new TypeToken<HashMap<String, String>>() {}.getType());
                    estimate = e.toString();
                }

                return new Claim(id, name, description, estimate, status, statusTemporal);
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
