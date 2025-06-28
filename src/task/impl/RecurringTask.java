package task.impl;

import enums.TaskType;
import manager.TaskManager;
import enums.Status;
import task.Task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RecurringTask implements Task {
    String name;
    LocalDate dueDate;
    int intervalDays;

    public RecurringTask(String name, LocalDate dueDate, int intervalDays) {
        this.name = name;
        this.dueDate = dueDate;
        this.intervalDays = intervalDays;
    }

    @Override
    public void completeTask() {

    }

    @Override
    public void checkOverdue() {
        LocalDate now = LocalDate.now();
        if(now.isEqual(dueDate)) {
            TaskManager.printNotification(this, Status.OVERDUE);
        }
    }

    @Override
    public void checkDueTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if(tomorrow.isEqual(dueDate)) {
            TaskManager.printNotification(this,Status.DUE_TOMORROW);
        }
    }

    public void updateNextDueDate() {
        this.dueDate = dueDate.plusDays(intervalDays);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.RECURRING;
    }

    @Override
    public String getDueDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return dueDate.format(formatter);
    }
}
