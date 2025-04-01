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

public class ViewTimetableSystemTests extends TUITest {

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
    }

    @Test
    public void testViewTimetable_EmptyTimetable() {
        // When no course has been added, timetable should indicate no scheduled activities.
        startOutputCapture();
        studentController.viewTimetable();
        String output = getCapturedOutput();
        assertTrue(output.contains("No scheduled activities"));
    }

    @Test
    public void testViewTimetable_DisplaysWorkingWeek() {
        // Prepopulate a course and add it to the timetable.
        context.getCourseManager().addCourse("CSC3005", "Operating Systems", "Course Description", false,
                "Dr. O", "o@hindeburg.ac.nz", "Ms. P", "p@hindeburg.ac.nz", 1, 1, "admin@hindeburg.ac.nz");
        setMockInput("CSC3005");
        studentController.addCourseToTimetable();
        // Now view timetable; assuming the course activity was scheduled on a working day (e.g., Monday).
        startOutputCapture();
        studentController.viewTimetable();
        String output = getCapturedOutput();
        assertTrue(output.contains("Timetable for"));
        assertTrue(output.contains("CSC3005"));
        assertTrue(output.contains("Monday")); // assuming the activity is scheduled on Monday.
    }

    @Test
    public void testViewTimetable_DoesNotShowWeekend() {
        // Prepopulate a course and add an activity that falls on Saturday.
        context.getCourseManager().addCourse("CSC3006", "Networks", "Course Description", false,
                "Dr. N", "n@hindeburg.ac.nz", "Ms. Q", "q@hindeburg.ac.nz", 1, 1, "admin@hindeburg.ac.nz");
        // Manually add an activity scheduled on Saturday to the student's timetable.
        context.getOrCreateTimetable("student@hindeburg.ac.nz")
                .addTimeSlot(java.time.DayOfWeek.SATURDAY,
                        java.time.LocalDate.now(),
                        java.time.LocalTime.of(10, 0),
                        java.time.LocalDate.now(),
                        java.time.LocalTime.of(11, 0),
                        "CSC3006",
                        1,
                        /* status */ null);
        startOutputCapture();
        studentController.viewTimetable();
        String output = getCapturedOutput();
        // The output should not include Saturday.
        assertTrue(!output.contains("Saturday"));
    }
}
