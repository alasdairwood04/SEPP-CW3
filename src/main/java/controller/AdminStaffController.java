package controller;

import external.MockAuthenticationService;
import external.MockEmailService;
import org.json.simple.parser.ParseException;
import org.tinylog.Logger;
import external.AuthenticationService;
import external.EmailService;
import model.*;
import util.LogUtil;
import view.TextUserInterface;
import view.View;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AdminStaffController extends StaffController {
    private final CourseManager courseManager;

    public AdminStaffController(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);

        this.courseManager = sharedContext.getCourseManager();
    }

    public void manageFAQ() {
        FAQSection currentSection = null;

        while (true) {
            if (currentSection == null) {
                view.displayFAQ(sharedContext.getFAQManager());
                view.displayInfo("[-1] Return to main menu");
            } else {
                view.displayFAQSection(currentSection);
                view.displayInfo("[-1] Return to " + (currentSection.getParent() == null ? "FAQ" : currentSection.getParent().getTopic()));
            }
            view.displayInfo("[-2] Add FAQ item");

            // User Input
            String input = view.getInput("Please choose an option: ");
            try {
                int optionNo = Integer.parseInt(input);

                if (optionNo == -2) {
                    addFAQItem(currentSection);
                } else if (optionNo == -1) {
                    if (currentSection == null) {
                        break;
                    } else {
                        currentSection = currentSection.getParent();
                    }
                } else {
                    try {
                        if (currentSection == null) {
                            currentSection = sharedContext.getFAQManager().getSections().get(optionNo);
                        } else {
                            currentSection = currentSection.getSubsections().get(optionNo);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        view.displayError("Invalid option: " + optionNo);
                    }
                }
            } catch (NumberFormatException e) {
                view.displayError("Invalid option: " + input);
            }
        }
    }

    private void addFAQItem(FAQSection currentSection) {
        // When adding an item at root of FAQ, creating a section is mandatory
        boolean createSection = (currentSection == null);
        if (!createSection) {
            createSection = view.getYesNoInput("Would you like to create a new topic for the FAQ item?");
        }

        if (createSection) {
            String newTopic = view.getInput("Enter new topic title: ");
            FAQSection newSection = new FAQSection(newTopic);

            if (currentSection == null) {
                if (sharedContext.getFAQManager().getSections().stream().anyMatch(section -> section.getTopic().equals(newTopic))) {
                    view.displayWarning("Topic '" + newTopic + "' already exists!");
                    newSection = sharedContext.getFAQManager().getSections().stream().filter(section -> section.getTopic().equals(newTopic)).findFirst().orElseThrow();
                } else {
                    sharedContext.getFAQManager().addSection(newTopic);

                    // Find the section that was just added to the FAQ manager
                    // This ensures we're working with the actual section object in the FAQ structure
                    // rather than a disconnected local object that isn't part of the persistent hierarchy
                    newSection = sharedContext.getFAQManager().getSections().stream()
                            .filter(section -> section.getTopic().equals(newTopic))
                            .findFirst()
                            .orElseThrow();
                    view.displayInfo("Created topic '" + newTopic + "'");
                }
            } else {
                if (currentSection.getSubsections().stream().anyMatch(section -> section.getTopic().equals(newTopic))) {
                    view.displayWarning("Topic '" + newTopic + "' already exists under '" + currentSection.getTopic() + "'!");
                    newSection = currentSection.getSubsections().stream().filter(section -> section.getTopic().equals(newTopic)).findFirst().orElseThrow();
                } else {
                    currentSection.addSubsection(newSection);
                    view.displayInfo("Created topic '" + newTopic + "' under '" + currentSection.getTopic() + "'");
                }
            }
            currentSection = newSection;
        }

        // Display header for clarity
        view.displayInfo("=== Add New FAQ Question-Answer Pair===");


        // Get current user's email for logging
        String currentUserEmail = ((AuthenticatedUser) sharedContext.currentUser).getEmail();
        String sectionTopic = currentSection.getTopic();

        // get question
        String question = view.getInput("Enter the question for new FAQ item: ");
        if (question.isBlank()) {
                LogUtil.logAction(
                        LocalDateTime.now(),
                        currentUserEmail,
                        "addFAQItem",
                        sectionTopic,
                        "FAILURE (Error: the question cannot be empty)"
                );
                view.displayError("The question cannot be empty");
                return;
        }
        String answer = view.getInput("Enter the answer for new FAQ item: ");

        if (answer.isBlank()) {
            LogUtil.logAction(
                    LocalDateTime.now(),
                    currentUserEmail,
                    "addFAQItem",
                    sectionTopic,
                    "FAILURE (Error: the answer cannot be empty)"
            );
            view.displayError("The answer cannot be empty");
            return;
        }

        boolean addTag = view.getYesNoInput("Would you like to add a course tags");

        FAQItem newItem;

        // TODO have to wait to implement this - need methods getCourseManager(), viewCourses() etc to do this part of the addFAQ part
        if (addTag) {

            // Get course manager from shared context
            CourseManager courseManager = sharedContext.getCourseManager();

            // Get course list
            String fullCourseDetailsAsString  = courseManager.viewCourses();

            // check if there is any courses
            if (fullCourseDetailsAsString.equals("No courses available.")) {
                view.displayInfo("No courses available in the system");
                // add without tag
                currentSection.addItem(question, answer);
            } else {
                view.displayInfo("Available courses:");

                String[] courseLines = fullCourseDetailsAsString.split("\n");
                for (String line : courseLines) {
                    if (!line.isEmpty()) {
                        String[] parts = line.split(" - ", 2);
                        if (parts.length >= 2) {
                            String courseCode = parts[0].trim();
                            String courseName = parts[1].trim();
                            view.displayInfo(courseCode + " : " + courseName);
                        }
                    }
                }
                // get course code input
                String courseTag = view.getInput("Enter course code to add as tag:");

                // validate input
                if (!courseManager.hasCourse(courseTag)) {
                    LogUtil.logAction(
                            LocalDateTime.now(),
                            currentUserEmail,
                            "addFAQItem",
                            sectionTopic,
                            "FAILURE (Error: The tag must correspond to a course code)"
                    );
                    view.displayError("The tag must correspond to a course code");
                    return; // Exit method
                }
                // add with tag
                currentSection.addItem(question, answer, courseTag);
            }
        } else {
            // add without tag
            currentSection.addItem(question, answer);
        }

        LogUtil.logAction(
                LocalDateTime.now(),
                currentUserEmail,
                "addFAQItem",
                sectionTopic,
                "SUCCESS (A new FAQ item was added)"
        );
        view.displaySuccess("Created new FAQ item");
    }



    public void manageInquiries() {
        String[] inquiryTitles = getInquiryTitles(sharedContext.inquiries);

        while (true) {
            view.displayInfo("Pending inquiries");
            int selection = selectFromMenu(inquiryTitles, "Back to main menu");
            if (selection == -1) {
                return;
            }
            Inquiry selectedInquiry = sharedContext.inquiries.get(selection);

            while (true) {
                view.displayDivider();
                view.displayInquiry(selectedInquiry);
                view.displayDivider();
                String[] followUpOptions = { "Redirect inquiry", "Respond to inquiry" };
                int followUpSelection = selectFromMenu(followUpOptions, "Back to all inquiries");

                if (followUpSelection == -1) {
                    break;
                } else if (followUpOptions[followUpSelection].equals("Redirect inquiry")) {
                    redirectInquiry(selectedInquiry);
                } else if (followUpOptions[followUpSelection].equals("Respond to inquiry")) {
                    respondToInquiry(selectedInquiry);
                    inquiryTitles = getInquiryTitles(sharedContext.inquiries); // required to remove responded inquiry from titles
                    break;
                }
            }
        }
    }

    private void redirectInquiry(Inquiry inquiry) {
        inquiry.setAssignedTo(view.getInput("Enter assignee email: "));
        email.sendEmail(
                SharedContext.ADMIN_STAFF_EMAIL,
                inquiry.getAssignedTo(),
                "New inquiry from " + inquiry.getInquirerEmail(),
                "Subject: " + inquiry.getSubject() + "\nPlease log into the Self Service Portal to review and respond to the inquiry."
        );
        view.displaySuccess("Inquiry has been reassigned");
    }

    public void manageCourses() {
        boolean exit = false;
        while (!exit) {
            view.displayInfo("Course Management:");
            view.displayInfo("[1] Add Course");
            view.displayInfo("[2] View Courses");
            view.displayInfo("[3] Remove Course");
            view.displayInfo("[0] Back to Main Menu");

            int choice = view.getIntegerInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    addCourse();
                    break;
                case 2:
                    viewCourses();
                    break;
                case 3:
                    removeCourse();
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

        int status = email.sendEmail(
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

        for (String emailAddress : emailsToNotify) {
            email.sendEmail(  // Changed from emailService to email
                    "noreply@hindeburg.ac.nz",
                    emailAddress,
                    "Course Removed - " + courseCode,
                    "Please be informed that course " + courseCode + " has been removed."
            );
        }
    }
}
