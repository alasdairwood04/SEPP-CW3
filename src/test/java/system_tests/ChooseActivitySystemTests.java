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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChooseActivitySystemTests extends TUITest {

    private SharedContext context;
    private View view;
    private StudentController studentController;
    private MockAuthenticationService authService;
    private MockEmailService emailService;

    @BeforeEach
    public void setup() throws URISyntaxException, IOException, ParseException {
        view = new TextUserInterface();
        context = new SharedContext(view);
        authService = new MockAuthenticationService();
        emailService = new MockEmailService();
        context.currentUser = new AuthenticatedUser("student@hindeburg.ac.nz", "Student");
        studentController = new StudentController(context, view, authService, emailService);

        // Prepopulate a course and add it to the student's timetable.
        context.getCourseManager().addCourse("CSC3004", "Computer Architecture", "Course Description", false,
                "Dr. X", "x@hindeburg.ac.nz", "Ms. Y", "y@hindeburg.ac.nz", 1, 1, "admin@hindeburg.ac.nz");
        setMockInput("CSC3004");
        studentController.addCourseToTimetable();
    }

    @Test
    public void testChooseActivityForCourse_Success() {
        // Assume the first activity added gets an ID of 1.
        setMockInput("CSC3004", "1");
        startOutputCapture();
        studentController.chooseActivityForCourse();
        String output = getCapturedOutput();
        assertTrue(output.contains("Activity 1 for course CSC3004 has been chosen successfully"));
    }

    @Test
    public void testChooseActivityForCourse_CourseNotInTimetable() {
        // Try choosing an activity for a course not added.
        setMockInput("NONEXISTENT", "1");
        startOutputCapture();
        studentController.chooseActivityForCourse();
        String output = getCapturedOutput();
        assertTrue(output.contains("Course NONEXISTENT is not in your timetable"));
    }

    @Test
    public void testChooseActivityForCourse_ActivityNotFound() {
        // Try choosing an activity with a non-existent ID.
        setMockInput("CSC3004", "999"); // Assuming 999 does not exist.
        startOutputCapture();
        studentController.chooseActivityForCourse();
        String output = getCapturedOutput();
        assertTrue(output.contains("Activity with ID 999 for course CSC3004 not found"));
    }
}
