package model;

import java.util.*;
import util.LogUtil;
import view.View;

import java.time.LocalDateTime;

/**
 * Manages all courses in the system.
 */
public class CourseManager {

    private final Map<String, Course> courseMap;
    private View view;
    private SharedContext sharedContext;


    public CourseManager(View view) {
        this.courseMap = new HashMap<>();
        this.view = view;
    }

    /**
     * Adds a new course to the system if the courseCode is unique.

     */

    public AddCourseResult addCourse(String code, String name, String description, boolean requiresComputers,
                                     String coName, String coEmail, String csName, String csEmail,
                                     int requiredTutorials, int requiredLabs,
                                     String addedByEmail) {

        String inputSummary = String.format("code=%s, name=%s, requiresComputers=%s", code, name, requiresComputers);

        if (code == null || code.trim().isEmpty()) {
            LogUtil.logAction(
                    LocalDateTime.now(), addedByEmail, "addCourse", inputSummary,
                    "FAILURE (Error: Required course info not provided)"
            );
            return new AddCourseResult(false, "Required course info not provided.");
        }

        if (!checkCourseCode(code)) {
            LogUtil.logAction(
                    LocalDateTime.now(), addedByEmail, "addCourse", inputSummary,
                    "FAILURE (Error: Provided courseCode is invalid)"
            );
            return new AddCourseResult(false, "Provided courseCode is invalid.");
        }

        if (hasCode(code)) {
            LogUtil.logAction(
                    LocalDateTime.now(), addedByEmail, "addCourse", inputSummary,
                    "FAILURE (Error: Course with that code already exists)"
            );
            return new AddCourseResult(false, "Course with that code already exists.");
        }

        Course newCourse = new Course(code, name, description, requiresComputers,
                coName, coEmail, csName, csEmail, requiredTutorials, requiredLabs);
        courseMap.put(code, newCourse);

        LogUtil.logAction(
                LocalDateTime.now(), addedByEmail, "addCourse", inputSummary,
                "SUCCESS"
        );

        return new AddCourseResult(true, "Course added successfully.");
    }





//    private boolean checkCourseCode(String code) {
//        return code != null && !code.trim().isEmpty();
//    }

    private boolean checkCourseCode(String code) {
        //Three uppercase letters followed by four digits
        return code != null && code.matches("^[A-Z]{3}[0-9]{4}$");
    }


    /**
     * Returns whether a course with the given code exists.
     */
    public boolean hasCode(String code) {
        return courseMap.containsKey(code);
    }

    /**
     * Removes a course and returns a list of emails to notify.
     * Also removes it from all student timetables.
     *
     * @return list of email addresses to notify or null if course not found.
     */

    public String[] removeCourse(String courseCode) {
        Course removed = courseMap.remove(courseCode);
        //String userEmail = SharedContext.getInstance().currentUser.getEmail(); // TODO: this is the old way - without the dependancy
        String userEmail = sharedContext.getCurrentUserEmail(); // new way

        if (removed == null) {
            LogUtil.logAction(
                    java.time.LocalDateTime.now(),
                    userEmail,
                    "removeCourse",
                    courseCode,
                    "FAILURE (Course not found)"
            );
            return null;
        }

        List<String> emailsToNotify = new ArrayList<>();
        emailsToNotify.add(removed.getCourseOrganiserEmail());

        for (Timetable timetable : sharedContext.getAllTimetables()) {
            if (timetable.hasSlotsForCourse(courseCode)) {
                emailsToNotify.add(timetable.getStudentEmail());
                timetable.removeSlotsForCourse(courseCode);
            }
        }

        LogUtil.logAction(
                java.time.LocalDateTime.now(),
                userEmail,
                "removeCourse",
                courseCode,
                "SUCCESS"
        );

        return emailsToNotify.toArray(new String[0]);
    }

    /**
     * Returns a string listing all course codes and names.
     */
    public String viewCourses() {
        if (courseMap.isEmpty()) {
            return "No courses available.";
        }

        StringBuilder sb = new StringBuilder();
        for (Course c : courseMap.values()) {
            sb.append(c.getCourseCode()).append(" - ").append(c.getName()).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * Returns a string of detailed course info.
     */
    public String viewCourse(String code) {
        Course course = courseMap.get(code);
        return course != null ? course.toString() : null;
    }

    /**
     * Adds a constructed activity (Lecture, Tutorial, or Lab) to a given course.
     *
     * @return true if successful, false if course not found.
     */
    public boolean addActivityToCourse(String courseCode, Activity activity) {
        Course course = courseMap.get(courseCode);
        if (course == null) return false;
        course.addActivity(activity);
        return true;
    }

    /**
     * Returns the Course object if found.
     */
    public Course getCourse(String code) {
        return courseMap.get(code);
    }

    /**
     * Returns all stored courses.
     */
    public Collection<Course> getAllCourses() {
        return courseMap.values();
    }

    // TODO: implent
    public CourseManager getCourseManager() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //TODO: implement
    public boolean hasCourse(String courseTag) {
        //return courseMap.containsKey(courseTag);
        throw new UnsupportedOperationException("hasCourse Not supported yet.");
    }

}
