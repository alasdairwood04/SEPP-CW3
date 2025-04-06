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
 * System tests for the "View Timetable" use case.
 */
public class ViewTimetableSystemTest extends TUITest {

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
     * Test viewing an empty timetable.
     */
    @Test
    public void testViewEmptyTimetable() throws URISyntaxException, IOException, ParseException {
        // Log in as a student
        loginAsStudent(context);

        // View empty timetable
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.viewTimetable();

        // Verify output indicates no activities
        assertOutputContains("No scheduled activities");
    }

    /**
     * Test viewing a timetable with one course.
     */
    @Test
    public void testViewTimetableWithOneCourse() throws URISyntaxException, IOException, ParseException {
        // Step 1: First log in as admin and add a course
        loginAsAdminStaff(context);

        setMockInput("CSE1001\nComputer Science Intro\nProgramming basics\ny\nDr. Smith\nsmith@hindeburg.ac.nz\nMs. Jones\njones@hindeburg.ac.nz\n1\n1\ny\n101\n09:00\n10:00\n2025-05-01\n2025-08-30\nRoom 101\nMONDAY\nn");

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Step 2: Log out and log in as a student
        new controller.AuthenticatedUserController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).logout();
        loginAsStudent(context);

        // Step 3: Add the course to the student's timetable
        setMockInput("CSE1001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());
        controller.addCourseToTimetable();

        // Step 4: View timetable
        startOutputCapture();
        controller.viewTimetable();

        // Verify output contains course code and day
        assertOutputContains("CSE1001");
        assertOutputContains("MONDAY");
    }

    /**
     * Test viewing a timetable with multiple courses.
     */
    @Test
    public void testViewTimetableWithMultipleCourses() throws URISyntaxException, IOException, ParseException {
        // Step 1: First log in as admin and add two courses
        loginAsAdminStaff(context);

        // Add first course
        setMockInput("CSE1001\nComputer Science Intro\nProgramming basics\ny\nDr. Smith\nsmith@hindeburg.ac.nz\nMs. Jones\njones@hindeburg.ac.nz\n1\n1\ny\n101\n09:00\n10:00\n2025-05-01\n2025-08-30\nRoom 101\nMONDAY\nn");

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Add second course
        setMockInput("MAT2001\nMathematics\nAdvanced math\nn\nDr. Math\nmath@hindeburg.ac.nz\nMs. Algebra\nalgebra@hindeburg.ac.nz\n1\n0\ny\n102\n11:00\n12:00\n2025-05-01\n2025-08-30\nRoom 102\nTUESDAY\nn");

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
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());
        controller.addCourseToTimetable();

        // Step 4: View timetable
        startOutputCapture();
        controller.viewTimetable();

        // Verify output contains both course codes and weekdays
        assertOutputContains("CSE1001");
        assertOutputContains("MAT2001");
        assertOutputContains("MONDAY");
        assertOutputContains("TUESDAY");
    }

    /**
     * Test viewing a timetable with chosen activities.
     */
    @Test
    public void testViewTimetableWithChosenActivities() throws URISyntaxException, IOException, ParseException {
        // Step 1: First log in as admin and add a course
        loginAsAdminStaff(context);

        // Add course with multiple activities
        setMockInput("CSE1001\nComputer Science Intro\nProgramming basics\ny\nDr. Smith\nsmith@hindeburg.ac.nz\nMs. Jones\njones@hindeburg.ac.nz\n1\n1\ny\n101\n09:00\n10:00\n2025-05-01\n2025-08-30\nRoom 101\nMONDAY\ny\n201\n14:00\n15:00\n2025-05-01\n2025-08-30\nRoom 201\nWEDNESDAY\ny\n301\n10:00\n12:00\n2025-05-01\n2025-08-30\nLab 301\nFRIDAY\nn");

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Step 2: Log out and log in as a student
        new controller.AuthenticatedUserController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).logout();
        loginAsStudent(context);

        // Recreate the controller with a new TextUserInterface for each mock input
        // Step 3: Add the course to the student's timetable
        setMockInput("CSE1001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());
        controller.addCourseToTimetable();

        // Step 4: Choose tutorial activity - Use a new controller instance
        setMockInput("CSE1001\n201");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());
        controller.chooseActivityForCourse();

        // Step 5: Choose lab activity - Use a new controller instance
        setMockInput("CSE1001\n301");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());
        controller.chooseActivityForCourse();

        // Step 6: View timetable
        startOutputCapture();
        controller.viewTimetable();

        // Verify output contains chosen activities indication
        assertOutputContains("CHOSEN");
    }

    /**
     * Test viewing a timetable after removing a course.
     */
    @Test
    public void testViewTimetableAfterRemovingCourse() throws URISyntaxException, IOException, ParseException {
        // Step 1: First log in as admin and add two courses
        loginAsAdminStaff(context);

        // Add first course
        setMockInput("CSE1001\nComputer Science Intro\nProgramming basics\ny\nDr. Smith\nsmith@hindeburg.ac.nz\nMs. Jones\njones@hindeburg.ac.nz\n1\n1\ny\n101\n09:00\n10:00\n2025-05-01\n2025-08-30\nRoom 101\nMONDAY\nn");

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Add second course
        setMockInput("MAT2001\nMathematics\nAdvanced math\nn\nDr. Math\nmath@hindeburg.ac.nz\nMs. Algebra\nalgebra@hindeburg.ac.nz\n1\n0\ny\n102\n11:00\n12:00\n2025-05-01\n2025-08-30\nRoom 102\nTUESDAY\nn");

        new controller.AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Step 2: Log out and log in as a student
        new controller.AuthenticatedUserController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).logout();
        loginAsStudent(context);
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        // Step 3: Add first course to timetable
        setMockInput("CSE1001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());
        controller.addCourseToTimetable();

        // Step 4: Add second course to timetable - Use a new controller instance
        setMockInput("MAT2001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());
        controller.addCourseToTimetable();

        // Step 5: Verify both courses exist in timetable
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());
        startOutputCapture();
        controller.viewTimetable();
        String initialTimetable = getCapturedOutput();
        resetOutputCapture();

        // Confirm both courses are added
        assertTrue(initialTimetable.contains("CSE1001") && initialTimetable.contains("MAT2001"),
                "Both courses should initially be in the timetable");

        // Step 6: Remove first course - Use a new controller instance
        setMockInput("CSE1001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());
        controller.removeCourseFromTimetable();

        // Step 7: View timetable to verify only MAT2001 remains
        startOutputCapture();
        controller.viewTimetable();

        // Should only contain MAT2001, not CSE1001
        assertOutputContains("MAT2001");
        String output = getCapturedOutput();
        assertFalse(output.contains("CSE1001"), "CSE1001 should have been removed from timetable");
    }
}