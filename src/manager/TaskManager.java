package manager;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import enums.Status;
import enums.TaskType;
import manager.gsonshit.GsonFactory;
import task.Task;
import task.impl.OneTimeTask;
import task.impl.RecurringTask;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.StreamSupport;

public class TaskManager {

    public final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public final String FILE_PATH = "src/files/tasks.json";


    List<Task> tasks = new ArrayList<>();
    public final TaskManager taskManager = this;

    public void start() {
        System.out.println("Starting the program...");
        System.out.println("Loading the files...");
        taskManager.loadTasks();
        System.out.println("Checking notifications...");
        taskManager.checkNotifications();
        System.out.println("Listening to commands...");
        taskManager.listenCommands();
    }

    public static void printNotification(Task task, Status status) {
        System.out.println("The task " + task.getName() + " is " + status.statusMessage + ".");
    }

    public void saveTasks() {
        Gson gson = GsonFactory.create();
        try (FileWriter writer = new FileWriter("src/files/tasks.json")) {
            gson.toJson(tasks, writer);
            System.out.println("Tasks saved!");
        } catch (IOException e) {
            System.out.println("Tasks NOT saved!!!");
            e.printStackTrace();
        }
    }

    public void loadTasks() {
        Gson gson = GsonFactory.create();
        try (FileReader reader = new FileReader(FILE_PATH)) {
            java.lang.reflect.Type taskListType = new TypeToken<List<Task>>() {}.getType();
            List<Task> nullableTasks = gson.fromJson(reader, taskListType);
            if(nullableTasks != null) {
                tasks = nullableTasks;
            }
            System.out.println("Tasks loaded!");
        } catch (IOException |JsonParseException e) {
            System.out.println("Tasks NOT loaded!!!");
            e.printStackTrace();
        }
    }

    public void listTasks() {
        if(tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        for(Task task : tasks) {
            System.out.println("Task: " + task.getName());
            System.out.println("Type: " + task.getTaskType());
            System.out.println("Due date: " + task.getDueDate());
            System.out.println("------------------------");
        }
    }

    public void addTask(TaskType type, String name, LocalDate dueDate, int intervalDays) {
        for(Task task : tasks) {
            String nameExisting = task.getName();
            if(name.equals(nameExisting)) {
                System.out.println("A task with that name already exists.");
                return;
            }
        }
        if(type == TaskType.ONE_TIME) {
            Task task = new OneTimeTask(name,dueDate);
            tasks.add(task);
            System.out.println("Task added successfully.");
        } else if (type == TaskType.RECURRING) {
            Task task = new RecurringTask(name,dueDate,intervalDays);
            tasks.add(task);
            System.out.println("Task added successfully.");
        } else {
            System.out.println("Unknown error.");
        }
    }

    public void deleteTask(String name) {
        for(Task task : tasks) {
            if(task.getName().equals(name)) {
                tasks.remove(task);
                return;
            }
        }
        System.out.println("A task with that name is not found.");
    }

    public void checkNotifications() {
        for(Task task : tasks) {
            task.checkOverdue();
        }
        for(Task task : tasks) {
            task.checkDueTomorrow();
        }
    }

    public void listenCommands() {
        Scanner scanner = new Scanner(System.in);
        boolean terminate = false;
        while(!terminate) {
            String[] args = scanner.nextLine().toLowerCase().split(" ");

            //Checks for enough arguements.
            if (args.length < 3 || (!args[1].equals("r") && args.length < 5)) {
                System.out.println("Usage: addtask <name> <dd-MM-yyyy> OR addtask r <name> <dd-MM-yyyy> <interval>");
                break;
            }

            switch(args[0]) {
                case "terminate","stop":
                    saveTasks();
                    terminate = true;
                    System.out.println("Terminating...");
                    break;
                case "addtask":
                    if (args[1].equals("r")) {
                        String taskName = args[2];
                        try {
                            LocalDate date = LocalDate.parse(args[3], FORMATTER);
                            int interval = Integer.parseInt(args[4]);
                            addTask(TaskType.ONE_TIME, taskName, date, interval);
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date format! Please use dd-MM-yyyy.");
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid interval! Please enter a valid number.");
                        }
                    } else {
                        String taskName = args[1];
                        try {
                            LocalDate date = LocalDate.parse(args[2], FORMATTER);
                            addTask(TaskType.ONE_TIME,taskName, date, 0);
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date format! Please use dd-MM-yyyy (e.g. 30-06-2025).");
                        }
                    }
                    break;
                case "deletetask":
                    try {
                        deleteTask(args[1]);
                    } catch(ArrayIndexOutOfBoundsException e) {
                        System.out.println("Please provide a task to delete.");
                    }
                    break;
                case "list":
                    listTasks();
                case "help":
                    break;
                default: System.out.println("Unknown command. Type 'help' to see a list of commands.");
            }
        }
    }
}
