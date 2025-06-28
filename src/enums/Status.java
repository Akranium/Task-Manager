package enums;

public enum Status {
    OVERDUE("overdue"),
    DUE_TOMORROW("due tomorrow");

    public String statusMessage;

    Status(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
