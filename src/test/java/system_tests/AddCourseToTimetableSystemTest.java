package system_tests;

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
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddCourseToTimetableSystemTest extends TUITest {

    private SharedContext context;
    private View view;
    private StudentController studentController;
    private MockAuthenticationService authService;
    private MockEmailService emailService;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, ParseException {
        view = new TextUserInterface();
        context = new SharedContext(view);
        authService = new MockAuthenticationService();
        emailService = new MockEmailService();
        // Set up a student user.
        context.currentUser = new AuthenticatedUser("student@hindeburg.ac.nz", "Student");
        studentController = new StudentController(context, view, authService, emailService);
    }

    @Test
    public void testAddCourseToStudentTimetable_Success() {
        // Prepopulate course in the system.
        context.getCourseManager().addCourse("CSC3001", "Advanced Systems", "Course Description", false,
                "Dr. A", "a@hindeburg.ac.nz", "Ms. B", "b@hindeburg.ac.nz", 3, 2, "admin@hindeburg.ac.nz");

        // Simulate student input for adding the course.
        setMockInput("CSC3001");
        startOutputCapture();
        studentController.addCourseToTimetable();
        String output = getCapturedOutput();
        assertTrue(output.contains("Course CSC3001 added to your timetable"));
    }

    @Test
    public void testAddCourseToStudentTimetable_InvalidCourseCode() {
        // Test with empty course code.
        setMockInput("");
        startOutputCapture();
        studentController.addCourseToTimetable();
        String output = getCapturedOutput();
        assertTrue(output.contains("Invalid course code"));
    }

    @Test
    public void testAddCourseToStudentTimetable_CourseDoesNotExist() {
        // No course added in system.
        setMockInput("NONEXISTENT");
        startOutputCapture();
        studentController.addCourseToTimetable();
        String output = getCapturedOutput();
        assertTrue(output.contains("Course NONEXISTENT does not exist"));
    }

    @Test
    public void testAddCourseToStudentTimetable_ConflictUnrecordedLecture() {
        // Prepopulate a course with two activities:
        // One lecture (unrecorded) and one tutorial.
        context.getCourseManager().addCourse("CSC3002", "Data Structures", "Course Description", false,
                "Dr. D", "d@hindeburg.ac.nz", "Ms. E", "e@hindeburg.ac.nz", 1, 1, "admin@hindeburg.ac.nz");
        // Assume that addCourseToStudentTimetable() in CourseManager checks conflicts by comparing dates/times.
        // For testing, simulate a conflict by adding a timeslot manually.
        // First, add course to timetable normally.
        setMockInput("CSC3002");
        studentController.addCourseToTimetable();
        // Now, manually add a conflicting timeslot to simulate an unrecorded lecture conflict.
        context.getOrCreateTimetable("student@hindeburg.ac.nz")
                .addTimeSlot(java.time.DayOfWeek.MONDAY, java.time.LocalDate.now(),
                        java.time.LocalTime.of(9, 0), java.time.LocalDate.now(),
                        java.time.LocalTime.of(10, 0), "OTHER", 99,
                        // Unrecorded lecture assumed to be critical.
                        /* status */ null);
        // Attempt to add the same course again, expecting conflict abort.
        setMockInput("CSC3002");
        startOutputCapture();
        studentController.addCourseToTimetable();
        String output = getCapturedOutput();
        assertTrue(output.contains("Cannot add course due to conflict with unrecorded lecture"));
    }

    @Test
    public void testAddCourseToStudentTimetable_IncompleteLabTutorialWarning() {
        // Prepopulate course with required tutorials/labs.
        context.getCourseManager().addCourse("CSC3003", "Operating Systems", "Course Description", false,
                "Dr. O", "o@hindeburg.ac.nz", "Ms. P", "p@hindeburg.ac.nz", 2, 1, "admin@hindeburg.ac.nz");

        // When adding the course, if the number of chosen activities is less than required,
        // the system should warn (post-addition check).
        setMockInput("CSC3003");
        startOutputCapture();
        studentController.addCourseToTimetable();
        String output = getCapturedOutput();
        // Expect a warning about incomplete lab/tutorial selection.
        assertTrue(output.contains("You have not yet chosen all required tutorials/labs"));
    }
}
