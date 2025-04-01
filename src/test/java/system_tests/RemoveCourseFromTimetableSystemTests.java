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

public class RemoveCourseFromTimetableSystemTests extends TUITest {

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

        // Prepopulate and add a course to the student's timetable.
        context.getCourseManager().addCourse("CSC3007", "Algorithms", "Course Description", false,
                "Dr. X", "x@hindeburg.ac.nz", "Ms. Y", "y@hindeburg.ac.nz", 2, 2, "admin@hindeburg.ac.nz");
        setMockInput("CSC3007");
        studentController.addCourseToTimetable();
    }

    @Test
    public void testRemoveCourseFromTimetable_Success() {
        // Remove the course from the student's timetable.
        setMockInput("CSC3007");
        startOutputCapture();
        studentController.removeCourseFromTimetable();
        String output = getCapturedOutput();
        assertTrue(output.contains("Course CSC3007 removed from your timetable"));
    }

    @Test
    public void testRemoveCourseFromTimetable_CourseNotPresent() {
        // Attempt to remove a course that was never added.
        setMockInput("NONEXISTENT");
        startOutputCapture();
        studentController.removeCourseFromTimetable();
        String output = getCapturedOutput();
        assertTrue(output.contains("Course NONEXISTENT is not in your timetable"));
    }
}

