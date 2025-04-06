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
import static org.junit.jupiter.api.Assertions.*;

/**
 * System tests for the "Remove Course from Timetable" use case.
 */
public class RemoveCourseFromTimetableSystemTest extends TUITest {

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
     * Test successfully removing a course from a timetable.
     */
    @Test
    public void testRemoveCourseFromTimetable() throws URISyntaxException, IOException, ParseException {
        // Step 1: First log in as admin and add a course
        loginAsAdminStaff(context);

        // Use a simple string input format that matches the expected format in the application
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
        StudentController addController = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());
        addController.addCourseToTimetable();

        // Step 4: Remove the course from the timetable
        setMockInput("CSE1001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.removeCourseFromTimetable();

        // Verify output contains success message
        assertOutputContains("removed from your timetable");
    }

    /**
     * Test attempting to remove a course that's not in the timetable.
     */
    @Test
    public void testRemoveCourseNotInTimetable() throws URISyntaxException, IOException, ParseException {
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
                "101\n" + // Activity ID
                "09:00\n" + // Start time
                "10:00\n" + // End time
                "2025-05-01\n" + // Start date
                "2025-08-30\n" + // End date
                "Room 101\n" + // Location
                "MONDAY\n" + // Day
                "n\n"; // No more activities

        setMockInput(input);

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Step 2: Log out and log in as a student
        new controller.AuthenticatedUserController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).logout();
        loginAsStudent(context);

        // Step 3: Try to remove a course that's not in the timetable
        setMockInput("CSE1001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.removeCourseFromTimetable();

        // Verify output contains error message
        assertOutputContains("not in your timetable");
    }

    /**
     * Test removing one course while keeping another in the timetable.
     */
    @Test
    public void testRemoveOneCourseKeepAnother() throws URISyntaxException, IOException, ParseException {
        // Step 1: First log in as admin and add two courses
        loginAsAdminStaff(context);

        // Add first course
        String input1 = "CSE1001\n" +
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
                "n\n"; // No more activities

        setMockInput(input1);

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Add second course
        String input2 = "MAT2001\n" +
                "Mathematics\n" +
                "Advanced math\n" +
                "n\n" +
                "Dr. Math\n" +
                "math@hindeburg.ac.nz\n" +
                "Ms. Algebra\n" +
                "algebra@hindeburg.ac.nz\n" +
                "1\n" +
                "0\n" +
                "y\n" +  // Add activity
                "102\n" + // Activity ID
                "11:00\n" + // Start time
                "12:00\n" + // End time
                "2025-05-02\n" + // Start date
                "2025-08-30\n" + // End date
                "Room 102\n" + // Location
                "TUESDAY\n" + // Day
                "n\n"; // No more activities

        setMockInput(input2);

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Step 2: Log out and log in as a student
        new controller.AuthenticatedUserController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).logout();
        loginAsStudent(context);

        // Step 3: Add both courses to the student's timetable
        setMockInput("CSE1001");
        new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourseToTimetable();

        setMockInput("MAT2001");
        new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourseToTimetable();

        // Step 4: Remove only the first course
        setMockInput("CSE1001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.removeCourseFromTimetable();

        // Verify output contains success message
        assertOutputContains("removed from your timetable");

        // Step 5: View timetable to verify second course is still there
        resetOutputCapture();
        controller.viewTimetable();

        // The output should not contain the first course but should contain the second
        assertOutputContains("MAT2001");
    }

    /**
     * Test removing a course after choosing activities.
     */
    @Test
    public void testRemoveCourseWithChosenActivities() throws URISyntaxException, IOException, ParseException {
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
                "101\n" + // Activity ID (lecture)
                "09:00\n" + // Start time
                "10:00\n" + // End time
                "2025-05-01\n" + // Start date
                "2025-08-30\n" + // End date
                "Room 101\n" + // Location
                "MONDAY\n" + // Day
                "y\n" +  // Add another activity
                "201\n" + // Activity ID (tutorial)
                "14:00\n" + // Start time
                "15:00\n" + // End time
                "2025-05-02\n" + // Start date
                "2025-08-30\n" + // End date
                "Room 201\n" + // Location
                "WEDNESDAY\n" + // Day
                "y\n" +  // Add another activity
                "301\n" + // Activity ID (lab)
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

        // Step 4: Choose some activities
        setMockInput("CSE1001\n201"); // Course code, Tutorial ID
        new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).chooseActivityForCourse();

        setMockInput("CSE1001\n301"); // Course code, Lab ID
        new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).chooseActivityForCourse();

        // Step 5: Remove the course
        setMockInput("CSE1001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.removeCourseFromTimetable();

        // Verify output contains success message
        assertOutputContains("removed from your timetable");

        // Step 6: View timetable to verify course is removed
        resetOutputCapture();
        controller.viewTimetable();

        // The output should show an empty timetable
        assertOutputContains("No scheduled activities");
    }

    /**
     * Test removing a non-existent course.
     */
    @Test
    public void testRemoveNonExistentCourse() throws URISyntaxException, IOException, ParseException {
        // Log in as a student
        loginAsStudent(context);

        // Try to remove a non-existent course
        setMockInput("NONEXIST101");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.removeCourseFromTimetable();

        // Verify output contains error message
        assertOutputContains("not in your timetable");
    }

    /**
     * Test removing a course with an empty course code.
     */
    @Test
    public void testRemoveCourseWithEmptyCode() throws URISyntaxException, IOException, ParseException {
        // Log in as a student
        loginAsStudent(context);

        // Try to remove a course with an empty code
        setMockInput("");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.removeCourseFromTimetable();

        // Verify output contains error message
        assertOutputContains("not in your timetable");
    }
}