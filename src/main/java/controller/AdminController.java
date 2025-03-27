package controller;

import external.EmailService;
import model.*;
import util.LogUtil;
import view.View;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AdminController {
    private final SharedContext sharedContext;
    private final View view;
    private final CourseManager courseManager;
    private final EmailService emailService;

    public AdminController(SharedContext sharedContext, View view, EmailService emailService) {
        this.sharedContext = sharedContext;
        this.view = view;
        this.courseManager = sharedContext.getCourseManager();
        this.emailService = emailService;

    }

    /**
     * Add a new course to the system.
     */

    public void addCourse() {
        view.displayInfo("=== Add Course ===");

        // --- Step 1: Get course info ---
        String courseCode = view.getInput("Enter course code: ");
        String name = view.getInput("Enter course name: ");
        String description = view.getInput("Enter description: ");
        boolean requiresComputers = view.getYesNoInput("Requires computers? (y/n): ");

        String organiserName = view.getInput("Enter organiser name: ");
        String organiserEmail = view.getInput("Enter organiser email: ");
        String secretaryName = view.getInput("Enter secretary name: ");
        String secretaryEmail = view.getInput("Enter secretary email: ");

        int requiredTutorials = view.getIntegerInput("Enter number of required tutorials: ");
        int requiredLabs = view.getIntegerInput("Enter number of required labs: ");

        String addedBy = sharedContext.getCurrentUserEmail();

        // --- Step 2: Try to add the course ---
        AddCourseResult result = courseManager.addCourse(
                courseCode, name, description, requiresComputers,
                organiserName, organiserEmail,
                secretaryName, secretaryEmail,
                requiredTutorials, requiredLabs,
                addedBy
        );

        if (!result.success) {
            view.displayError(result.message);
            return;
        }

        // --- Step 3: Course created, now collect activities ---
        view.displayInfo("=== Add Course - Activities ===");

        Course course = courseManager.getCourse(courseCode); // get created course
        while (true) {
            boolean addMore = view.getYesNoInput("Do you want to add an activity to this course? (y/n): ");
            if (!addMore) break;

            try {
                int id = Integer.parseInt(view.getInput("Enter activity ID (as integer): "));
                LocalTime startTime = LocalTime.parse(view.getInput("Enter start time (e.g., 09:00): "));
                LocalTime endTime = LocalTime.parse(view.getInput("Enter end time (e.g., 10:00): "));
                LocalDate startDate = LocalDate.parse(view.getInput("Enter start date (e.g., 2025-03-26): "));
                LocalDate endDate = LocalDate.parse(view.getInput("Enter end date (e.g., 2025-04-30): "));
                String location = view.getInput("Enter location: ");
                DayOfWeek day = DayOfWeek.valueOf(view.getInput("Enter day of week (e.g., MONDAY): ").toUpperCase());

                Activity activity = new ConcreteActivity(id, startDate, startTime, endDate, endTime, location, day);
                course.addActivity(activity);

            } catch (Exception e) {
                view.displayError("Invalid activity input. Please try again.");
            }
        }

        // --- Step 4: Final success message, log, and email ---
        LogUtil.logAction(
                LocalDateTime.now(), addedBy, "addCourse", courseCode,
                "SUCCESS (New course added)"
        );

        view.displaySuccess("Course has been successfully created.");

        int status = emailService.sendEmail(
                "noreply@hindeburg.ac.nz",
                organiserEmail,
                "Course Created - " + courseCode,
                "A course has been provided with the following details:\n\n" + course.toString()
        );

        if (status == EmailService.STATUS_SUCCESS) {
            view.displaySuccess("Confirmation email sent to course organiser.");
        } else {
            view.displayWarning("Failed to send confirmation email. Status code: " + status);
        }
    }

    /**
     * View all courses in the system.
     */

    public void viewCourses() {
        Collection<Course> allCourses = courseManager.getAllCourses();

        if (allCourses.isEmpty()) {
            view.displayInfo("No courses found.");
            return;
        }

        view.displayInfo("=== All Courses ===");

        for (Course course : allCourses) {
            view.displayInfo("Course Code: " + course.getCourseCode());
            view.displayInfo("Name: " + course.getName());
            view.displayInfo("Description: " + course.getDescription());
            view.displayInfo("Requires Computers: " + course.requiresComputers());
            view.displayInfo("Organiser: " + course.getCourseOrganiserName() + " <" + course.getCourseOrganiserEmail() + ">");
            view.displayInfo("Secretary: " + course.getCourseSecretaryName() + " <" + course.getCourseSecretaryEmail() + ">");
            view.displayInfo("Tutorials Required: " + course.getRequiredTutorials());
            view.displayInfo("Labs Required: " + course.getRequiredLabs());
            view.displayDivider();
        }
    }

    /**
     * Delete a course from the system.
     */

    public void removeCourse() {
        view.displayInfo("=== Delete Course ===");

        String courseCode = view.getInput("Enter course code to delete: ");
        String currentUserEmail = sharedContext.getCurrentUserEmail();

        String[] emailsToNotify = courseManager.removeCourse(courseCode);

        if (emailsToNotify == null) {
            view.displayError("Course not found: " + courseCode);
            return;
        }

        // Log
        LogUtil.logAction(
                LocalDateTime.now(),
                currentUserEmail,
                "removeCourse",
                courseCode,
                "SUCCESS"
        );

        view.displaySuccess("Course " + courseCode + " removed successfully.");

        for (String email : emailsToNotify) {
            emailService.sendEmail(
                    "noreply@hindeburg.ac.nz",
                    email,
                    "Course Removed - " + courseCode,
                    "Please be informed that course " + courseCode + " has been removed."
            );
        }
    }
}



