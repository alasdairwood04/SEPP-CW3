package system_tests;

import controller.GuestController;
import controller.StudentController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.AuthenticatedUser;
import model.SharedContext;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;
import view.View;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * System tests for the "Choose Activity for Course" use case.
 */
public class ChooseActivityForCourseSystemTest extends TUITest {

    private SharedContext context;
    private StudentController controller;
    private View view;

    @BeforeEach
    public void setUp() throws Exception {
        // Create a new context with a view for each test
        view = new TextUserInterface();
        context = new SharedContext(view);
    }

    /**
     * Test successfully choosing a tutorial for a course.
     */
    @Test
    public void testChooseTutorialForCourse() throws URISyntaxException, IOException, ParseException {
        // Step 1: First log in as admin and add a course with a tutorial
        loginAsAdminStaff(context);

        // Use a simpler input string with exact formatting to ensure all data is parsed correctly
        String input = "CSE1001\n" +
                "Computer Science Intro\n" +
                "Programming basics\n" +
                "y\n" +
                "Dr. Smith\n" +
                "smith@hindeburg.ac.nz\n" +
                "Ms. Jones\n" +
                "jones@hindeburg.ac.nz\n" +
                "1\n" +
                "1\n" +
                "y\n" +  // Add activity
                "101\n" + // Activity ID
                "09:00\n" + // Start time
                "10:00\n" + // End time
                "2025-05-01\n" + // Start date
                "2025-08-30\n" + // End date
                "Room 101\n" + // Location
                "MONDAY\n" + // Day
                "y\n" +  // Add another activity
                "201\n" + // Activity ID for tutorial
                "14:00\n" + // Start time
                "15:00\n" + // End time
                "2025-05-02\n" + // Start date
                "2025-08-30\n" + // End date
                "Room 201\n" + // Location
                "WEDNESDAY\n" + // Day
                "n\n"; // No more activities

        setMockInput(input);

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Step 2: Log out and log in as a student
        new controller.AuthenticatedUserController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).logout();
        loginAsStudent(context);

        // Step 3: Add the course to the student's timetable
        setMockInput("CSE1001");
        new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourseToTimetable();

        // Step 4: Choose the tutorial activity
        setMockInput("CSE1001\n201"); // Course code, Tutorial ID
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.chooseActivityForCourse();

