package task;

import enums.TaskType;

public interface Task {

    void completeTask();
    void checkOverdue();
    void checkDueTomorrow();
    String getName();
    TaskType getTaskType();
    String getDueDate();
}
