package controller;

import external.AuthenticationService;
import external.EmailService;
import model.AuthenticatedUser;
import model.SharedContext;
import model.Timetable;
import util.LogUtil;
import view.View;

import java.time.LocalDateTime;

public class StudentController extends Controller {

    public StudentController(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);
    }

    /**
     * Displays a simple menu for timetable management, including adding a course,
     * viewing the timetable, and choosing a tutorial/lab for a course.
     */
    public void manageTimetable() {
        boolean exit = false;
        while (!exit) {
            view.displayDivider();
            view.displayInfo("Student Timetable Management:");
            view.displayInfo("[1] Add course to timetable");
            view.displayInfo("[2] View timetable");
            view.displayInfo("[3] Choose tutorial/lab for course");
            view.displayInfo("[0] Exit");

            int choice = view.getIntegerInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    addCourseToTimetable();
                    break;
                case 2:
                    viewTimetable();
                    break;
                case 3:
                    chooseActivityForCourse();
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    view.displayError("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Prompts the student for a course code and adds the course to the student's timetable.
     */
    public void addCourseToTimetable() {
        String courseCode = view.getInput("Enter the course code to add to your timetable: ");
        String studentEmail = ((AuthenticatedUser) sharedContext.currentUser).getEmail();
        boolean success = sharedContext.getCourseManager().addCourseToStudentTimetable(studentEmail, courseCode);
        if (!success) {
            view.displayError("Failed to add course " + courseCode + " to your timetable.");
        }
    }

    /**
     * Removes a course from the student's timetable.
     * The student is prompted to provide the course code, and if the course exists in their timetable,
     * all time slots for that course are removed.
     */
    public void removeCourseFromTimetable() {
        String courseCode = view.getInput("Enter the course code to remove from your timetable: ");
        String studentEmail = ((AuthenticatedUser) sharedContext.currentUser).getEmail();

        Timetable timetable = sharedContext.getOrCreateTimetable(studentEmail);

        // Check if the student's timetable contains the course.
        if (!timetable.hasSlotsForCourse(courseCode)) {
            view.displayError("Course " + courseCode + " is not in your timetable.");
            LogUtil.logAction(LocalDateTime.now(), studentEmail, "removeCourseFromTimetable", courseCode,
                    "FAILURE (Course not in timetable)");
            return;
        }

        // Remove all time slots for the course.
        timetable.removeSlotsForCourse(courseCode);

        view.displaySuccess("Course " + courseCode + " removed from your timetable.");
        LogUtil.logAction(LocalDateTime.now(), studentEmail, "removeCourseFromTimetable", courseCode,
                "SUCCESS");
    }


    /**
     * Retrieves and displays the student's timetable for the working week.
     */
    public void viewTimetable() {
        String studentEmail = ((AuthenticatedUser) sharedContext.currentUser).getEmail();
        Timetable timetable = sharedContext.getOrCreateTimetable(studentEmail);
        String workingWeekTimetable = timetable.toWorkingWeekString();
        view.displayInfo(workingWeekTimetable);
    }

    /**
     * Prompts the student to choose a tutorial or lab for a course already added to their timetable.
     * This method assumes that CourseManager has a method chooseActivityForCourse.
     */
    public void chooseActivityForCourse() {
        String courseCode = view.getInput("Enter the course code for which you want to choose an activity: ");
        int activityId = view.getIntegerInput("Enter the activity ID to choose: ");
        String studentEmail = ((AuthenticatedUser) sharedContext.currentUser).getEmail();
        boolean success = sharedContext.getCourseManager().chooseActivityForCourse(studentEmail, courseCode, activityId);
        if (success) {
            view.displaySuccess("Activity " + activityId + " for course " + courseCode + " chosen successfully.");
        } else {
            view.displayError("Failed to choose activity " + activityId + " for course " + courseCode + ".");
        }
    }
}
