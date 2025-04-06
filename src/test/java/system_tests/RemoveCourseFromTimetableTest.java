package system_tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import model.*;
import controller.StudentController;
import view.View;
import external.AuthenticationService;
import external.EmailService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Unit test for the "Remove Course from Timetable" use case.
 */
public class RemoveCourseFromTimetableTest {

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
    private static final String COURSE_CODE = "CSE1001";
    private static final String COURSE_NAME = "Introduction to Computer Science";
    private static final String SECOND_COURSE_CODE = "MAT2001";
    private static final String SECOND_COURSE_NAME = "Mathematics";

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

        // Add test courses to the system
        setupFirstCourse();
        setupSecondCourse();
    }

    private void setupFirstCourse() {
        // Add first course to the system
        courseManager.addCourse(
                COURSE_CODE,
                COURSE_NAME,
                "Learn the basics of computer science",
                true,
                "Prof. Smith", "smith@hindeburg.ac.nz",
                "John Doe", "john@hindeburg.ac.nz",
                1, // required tutorials
                1, // required labs
                "admin@hindeburg.ac.nz"
        );

        // Add activities to the course
        Course course = courseManager.getCourse(COURSE_CODE);

        // Add a lecture
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
        course.addActivity(lecture);

        // Add a tutorial
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
        course.addActivity(tutorial);
    }

    private void setupSecondCourse() {
        // Add second course to the system
        courseManager.addCourse(
                SECOND_COURSE_CODE,
                SECOND_COURSE_NAME,
                "Advanced mathematics",
                false,
                "Prof. Jones", "jones@hindeburg.ac.nz",
                "Jane Smith", "jane@hindeburg.ac.nz",
                1, // required tutorials
                1, // required labs
                "admin@hindeburg.ac.nz"
        );

        // Add activities to the course
        Course course = courseManager.getCourse(SECOND_COURSE_CODE);

        // Add a lecture
        Lecture lecture = new Lecture(
                102,
                LocalDate.now(),
                LocalTime.of(11, 0),
                LocalDate.now().plusMonths(3),
                LocalTime.of(12, 0),
                "Room D104",
                DayOfWeek.TUESDAY,
                true // recorded
        );
        course.addActivity(lecture);
    }

    /**
     * Test successfully removing a course from timetable.
     */
    @Test
    void testRemoveCourseFromTimetable() {
        // Arrange - Add the course to the timetable first
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE);

        // Reset mock to clear previous interactions
        reset(mockView);

        // Mock input for removing the course
        when(mockView.getInput(contains("Enter the course code"))).thenReturn(COURSE_CODE);

        // Act
        studentController.removeCourseFromTimetable();

        // Assert
        Timetable timetable = sharedContext.getTimetable(STUDENT_EMAIL);
        assertFalse(timetable.hasSlotsForCourse(COURSE_CODE),
                "Course should be removed from timetable");

        // Verify view interactions
        verify(mockView).getInput(contains("Enter the course code"));
        verify(mockView).displaySuccess(contains("removed from your timetable"));
    }

    /**
     * Test attempting to remove a course that's not in the timetable.
     */
    @Test
    void testRemoveCourseNotInTimetable() {
        // Arrange - Don't add any course to the timetable

        // Mock input for trying to remove a course
        when(mockView.getInput(contains("Enter the course code"))).thenReturn(COURSE_CODE);

        // Act
        studentController.removeCourseFromTimetable();

        // Assert - Should get an error message
        verify(mockView).displayError(contains("not in your timetable"));
    }

    /**
     * Test removing one course while keeping another in the timetable.
     */
    @Test
    void testRemoveOneCourseKeepAnother() {
        // Arrange - Add two courses to the timetable
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE);
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, SECOND_COURSE_CODE);

        // Reset mock to clear previous interactions
        reset(mockView);

        // Mock input for removing the first course
        when(mockView.getInput(contains("Enter the course code"))).thenReturn(COURSE_CODE);

        // Act
        studentController.removeCourseFromTimetable();

        // Assert
        Timetable timetable = sharedContext.getTimetable(STUDENT_EMAIL);
        assertFalse(timetable.hasSlotsForCourse(COURSE_CODE),
                "First course should be removed from timetable");
        assertTrue(timetable.hasSlotsForCourse(SECOND_COURSE_CODE),
                "Second course should still be in timetable");

        // Verify view interactions
        verify(mockView).displaySuccess(contains("removed from your timetable"));
    }

    /**
     * Test removing a course after choosing some activities.
     */
    @Test
    void testRemoveCourseWithChosenActivities() {
        // Arrange - Add the course to the timetable and choose an activity
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE);
        courseManager.chooseActivityForCourse(STUDENT_EMAIL, COURSE_CODE, 201); // Choose the tutorial

        // Reset mock to clear previous interactions
        reset(mockView);

        // Mock input for removing the course
        when(mockView.getInput(contains("Enter the course code"))).thenReturn(COURSE_CODE);

        // Act
        studentController.removeCourseFromTimetable();

        // Assert
        Timetable timetable = sharedContext.getTimetable(STUDENT_EMAIL);
        assertFalse(timetable.hasSlotsForCourse(COURSE_CODE),
                "Course with chosen activities should be removed from timetable");

        // Verify view interactions
        verify(mockView).displaySuccess(contains("removed from your timetable"));
    }

    /**
     * Test removing a non-existent course.
     */
    @Test
    void testRemoveNonExistentCourse() {
        // Arrange - Add a course to the timetable
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE);

        // Reset mock to clear previous interactions
        reset(mockView);

        // Mock input for removing a non-existent course
        String nonExistentCourse = "FAKE1000";
        when(mockView.getInput(contains("Enter the course code"))).thenReturn(nonExistentCourse);

        // Act
        studentController.removeCourseFromTimetable();

        // Assert
        Timetable timetable = sharedContext.getTimetable(STUDENT_EMAIL);
        assertTrue(timetable.hasSlotsForCourse(COURSE_CODE),
                "Existing course should still be in timetable");

        // Verify error message
        verify(mockView).displayError(contains("not in your timetable"));
    }

    /**
     * Test removing a course with an empty course code.
     */
    @Test
    void testRemoveCourseWithEmptyCode() {
        // Arrange
        when(mockView.getInput(contains("Enter the course code"))).thenReturn("");

        // Act
        studentController.removeCourseFromTimetable();

        // Assert - Should get an error message about the course not being in timetable
        verify(mockView).displayError(contains("not in your timetable"));
    }
}