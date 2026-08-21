package com.chenwei666.netserial.change;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ChangeTask {
    private final String id;
    private final String ticketNumber;
    private final String site;
    private final String deviceName;
    private final String operatorName;
    private final String goal;
    private final String precheckPlan;
    private final String commandPlan;
    private final String verificationPlan;
    private final String rollbackPlan;
    private final long windowStartMillis;
    private final long windowEndMillis;
    private final ChangeTaskStatus status;
    private final List<ChangeEvent> events;

    public ChangeTask(String id, String ticketNumber, String site, String deviceName,
                      String operatorName, String goal, String precheckPlan, String commandPlan,
                      String verificationPlan, String rollbackPlan, long windowStartMillis,
                      long windowEndMillis, ChangeTaskStatus status, List<ChangeEvent> events) {
        this.id = required(id, 64, "id");
        this.ticketNumber = required(ticketNumber, 128, "ticketNumber");
        this.site = optional(site, 256, "site");
        this.deviceName = required(deviceName, 128, "deviceName");
        this.operatorName = required(operatorName, 128, "operatorName");
        this.goal = required(goal, 2_000, "goal");
        this.precheckPlan = optional(precheckPlan, 8_000, "precheckPlan");
        this.commandPlan = optional(commandPlan, 12_000, "commandPlan");
        this.verificationPlan = optional(verificationPlan, 8_000, "verificationPlan");
        this.rollbackPlan = required(rollbackPlan, 8_000, "rollbackPlan");
        if (windowStartMillis <= 0 || windowEndMillis <= windowStartMillis) {
            throw new IllegalArgumentException("invalid maintenance window");
        }
        this.windowStartMillis = windowStartMillis;
        this.windowEndMillis = windowEndMillis;
        this.status = Objects.requireNonNull(status, "status");
        List<ChangeEvent> copy = new ArrayList<>(Objects.requireNonNull(events, "events"));
        if (copy.size() > 500) throw new IllegalArgumentException("too many change events");
        this.events = Collections.unmodifiableList(copy);
    }

    public String getId() { return id; }
    public String getTicketNumber() { return ticketNumber; }
    public String getSite() { return site; }
    public String getDeviceName() { return deviceName; }
    public String getOperatorName() { return operatorName; }
    public String getGoal() { return goal; }
    public String getPrecheckPlan() { return precheckPlan; }
    public String getCommandPlan() { return commandPlan; }
    public String getVerificationPlan() { return verificationPlan; }
    public String getRollbackPlan() { return rollbackPlan; }
    public long getWindowStartMillis() { return windowStartMillis; }
    public long getWindowEndMillis() { return windowEndMillis; }
    public ChangeTaskStatus getStatus() { return status; }
    public List<ChangeEvent> getEvents() { return events; }

    public boolean isAuthorizedAt(long nowMillis, String targetDevice) {
        return status == ChangeTaskStatus.ACTIVE
                && nowMillis >= windowStartMillis
                && nowMillis <= windowEndMillis
                && deviceName.equalsIgnoreCase(Objects.requireNonNull(targetDevice, "targetDevice").trim());
    }

    public ChangeTask start(long nowMillis) {
        if (status != ChangeTaskStatus.DRAFT) throw new IllegalStateException("only a draft can start");
        return withStatusAndEvent(ChangeTaskStatus.ACTIVE,
                new ChangeEvent(nowMillis, ChangeEventType.TASK_STARTED, deviceName, ticketNumber));
    }

    public ChangeTask append(ChangeEvent event) {
        if (status != ChangeTaskStatus.ACTIVE) throw new IllegalStateException("task is not active");
        List<ChangeEvent> updated = new ArrayList<>(events);
        if (updated.size() >= 500) updated.remove(0);
        updated.add(Objects.requireNonNull(event, "event"));
        return copy(status, updated);
    }

    public ChangeTask complete(long nowMillis) {
        if (status != ChangeTaskStatus.ACTIVE) throw new IllegalStateException("task is not active");
        return withStatusAndEvent(ChangeTaskStatus.COMPLETED,
                new ChangeEvent(nowMillis, ChangeEventType.TASK_COMPLETED, deviceName, ticketNumber));
    }

    public ChangeTask cancel(long nowMillis) {
        if (status == ChangeTaskStatus.COMPLETED || status == ChangeTaskStatus.CANCELLED) {
            throw new IllegalStateException("task is already closed");
        }
        return withStatusAndEvent(ChangeTaskStatus.CANCELLED,
                new ChangeEvent(nowMillis, ChangeEventType.TASK_CANCELLED, deviceName, ticketNumber));
    }

    private ChangeTask withStatusAndEvent(ChangeTaskStatus next, ChangeEvent event) {
        List<ChangeEvent> updated = new ArrayList<>(events);
        updated.add(event);
        return copy(next, updated);
    }

    private ChangeTask copy(ChangeTaskStatus next, List<ChangeEvent> updated) {
        return new ChangeTask(id, ticketNumber, site, deviceName, operatorName, goal,
                precheckPlan, commandPlan, verificationPlan, rollbackPlan,
                windowStartMillis, windowEndMillis, next, updated);
    }

    private static String required(String value, int maximum, String field) {
        String normalized = optional(value, maximum, field);
        if (normalized.isEmpty()) throw new IllegalArgumentException(field + " is required");
        return normalized;
    }

    private static String optional(String value, int maximum, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.length() > maximum) throw new IllegalArgumentException(field + " is too long");
        return normalized;
    }
}