        // Verify output contains success message
        assertOutputContains("chosen successfully");
    }

    /**
     * Test successfully choosing a lab for a course.
     */
    @Test
    public void testChooseLabForCourse() throws URISyntaxException, IOException, ParseException {
        // Step 1: First log in as admin and add a course with a lab
        loginAsAdminStaff(context);

        // Use a simpler input string with exact formatting to ensure all data is parsed correctly
        String input = "CSE1001\n" +
                "Computer Science Intro\n" +
                "Programming basics\n" +
                "y\n" +
                "Dr. Smith\n" +
                "smith@hindeburg.ac.nz\n" +
                "Ms. Jones\n" +
                "jones@hindeburg.ac.nz\n" +
                "1\n" +
                "1\n" +
                "y\n" +  // Add activity
                "101\n" + // Activity ID
                "09:00\n" + // Start time
                "10:00\n" + // End time
                "2025-05-01\n" + // Start date
                "2025-08-30\n" + // End date
                "Room 101\n" + // Location
                "MONDAY\n" + // Day
                "y\n" +  // Add another activity
                "301\n" + // Activity ID for lab
                "10:00\n" + // Start time
                "12:00\n" + // End time
                "2025-05-03\n" + // Start date
                "2025-08-30\n" + // End date
                "Lab 301\n" + // Location
                "FRIDAY\n" + // Day
                "n\n"; // No more activities

        setMockInput(input);

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Step 2: Log out and log in as a student
        new controller.AuthenticatedUserController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).logout();
        loginAsStudent(context);

        // Step 3: Add the course to the student's timetable
        setMockInput("CSE1001");
        new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourseToTimetable();

        // Step 4: Choose the lab activity
        setMockInput("CSE1001\n301"); // Course code, Lab ID
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.chooseActivityForCourse();

        // Verify output contains success message
        assertOutputContains("chosen successfully");
    }

    /**
     * Test attempting to choose an activity for a course not in the timetable.
     */
    @Test
    public void testChooseActivityForCourseNotInTimetable() throws URISyntaxException, IOException, ParseException {
        // Step 1: First log in as admin and add a course
        loginAsAdminStaff(context);

        // Use a simpler input string with exact formatting
        String input = "CSE1001\n" +
                "Computer Science Intro\n" +
                "Programming basics\n" +
                "y\n" +
                "Dr. Smith\n" +
                "smith@hindeburg.ac.nz\n" +
                "Ms. Jones\n" +
                "jones@hindeburg.ac.nz\n" +
                "1\n" +
                "1\n" +
                "y\n" +  // Add activity
                "201\n" + // Activity ID for tutorial
                "14:00\n" + // Start time
                "15:00\n" + // End time
                "2025-05-02\n" + // Start date
                "2025-08-30\n" + // End date
                "Room 201\n" + // Location
                "WEDNESDAY\n" + // Day
                "n\n"; // No more activities

        setMockInput(input);

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Step 2: Log out and log in as a student
        new controller.AuthenticatedUserController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).logout();
        loginAsStudent(context);

        // Step 3: Try to choose an activity without adding the course first
        setMockInput("CSE1001\n201"); // Course code, Tutorial ID
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.chooseActivityForCourse();

        // Verify output contains error message
        assertOutputContains("not in your timetable");
    }

    /**
     * Test choosing a non-existent activity for a course.
     */
    @Test
    public void testChooseNonExistentActivity() throws URISyntaxException, IOException, ParseException {
        // Step 1: First log in as admin and add a course
        loginAsAdminStaff(context);

        String input = "CSE1001\n" +
                "Computer Science Intro\n" +
                "Programming basics\n" +
                "y\n" +
                "Dr. Smith\n" +
                "smith@hindeburg.ac.nz\n" +
                "Ms. Jones\n" +
                "jones@hindeburg.ac.nz\n" +
                "1\n" +
                "1\n" +
                "y\n" +  // Add activity
                "201\n" + // Activity ID for tutorial
                "14:00\n" + // Start time
                "15:00\n" + // End time
                "2025-05-02\n" + // Start date
                "2025-08-30\n" + // End date
                "Room 201\n" + // Location
                "WEDNESDAY\n" + // Day
                "n\n"; // No more activities

        setMockInput(input);

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Step 2: Log out and log in as a student
        new controller.AuthenticatedUserController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).logout();
        loginAsStudent(context);

        // Step 3: Add the course to the student's timetable
        setMockInput("CSE1001");
        new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourseToTimetable();

        // Step 4: Try to choose a non-existent activity
        setMockInput("CSE1001\n999"); // Course code, Non-existent activity ID
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.chooseActivityForCourse();

        // Verify output contains error message - it will say "not found" or something similar
        assertOutputContains("not found");
    }

    /**
     * Test choosing multiple activities for a course.
     */
    @Test
    public void testChooseMultipleActivities() throws URISyntaxException, IOException, ParseException {
        // Step 1: First log in as admin and add a course with tutorial and lab
        loginAsAdminStaff(context);

        String input = "CSE1001\n" +
                "Computer Science Intro\n" +
                "Programming basics\n" +
                "y\n" +
                "Dr. Smith\n" +
                "smith@hindeburg.ac.nz\n" +
                "Ms. Jones\n" +
                "jones@hindeburg.ac.nz\n" +
                "1\n" +
                "1\n" +
                "y\n" +  // Add activity
                "101\n" + // Activity ID
                "09:00\n" + // Start time
                "10:00\n" + // End time
                "2025-05-01\n" + // Start date
                "2025-08-30\n" + // End date
                "Room 101\n" + // Location
                "MONDAY\n" + // Day
                "y\n" +  // Add another activity
                "201\n" + // Activity ID for tutorial
                "14:00\n" + // Start time
                "15:00\n" + // End time
                "2025-05-02\n" + // Start date
                "2025-08-30\n" + // End date
                "Room 201\n" + // Location
                "WEDNESDAY\n" + // Day
                "y\n" +  // Add another activity
                "301\n" + // Activity ID for lab
                "10:00\n" + // Start time
                "12:00\n" + // End time
                "2025-05-03\n" + // Start date
                "2025-08-30\n" + // End date
                "Lab 301\n" + // Location
                "FRIDAY\n" + // Day
                "n\n"; // No more activities

        setMockInput(input);

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Step 2: Log out and log in as a student
        new controller.AuthenticatedUserController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).logout();
        loginAsStudent(context);

        // Step 3: Add the course to the student's timetable
        setMockInput("CSE1001");
        new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourseToTimetable();

        // Step 4: Choose the tutorial activity
        setMockInput("CSE1001\n201"); // Course code, Tutorial ID
        new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).chooseActivityForCourse();

        // Step 5: Choose the lab activity
        setMockInput("CSE1001\n301"); // Course code, Lab ID
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.chooseActivityForCourse();

        // Verify output contains success message
        assertOutputContains("chosen successfully");

        // Step 6: View timetable to verify both activities are chosen
        controller.viewTimetable();

        // The output should contain the course code and both chosen activities
        assertOutputContains("CHOSEN");
    }

    /**
     * Test choosing an activity for a non-existent course.
     */
    @Test
    public void testChooseActivityForNonExistentCourse() throws URISyntaxException, IOException, ParseException {
        // Log in as a student
        loginAsStudent(context);

        // Try to choose an activity for a non-existent course
        setMockInput("NONEXIST101\n201"); // Non-existent course code, some activity ID
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.chooseActivityForCourse();

        // Verify output contains error message
        assertOutputContains("does not exist");
    }
}