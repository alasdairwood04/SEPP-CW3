package system_tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

import model.*;
import controller.StudentController;
import view.View;
import external.AuthenticationService;
import external.EmailService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Unit test for the "View Timetable" use case.
 */
public class ViewTimetableTest {

    @Mock
    private View mockView;

    @Mock
    private AuthenticationService mockAuth;

    @Mock
    private EmailService mockEmail;

    private SharedContext sharedContext;
    private CourseManager courseManager;
    private StudentController studentController;
    private static final String STUDENT_EMAIL = "student@hindeburg.ac.nz";
    private static final String COURSE_CODE_1 = "CSE1001";
    private static final String COURSE_NAME_1 = "Introduction to Computer Science";
    private static final String COURSE_CODE_2 = "MAT2001";
    private static final String COURSE_NAME_2 = "Mathematics";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up shared context with mocked view
        sharedContext = new SharedContext(mockView);
        courseManager = sharedContext.getCourseManager();

        // Set up StudentController with mocked dependencies
        studentController = new StudentController(sharedContext, mockView, mockAuth, mockEmail);

        // Set current user as a student
        AuthenticatedUser student = new AuthenticatedUser(STUDENT_EMAIL, "Student");
        sharedContext.currentUser = student;

        // Set up courses
        setupCourses();
    }

    private void setupCourses() {
        // Add first course to the system
        courseManager.addCourse(
                COURSE_CODE_1,
                COURSE_NAME_1,
                "Learn the basics of computer science",
                true,
                "Prof. Smith", "smith@hindeburg.ac.nz",
                "John Doe", "john@hindeburg.ac.nz",
                1, // required tutorials
                1, // required labs
                "admin@hindeburg.ac.nz"
        );

        Course course1 = courseManager.getCourse(COURSE_CODE_1);

        // Add a lecture (Monday)
        Lecture lecture = new Lecture(
                101,
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalDate.now().plusMonths(3),
                LocalTime.of(10, 0),
                "Room A101",
                DayOfWeek.MONDAY,
                true // recorded
        );
        course1.addActivity(lecture);

        // Add a tutorial (Wednesday)
        Tutorial tutorial = new Tutorial(
                201,
                LocalDate.now(),
                LocalTime.of(14, 0),
                LocalDate.now().plusMonths(3),
                LocalTime.of(15, 0),
                "Room B202",
                DayOfWeek.WEDNESDAY,
                30 // capacity
        );
        course1.addActivity(tutorial);

        // Add a lab (Friday)
        Lab lab = new Lab(
                301,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalDate.now().plusMonths(3),
                LocalTime.of(12, 0),
                "Lab C303",
                DayOfWeek.FRIDAY,
                20 // capacity
        );
        course1.addActivity(lab);

        // Add second course to the system
        courseManager.addCourse(
                COURSE_CODE_2,
                COURSE_NAME_2,
                "Advanced mathematics",
                false,
                "Prof. Jones", "jones@hindeburg.ac.nz",
                "Jane Smith", "jane@hindeburg.ac.nz",
                1, // required tutorials
                0, // required labs
                "admin@hindeburg.ac.nz"
        );

        Course course2 = courseManager.getCourse(COURSE_CODE_2);

        // Add a lecture (Tuesday)
        Lecture mathLecture = new Lecture(
                102,
                LocalDate.now(),
                LocalTime.of(11, 0),
                LocalDate.now().plusMonths(3),
                LocalTime.of(12, 0),
                "Room D104",
                DayOfWeek.TUESDAY,
                true // recorded
        );
        course2.addActivity(mathLecture);

        // Add a tutorial (Thursday)
        Tutorial mathTutorial = new Tutorial(
                202,
                LocalDate.now(),
                LocalTime.of(15, 0),
                LocalDate.now().plusMonths(3),
                LocalTime.of(16, 0),
                "Room E205",
                DayOfWeek.THURSDAY,
                30 // capacity
        );
        course2.addActivity(mathTutorial);

        // Add a weekend activity (Saturday)
        Tutorial saturdaySession = new Tutorial(
                203,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalDate.now().plusMonths(3),
                LocalTime.of(12, 0),
                "Room F206",
                DayOfWeek.SATURDAY,
                20 // capacity
        );
        course2.addActivity(saturdaySession);
    }

    /**
     * Test viewing an empty timetable.
     */
    @Test
    void testViewEmptyTimetable() {
        // Arrange - Don't add any courses to the timetable

        // Act
        studentController.viewTimetable();

        // Assert - Capture the displayed message
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockView).displayInfo(messageCaptor.capture());

        String displayedMessage = messageCaptor.getValue();
        assertTrue(displayedMessage.contains("No scheduled activities") ||
                        displayedMessage.trim().isEmpty(),
                "Empty timetable should indicate no activities");
    }

    /**
     * Test viewing a timetable with one course.
     */
    @Test
    void testViewTimetableWithOneCourse() {
        // Arrange - Add one course to the timetable
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE_1);

        // Reset mock to clear previous interactions
        reset(mockView);

        // Act
        studentController.viewTimetable();

        // Assert - Capture the displayed message
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockView).displayInfo(messageCaptor.capture());

        String displayedMessage = messageCaptor.getValue();
        assertTrue(displayedMessage.contains(COURSE_CODE_1),
                "Timetable should contain the course code");
        assertTrue(displayedMessage.contains("MONDAY"),
                "Timetable should contain Monday lecture");
        assertTrue(displayedMessage.contains("WEDNESDAY"),
                "Timetable should contain Wednesday tutorial");
        assertTrue(displayedMessage.contains("FRIDAY"),
                "Timetable should contain Friday lab");
    }

    /**
     * Test viewing a timetable with multiple courses.
     */
    @Test
    void testViewTimetableWithMultipleCourses() {
        // Arrange - Add two courses to the timetable
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE_1);
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE_2);

        // Reset mock to clear previous interactions
        reset(mockView);

        // Act
        studentController.viewTimetable();

        // Assert - Capture the displayed message
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockView).displayInfo(messageCaptor.capture());

        String displayedMessage = messageCaptor.getValue();

        // Should contain both course codes
        assertTrue(displayedMessage.contains(COURSE_CODE_1),
                "Timetable should contain the first course code");
        assertTrue(displayedMessage.contains(COURSE_CODE_2),
                "Timetable should contain the second course code");

        // Should contain all weekdays
        assertTrue(displayedMessage.contains("MONDAY"), "Timetable should contain Monday");
        assertTrue(displayedMessage.contains("TUESDAY"), "Timetable should contain Tuesday");
        assertTrue(displayedMessage.contains("WEDNESDAY"), "Timetable should contain Wednesday");
        assertTrue(displayedMessage.contains("THURSDAY"), "Timetable should contain Thursday");
        assertTrue(displayedMessage.contains("FRIDAY"), "Timetable should contain Friday");

        // Should NOT contain weekend days (filtered out by working week)
        assertFalse(displayedMessage.contains("SATURDAY"),
                "Working week timetable should not contain Saturday");
        assertFalse(displayedMessage.contains("SUNDAY"),
                "Working week timetable should not contain Sunday");
    }

    /**
     * Test viewing a timetable with chosen activities.
     */
    @Test
    void testViewTimetableWithChosenActivities() {
        // Arrange - Add courses and choose specific activities
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE_1);
        courseManager.chooseActivityForCourse(STUDENT_EMAIL, COURSE_CODE_1, 201); // Choose tutorial
        courseManager.chooseActivityForCourse(STUDENT_EMAIL, COURSE_CODE_1, 301); // Choose lab

        // Reset mock to clear previous interactions
        reset(mockView);

        // Act
        studentController.viewTimetable();

        // Assert - Capture the displayed message
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockView).displayInfo(messageCaptor.capture());

        String displayedMessage = messageCaptor.getValue();
        assertTrue(displayedMessage.contains("CHOSEN"),
                "Timetable should indicate chosen activities");
    }

    /**
     * Test viewing a timetable after removing a course.
     */
    @Test
    void testViewTimetableAfterRemovingCourse() {
        // Arrange - Add two courses then remove one
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE_1);
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE_2);

        // Remove the first course
        Timetable timetable = sharedContext.getTimetable(STUDENT_EMAIL);
        timetable.removeSlotsForCourse(COURSE_CODE_1);

        // Reset mock to clear previous interactions
        reset(mockView);

        // Act
        studentController.viewTimetable();

        // Assert - Capture the displayed message
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockView).displayInfo(messageCaptor.capture());

        String displayedMessage = messageCaptor.getValue();
        assertFalse(displayedMessage.contains(COURSE_CODE_1),
                "Removed course should not appear in timetable");
        assertTrue(displayedMessage.contains(COURSE_CODE_2),
                "Remaining course should still appear in timetable");

        // Should NOT contain days that only had the removed course
        assertFalse(displayedMessage.contains("MONDAY"),
                "Monday activities were from removed course");
        assertTrue(displayedMessage.contains("TUESDAY"),
                "Tuesday activities should still be present");
    }
}