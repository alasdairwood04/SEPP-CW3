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


    public CourseManager(View view, SharedContext sharedContext) {
        this.courseMap = new HashMap<>();
        this.view = view;
        this.sharedContext = sharedContext;
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

    /**
     * Validates course code format: three uppercase letters followed by four digits.
     */
    private boolean checkCourseCode(String code) {
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
        String userEmail = sharedContext.getCurrentUserEmail();

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

    /**
     * Returns the CourseManager instance.
     */
    public CourseManager getCourseManager() {
        return this; // Return this instance to prevent null pointer exceptions
    }

    /**
     * Checks if a course with the given tag exists.
     */
    public boolean hasCourse(String courseTag) {
        return courseMap.containsKey(courseTag);
    }

    /**
     * Adds a course to a student's timetable.
     *
     * @param studentEmail The email of the student
     * @param courseCode The code of the course to add
     * @return true if the course was added successfully, false otherwise
     */
    public boolean addCourseToStudentTimetable(String studentEmail, String courseCode) {
        if (!isValidCourseCode(courseCode, studentEmail)) {
            view.displayError("Invalid course code.");
            LogUtil.logAction(LocalDateTime.now(), studentEmail, "addCourseToStudentTimetable", courseCode,
                    "FAILURE (Invalid input)");
            return false;
        }
        if (!courseExists(courseCode, studentEmail)) {
            view.displayError("Course " + courseCode + " does not exist.");
            LogUtil.logAction(LocalDateTime.now(), studentEmail, "addCourseToStudentTimetable", courseCode,
                    "FAILURE (Course does not exist)");
            return false;
        }

        Course course = getCourse(courseCode);
        Timetable timetable = sharedContext.getOrCreateTimetable(studentEmail);

        if (!processActivities(course, timetable, studentEmail)) {
            return false;
        }

        postAdditionCheck(course, timetable, studentEmail);

        view.displaySuccess("Course " + courseCode + " added to your timetable.");
        LogUtil.logAction(LocalDateTime.now(), studentEmail, "addCourseToStudentTimetable",
                courseCode, "SUCCESS");
        return true;
    }

    private boolean isValidCourseCode(String courseCode, String studentEmail) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            view.displayError("Invalid course code.");
            LogUtil.logAction(LocalDateTime.now(), studentEmail, "addCourseToStudentTimetable", courseCode,
                    "FAILURE (Invalid input)");
            return false;
        }
        return true;
    }

    private boolean courseExists(String courseCode, String studentEmail) {
        if (!hasCode(courseCode)) {
            view.displayError("Course " + courseCode + " does not exist.");
            LogUtil.logAction(LocalDateTime.now(), studentEmail, "addCourseToStudentTimetable", courseCode,
                    "FAILURE (Course does not exist)");
            return false;
        }
        return true;
    }

    private boolean processActivities(Course course, Timetable timetable, String studentEmail) {
        for (Activity activity : course.getActivities()) {
            String[] conflict = timetable.checkConflicts(
                    activity.getStartDate(), activity.getStartTime(),
                    activity.getEndDate(), activity.getEndTime()
            );

            if (conflict != null) {
                if (course.isUnrecordedLecture(activity.getId())) {
                    view.displayError("Cannot add course due to conflict with unrecorded lecture from course "
                            + conflict[0] + " (Activity ID: " + conflict[1] + ").");
                    LogUtil.logAction(LocalDateTime.now(), studentEmail, "addCourseToStudentTimetable",
                            course.getCourseCode(), "FAILURE (Unrecorded lecture conflict)");
                    return false;
                } else {
                    view.displayWarning("Warning: Conflict detected with course "
                            + conflict[0] + " (Activity ID: " + conflict[1] + "). Adding course anyway.");
                    LogUtil.logAction(LocalDateTime.now(), studentEmail, "addCourseToStudentTimetable",
                            course.getCourseCode(), "WARNING (Conflict with another activity)");
                }
            }

            TimeSlotStatus status = "Lecture".equals(activity.getType()) ?
                    TimeSlotStatus.CHOSEN : TimeSlotStatus.UNCHOSEN;
            timetable.addTimeSlot(
                    activity.getDay(),
                    activity.getStartDate(),
                    activity.getStartTime(),
                    activity.getEndDate(),
                    activity.getEndTime(),
                    course.getCourseCode(),
                    activity.getId(),
                    status
            );
        }
        return true;
    }

    private void postAdditionCheck(Course course, Timetable timetable, String studentEmail) {
        int chosenCount = timetable.numChosenActivities(course.getCourseCode());
        int requiredSelections = course.getRequiredTutorials() + course.getRequiredLabs();
        if (chosenCount < requiredSelections) {
            view.displayWarning("You have not yet chosen all required tutorials/labs. ("
                    + chosenCount + "/" + requiredSelections + ")");
            LogUtil.logAction(LocalDateTime.now(), studentEmail, "addCourseToStudentTimetable",
                    course.getCourseCode(), "WARNING (Incomplete lab/tutorial selection)");
        }
    }

    /**
     * Marks a specific activity (tutorial/lab) as chosen for a course already added to a student's timetable.
     *
     * @param studentEmail the email of the student
     * @param courseCode   the code of the course
     * @param activityId   the identifier of the activity to choose
     * @return true if the activity was marked as chosen successfully, false otherwise
     */
    public boolean chooseActivityForCourse(String studentEmail, String courseCode, int activityId) {
        // Validate that the course exists.
        if (!hasCode(courseCode)) {
            view.displayError("Course " + courseCode + " does not exist.");
            LogUtil.logAction(LocalDateTime.now(), studentEmail, "chooseActivityForCourse", courseCode,
                    "FAILURE (Course does not exist)");
            return false;
        }

        // Retrieve the student's timetable (create if it doesn't exist)
        Timetable timetable = sharedContext.getOrCreateTimetable(studentEmail);

        // Ensure that the timetable has at least one timeslot for the specified course.
        if (!timetable.hasSlotsForCourse(courseCode)) {
            view.displayError("Course " + courseCode + " is not in your timetable.");
            LogUtil.logAction(LocalDateTime.now(), studentEmail, "chooseActivityForCourse", courseCode,
                    "FAILURE (Course not in timetable)");
            return false;
        }

        // Attempt to mark the activity as chosen.
        boolean chosen = timetable.chooseActivity(courseCode, activityId);
        if (!chosen) {
            view.displayError("Activity with ID " + activityId + " for course " + courseCode + " not found in your timetable.");
            LogUtil.logAction(LocalDateTime.now(), studentEmail, "chooseActivityForCourse", courseCode,
                    "FAILURE (Activity not found in timetable)");
            return false;
        }

        // Log success and notify the user.
        view.displaySuccess("Activity " + activityId + " for course " + courseCode + " has been chosen successfully.");
        LogUtil.logAction(LocalDateTime.now(), studentEmail, "chooseActivityForCourse", courseCode,
                "SUCCESS");
        return true;
    }
}