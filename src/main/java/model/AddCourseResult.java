package model;

public class AddCourseResult {
    public final boolean success;
    public final String message;

    public AddCourseResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
