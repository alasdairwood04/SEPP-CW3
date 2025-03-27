package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a university course, including metadata and a list of activities.
 */
public class Course {
    private String courseCode;
    private String name;
    private String description;
    private boolean requiresComputers;
    private String courseOrganiserName;
    private String courseOrganiserEmail;
    private String courseSecretaryName;
    private String courseSecretaryEmail;
    private int requiredTutorials;
    private int requiredLabs;

    private List<Activity> activities;

    /**
     * Constructs a Course with all required information.
     */
    public Course(String courseCode, String name, String description, boolean requiresComputers,
                  String courseOrganiserName, String courseOrganiserEmail,
                  String courseSecretaryName, String courseSecretaryEmail,
                  int requiredTutorials, int requiredLabs) {
        this.courseCode = courseCode;
        this.name = name;
        this.description = description;
        this.requiresComputers = requiresComputers;
        this.courseOrganiserName = courseOrganiserName;
        this.courseOrganiserEmail = courseOrganiserEmail;
        this.courseSecretaryName = courseSecretaryName;
        this.courseSecretaryEmail = courseSecretaryEmail;
        this.requiredTutorials = requiredTutorials;
        this.requiredLabs = requiredLabs;
        this.activities = new ArrayList<>();
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresComputers() {
        return requiresComputers;
    }

    public String getCourseOrganiserName() {
        return courseOrganiserName;
    }

    public String getCourseOrganiserEmail() {
        return courseOrganiserEmail;
    }

    public String getCourseSecretaryName() {
        return courseSecretaryName;
    }

    public String getCourseSecretaryEmail() {
        return courseSecretaryEmail;
    }

    public int getRequiredTutorials() {
        return requiredTutorials;
    }

    public int getRequiredLabs() {
        return requiredLabs;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void addActivity(Activity activity) {
        this.activities.add(activity);
    }

    public void removeAllActivities() {
        this.activities.clear();
    }

    public boolean hasCode(String code) {
        return this.courseCode.equals(code);
    }

    public boolean hasActivityWithId(int id) {
        return activities.stream().anyMatch(a -> a.hasId(id));
    }

    public boolean isUnrecordedLecture(int activityId) {
        return activities.stream()
                .filter(a -> a instanceof Lecture && a.hasId(activityId))
                .anyMatch(a -> !((Lecture) a).isRecorded());
    }

    public String getActivitiesAsString() {
        if (activities.isEmpty()) return "No activities assigned.";
        StringBuilder sb = new StringBuilder();
        for (Activity a : activities) {
            sb.append(a.toString()).append("\n");
        }
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return courseCode + " - " + name + "\n" +
                "Organiser: " + courseOrganiserName + " <" + courseOrganiserEmail + ">\n" +
                "Secretary: " + courseSecretaryName + " <" + courseSecretaryEmail + ">\n" +
                "Requires Computers: " + requiresComputers + "\n" +
                "Required Tutorials: " + requiredTutorials + ", Labs: " + requiredLabs + "\n" +
                "Description: " + description + "\n" +
                "Activities:\n" + getActivitiesAsString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        Course course = (Course) o;
        return Objects.equals(courseCode, course.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode);
    }
}
