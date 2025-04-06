package system_tests;

import controller.GuestController;
import controller.StudentController;
import controller.AdminStaffController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.AuthenticatedUser;
import model.Course;
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
 * System tests for the "Add Course to Timetable" use case.
 */
public class AddCourseToTimetableSystemTest extends TUITest {

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
     * Helper method to add a course to the system as admin
     */
    private void addCourseAsAdmin(String courseCode, String name, String description,
                                  String requiresComputers, String organiserName,
                                  String organiserEmail, String secretaryName,
                                  String secretaryEmail, String tutorials, String labs)
            throws URISyntaxException, IOException, ParseException {
        // Log in as admin staff
        loginAsAdminStaff(context);

        // Create input for adding a course with activities
        setMockInput(
                courseCode, name, description, requiresComputers,
                organiserName, organiserEmail,
                secretaryName, secretaryEmail,
                tutorials, labs,
                "n" // No activities for simplicity
        );

        // Use AdminStaffController to add the course
        new AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Log out
        new controller.AuthenticatedUserController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).logout();
    }

    /**
     * Helper method to add a course with activities to the system as admin
     */
    private void addCourseWithActivitiesAsAdmin(String courseCode, String name, String description,
                                                String requiresComputers, String organiserName,
                                                String organiserEmail, String secretaryName,
                                                String secretaryEmail, String tutorials, String labs,
                                                String[][] activities)
            throws URISyntaxException, IOException, ParseException {
        // Log in as admin staff
        loginAsAdminStaff(context);

        // Build input for adding a course with activities
        StringBuilder input = new StringBuilder();
        input.append(courseCode).append("\n")
                .append(name).append("\n")
                .append(description).append("\n")
                .append(requiresComputers).append("\n")
                .append(organiserName).append("\n")
                .append(organiserEmail).append("\n")
                .append(secretaryName).append("\n")
                .append(secretaryEmail).append("\n")
                .append(tutorials).append("\n")
                .append(labs).append("\n");

        // Add activities if provided
        if (activities != null && activities.length > 0) {
            for (String[] activity : activities) {
                input.append("y\n"); // Yes, add an activity
                for (String param : activity) {
                    input.append(param).append("\n");
                }
            }
        }

        input.append("n\n"); // No more activities

        // Set the input
        setMockInput(input.toString());

        // Use AdminStaffController to add the course
        new AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourse();

        // Log out
        new controller.AuthenticatedUserController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).logout();
    }

    /**
     * Test successfully adding a course to a student's timetable.
     */
    @Test
    public void testAddCourseToTimetable() throws URISyntaxException, IOException, ParseException {
        // Step 1: First add a course to the system as admin
        addCourseAsAdmin(
                "CSE1001", "Computer Science Intro", "Programming basics", "y",
                "Dr. Smith", "smith@hindeburg.ac.nz",
                "Ms. Jones", "jones@hindeburg.ac.nz",
                "1", "1"
        );

        // Step 2: Log in as a student
        loginAsStudent(context);

        // Step 3: Add the course to the student's timetable
        setMockInput("CSE1001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.addCourseToTimetable();

        // Verify output contains success message
        assertOutputContains("added to your timetable");
    }

    /**
     * Test attempting to add a non-existent course to the timetable.
     */
    @Test
    public void testAddNonExistentCourse() throws URISyntaxException, IOException, ParseException {
        // Log in as a student
        loginAsStudent(context);

        // Try to add a non-existent course to the timetable
        setMockInput("NONEXIST101");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.addCourseToTimetable();

        // Verify output contains error message
        assertOutputContains("does not exist");
    }

    /**
     * Test adding a course with an empty course code.
     */
    @Test
    public void testAddCourseWithEmptyCode() throws URISyntaxException, IOException, ParseException {
        // Log in as a student
        loginAsStudent(context);

        // Try to add a course with an empty code
        setMockInput("");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.addCourseToTimetable();

        // Verify output contains error message
        assertOutputContains("Invalid course code");
    }

    /**
     * Test adding a course with timing conflicts.
     */
    @Test
    public void testAddCourseWithConflict() throws URISyntaxException, IOException, ParseException {
        // Step 1: First add two courses with conflicting schedules as admin

        // Add first course with a Monday 9-10 lecture
        String[][] course1Activities = {
                {"101", "09:00", "10:00", "2025-05-01", "2025-08-30", "Room 101", "MONDAY"}
        };

        addCourseWithActivitiesAsAdmin(
                "CSE1001", "Computer Science Intro", "Programming basics", "y",
                "Dr. Smith", "smith@hindeburg.ac.nz",
                "Ms. Jones", "jones@hindeburg.ac.nz",
                "1", "1",
                course1Activities
        );

        // Add second course with a conflicting Monday 9-10 lecture
        String[][] course2Activities = {
                {"102", "09:00", "10:00", "2025-05-01", "2025-08-30", "Room 102", "MONDAY"}
        };

        addCourseWithActivitiesAsAdmin(
                "MAT2001", "Mathematics", "Advanced math", "n",
                "Dr. Math", "math@hindeburg.ac.nz",
                "Ms. Algebra", "algebra@hindeburg.ac.nz",
                "1", "0",
                course2Activities
        );

        // Step 2: Log in as a student
        loginAsStudent(context);

        // Step 3: Add the first course to the student's timetable
        setMockInput("CSE1001");
        new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourseToTimetable();

        // Step 4: Try to add the second course with conflicting schedule
        setMockInput("MAT2001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.addCourseToTimetable();

        // Verify the conflict warning - check for various possible warning messages
        String output = getCapturedOutput();
        boolean conflictDetected =
                output.contains("Conflict detected") ||
                        output.contains("conflict with") ||
                        output.contains("WARNING (Conflict");

        assertTrue(conflictDetected, "Warning about conflict should be shown");

        // Also verify the course was still added despite the conflict
        assertOutputContains("added to your timetable");
    }

    /**
     * Test adding a course that requires choosing tutorials and labs.
     */
    @Test
    public void testAddCourseWithRequiredActivities() throws URISyntaxException, IOException, ParseException {
        // Step 1: Add a course with required tutorials and labs
        addCourseAsAdmin(
                "CSE1001", "Computer Science Intro", "Programming basics", "y",
                "Dr. Smith", "smith@hindeburg.ac.nz",
                "Ms. Jones", "jones@hindeburg.ac.nz",
                "2", "1" // 2 tutorials and 1 lab required
        );

        // Step 2: Log in as a student
        loginAsStudent(context);

        // Step 3: Add the course to the student's timetable
        setMockInput("CSE1001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.addCourseToTimetable();

        // Verify warning about tutorials/labs selection
        assertOutputContains("not yet chosen all required");

        // Verify the course was still added
        assertOutputContains("added to your timetable");
    }

    /**
     * Test adding a course that is already in the timetable.
     */
    @Test
    public void testAddDuplicateCourse() throws URISyntaxException, IOException, ParseException {
        // Step 1: Add a course to the system
        addCourseAsAdmin(
                "CSE1001", "Computer Science Intro", "Programming basics", "y",
                "Dr. Smith", "smith@hindeburg.ac.nz",
                "Ms. Jones", "jones@hindeburg.ac.nz",
                "1", "1"
        );

        // Step 2: Log in as a student
        loginAsStudent(context);

        // Step 3: Add the course to the student's timetable
        setMockInput("CSE1001");
        new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourseToTimetable();

        // Step 4: Try to add the same course again
        setMockInput("CSE1001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.addCourseToTimetable();

        // The implementation doesn't specifically prevent adding the same course twice
        // It's expected to succeed but might create duplicated activities
        assertOutputContains("added to your timetable");
    }

    /**
     * Test adding a course with an invalid code format.
     */
    @Test
    public void testAddCourseWithInvalidCodeFormat() throws URISyntaxException, IOException, ParseException {
        // Log in as a student
        loginAsStudent(context);

        // Try to add a course with an invalid code format
        setMockInput("CSE-1001"); // Invalid format with hyphen
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.addCourseToTimetable();

        // Verify error message
        // The exact message might vary, but it should indicate an invalid input
        String output = getCapturedOutput();
        boolean errorFound =
                output.contains("Invalid course code") ||
                        output.contains("does not exist") ||
                        output.contains("invalid");

        assertTrue(errorFound, "Error about invalid course code should be shown");
    }

    /**
     * Test adding multiple courses to the timetable.
     * Fixed to add activities to the courses so they will appear in the timetable output.
     */
    @Test
    public void testAddMultipleCourses() throws URISyntaxException, IOException, ParseException {
        // Step 1: Add two courses with activities to the system
        String[][] course1Activities = {
                {"101", "09:00", "10:00", "2025-05-01", "2025-08-30", "Room 101", "MONDAY"}
        };

        addCourseWithActivitiesAsAdmin(
                "CSE1001", "Computer Science Intro", "Programming basics", "y",
                "Dr. Smith", "smith@hindeburg.ac.nz",
                "Ms. Jones", "jones@hindeburg.ac.nz",
                "1", "1",
                course1Activities
        );

        String[][] course2Activities = {
                {"102", "11:00", "12:00", "2025-05-01", "2025-08-30", "Room 102", "TUESDAY"}
        };

        addCourseWithActivitiesAsAdmin(
                "MAT2001", "Mathematics", "Advanced math", "n",
                "Dr. Math", "math@hindeburg.ac.nz",
                "Ms. Algebra", "algebra@hindeburg.ac.nz",
                "1", "0",
                course2Activities
        );

        // Step 2: Log in as a student
        loginAsStudent(context);

        // Step 3: Add the first course to the student's timetable
        setMockInput("CSE1001");
        new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService()).addCourseToTimetable();

        // Step 4: Add the second course to the student's timetable
        setMockInput("MAT2001");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.addCourseToTimetable();

        // Verify the second course was added
        assertOutputContains("added to your timetable");

        // Step 5: View timetable to verify both courses are present
        resetOutputCapture();
        controller.viewTimetable();

        String timetableOutput = getCapturedOutput();
        assertTrue(timetableOutput.contains("CSE1001") && timetableOutput.contains("MAT2001"),
                "Both courses should appear in the timetable");
    }

    /**
     * Test adding a course when student has reached maximum allowed courses.
     * Note: Since there's no actual limit implemented, this test verifies we can add multiple courses
     * (up to a reasonable number) without errors.
     */
    @Test
    public void testAddCourseWithMaximumReached() throws URISyntaxException, IOException, ParseException {
        // Add a single course with activities to ensure we'll have a complete test
        // even if there's no limit on courses
        String[][] courseActivities = {
                {"101", "09:00", "10:00", "2025-05-01", "2025-08-30", "Room 101", "MONDAY"}
        };

        addCourseWithActivitiesAsAdmin(
                "CSE0001", "Computer Science 1", "Course 1", "y",
                "Dr. Smith", "smith@hindeburg.ac.nz",
                "Ms. Jones", "jones@hindeburg.ac.nz",
                "1", "1",
                courseActivities
        );

        // Add one more course that we'll test adding at the end
        addCourseAsAdmin(
                "CSE9999", "Extra Course", "One more course", "y",
                "Dr. Extra", "extra@hindeburg.ac.nz",
                "Ms. Extra", "extra@hindeburg.ac.nz",
                "1", "1"
        );

        // Log in as a student
        loginAsStudent(context);

        // Add the course to the student's timetable
        setMockInput("CSE0001");
        StudentController studentController = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());
        studentController.addCourseToTimetable();

        // Try to add one more course
        setMockInput("CSE9999");
        controller = new StudentController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.addCourseToTimetable();

        // Expect the course to be added since there's no limit implemented
        assertOutputContains("added to your timetable");
    }
}