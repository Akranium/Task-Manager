package manager;

import com.google.gson.Gson;
import enums.Status;
import task.Task;
import task.impl.OneTimeTask;
import task.impl.RecurringTask;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TaskManager {

    public final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");


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
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter("src/files/tasks.json")) {
            gson.toJson(tasks, writer);
            System.out.println("Tasks saved!");
        } catch (IOException e) {
            System.out.println("Tasks NOT saved!!!");
            e.printStackTrace();
        }
    }

    public void loadTasks() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("src/files/tasks.json")) {
            List nullableTasks = gson.fromJson(reader, List.class);
            if(nullableTasks != null) {
                tasks = nullableTasks;
            }
            System.out.println("Tasks loaded!");
        } catch (IOException e) {
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
            System.out.println("\n-------------------------------------------");
        }
    }

    public void addOneTimeTask(String name, LocalDate dueDate) {
        Task task = new OneTimeTask(name,dueDate);
        tasks.add(task);
        System.out.println("Task added successfully.");
    }

    public void addRecurringTask(String name, LocalDate dueDate, int intervalDays) {
        Task task = new RecurringTask(name,dueDate,intervalDays);
        tasks.add(task);
        System.out.println("Task added successfully.");
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
            switch(args[0]) {
                case "terminate","stop":
                    saveTasks();
                    terminate = true;
                    System.out.println("Terminating...");
                    break;
                case "addtask":
                    if (args[1].equals("r")) {
                        String taskName = args[2];
                        LocalDate date = LocalDate.parse(args[3], formatter);
                        int interval = Integer.parseInt(args[4]);
                        addRecurringTask(taskName, date, interval);
                    } else {
                        String taskName = args[1];
                        LocalDate date = LocalDate.parse(args[2], formatter);
                        addOneTimeTask(taskName, date);
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
