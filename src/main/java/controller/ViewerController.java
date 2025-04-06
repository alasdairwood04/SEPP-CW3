package controller;

import external.AuthenticationService;
import external.EmailService;
import model.Course;
import model.SharedContext;
import view.View;

/**
 * Controller for viewing courses and course details.
 * This controller contains functionality that is common across different user types.
 */
public class ViewerController extends Controller {

    public ViewerController(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);
    }

    /**
     * Displays a list of all courses in the system.
     */
    public void viewCourses() {
        String coursesListText = sharedContext.getCourseManager().viewCourses();

        if (coursesListText.equals("No courses available.")) {
            view.displayInfo("No courses found.");
            return;
        }

        view.displayInfo("=== All Courses ===");
        view.displayInfo(coursesListText);
    }

    /**
     * Displays detailed information about a specific course.
     *
     * @param courseCode The code of the course to view
     */
    public void viewSpecificCourse(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            view.displayError("Please provide a valid course code.");
            return;
        }

        String courseDetails = sharedContext.getCourseManager().viewCourse(courseCode);

        if (courseDetails == null) {
            view.displayError("Course not found: " + courseCode);
            return;
        }

        view.displayInfo("=== Course Details ===");
        view.displayInfo(courseDetails);
    }
}