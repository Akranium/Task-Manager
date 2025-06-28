package task.impl;

import enums.TaskType;
import manager.TaskManager;
import enums.Status;
import task.Task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class OneTimeTask implements Task {
    private String name;
    private LocalDate dueDate;
    private boolean isComplete;

    public OneTimeTask(String name, LocalDate dueDate) {
        this.name = name;
        this.dueDate = dueDate;
        this.isComplete = false;
    }

    @Override
    public void completeTask() {
        isComplete = true;
    }

    @Override
    public void checkOverdue() {
        LocalDate now = LocalDate.now();
        if(now.isAfter(dueDate) && !isComplete) {
            TaskManager.printNotification(this, Status.OVERDUE);
        }
    }

    @Override
    public void checkDueTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if(tomorrow.isAfter(dueDate) && !isComplete) {
            TaskManager.printNotification(this, Status.DUE_TOMORROW);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.ONE_TIME;
    }

    @Override
    public String getDueDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return dueDate.format(formatter);
    }


}
