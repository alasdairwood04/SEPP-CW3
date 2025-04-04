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
 * Unit test for the "Choose Tutorial or Lab for Course" use case.
 */
public class ChooseActivityForCourseTest {

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
    private static final int TUTORIAL_ID = 201;
    private static final int LAB_ID = 301;

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

        // Add a test course to the system
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
                TUTORIAL_ID,
                LocalDate.now(),
                LocalTime.of(14, 0),
                LocalDate.now().plusMonths(3),
                LocalTime.of(15, 0),
                "Room B202",
                DayOfWeek.WEDNESDAY,
                30 // capacity
        );
        course.addActivity(tutorial);

        // Add a lab
        Lab lab = new Lab(
                LAB_ID,
                LocalDate.now(),
                LocalTime.of(10, 0),
                LocalDate.now().plusMonths(3),
                LocalTime.of(12, 0),
                "Lab C303",
                DayOfWeek.FRIDAY,
                20 // capacity
        );
        course.addActivity(lab);
    }

    /**
     * Test successfully choosing a tutorial for a course.
     */
    @Test
    void testChooseTutorialForCourse() {
        // Arrange - First add the course to the timetable
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE);

        // Reset mock to clear previous interactions
        reset(mockView);

        // Mock inputs for choosing a tutorial
        when(mockView.getInput(contains("course code"))).thenReturn(COURSE_CODE);
        when(mockView.getIntegerInput(contains("activity ID"))).thenReturn(TUTORIAL_ID);

        // Act
        studentController.chooseActivityForCourse();

        // Assert
        Timetable timetable = sharedContext.getTimetable(STUDENT_EMAIL);
        assertNotNull(timetable, "Timetable should exist");
        assertEquals(1, timetable.numChosenActivities(COURSE_CODE),
                "One activity should be chosen for the course");

        // Verify view interactions
        verify(mockView).getInput(contains("course code"));
        verify(mockView).getIntegerInput(contains("activity ID"));
        verify(mockView, atLeastOnce()).displaySuccess(contains("chosen successfully"));
    }

    /**
     * Test successfully choosing a lab for a course.
     */
    @Test
    void testChooseLabForCourse() {
        // Arrange - First add the course to the timetable
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE);

        // Reset mock to clear previous interactions
        reset(mockView);

        // Mock inputs for choosing a lab
        doReturn(COURSE_CODE).when(mockView).getInput(contains("course code"));
        doReturn(LAB_ID).when(mockView).getIntegerInput(contains("activity ID"));

        // Act
        studentController.chooseActivityForCourse();

        // Assert
        Timetable timetable = sharedContext.getTimetable(STUDENT_EMAIL);
        assertNotNull(timetable, "Timetable should exist");
        assertEquals(1, timetable.numChosenActivities(COURSE_CODE),
                "One activity should be chosen for the course");

        // Verify view interactions
        verify(mockView).getInput(contains("course code"));
        verify(mockView).getIntegerInput(contains("activity ID"));
        verify(mockView, atLeastOnce()).displaySuccess(contains("chosen successfully"));
    }

    /**
     * Test attempting to choose an activity for a course not in the timetable.
     */
    @Test
    void testChooseActivityForCourseNotInTimetable() {
        // Arrange - Don't add the course to timetable first
        when(mockView.getInput(contains("course code"))).thenReturn(COURSE_CODE);
        when(mockView.getIntegerInput(contains("activity ID"))).thenReturn(TUTORIAL_ID);

        // Act
        studentController.chooseActivityForCourse();

        // Assert - The course is not in the timetable, so should get an error
        verify(mockView, atLeastOnce()).displayError(contains("not in your timetable"));
    }

    /**
     * Test attempting to choose a non-existent activity for a course.
     */
    @Test
    void testChooseNonExistentActivityForCourse() {
        // Arrange - First add the course to the timetable
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE);

        // Reset mock to clear previous interactions
        reset(mockView);

        // Mock inputs for choosing a non-existent activity
        int nonExistentActivityId = 999;
        when(mockView.getInput(contains("course code"))).thenReturn(COURSE_CODE);
        when(mockView.getIntegerInput(contains("activity ID"))).thenReturn(nonExistentActivityId);

        // Act
        studentController.chooseActivityForCourse();

        // Assert
        Timetable timetable = sharedContext.getTimetable(STUDENT_EMAIL);
        assertEquals(0, timetable.numChosenActivities(COURSE_CODE),
                "No activities should be chosen for the course");

        // Verify error message
        verify(mockView, atLeastOnce()).displayError(contains("not found"));
    }

    /**
     * Test choosing multiple activities for a course.
     */
    @Test
    void testChooseMultipleActivitiesForCourse() {
        // Arrange - First add the course to the timetable
        courseManager.addCourseToStudentTimetable(STUDENT_EMAIL, COURSE_CODE);

        // Choose a tutorial first
        courseManager.chooseActivityForCourse(STUDENT_EMAIL, COURSE_CODE, TUTORIAL_ID);

        // Reset mock to clear previous interactions
        reset(mockView);

        // Mock inputs for choosing a lab
        when(mockView.getInput(contains("course code"))).thenReturn(COURSE_CODE);
        when(mockView.getIntegerInput(contains("activity ID"))).thenReturn(LAB_ID);

        // Act - Choose a lab
        studentController.chooseActivityForCourse();

        // Assert
        Timetable timetable = sharedContext.getTimetable(STUDENT_EMAIL);
        assertEquals(2, timetable.numChosenActivities(COURSE_CODE),
                "Two activities should be chosen for the course");

        // Verify success message
        verify(mockView, atLeastOnce()).displaySuccess(contains("chosen successfully"));
    }

    /**
     * Test choosing an activity for a non-existent course.
     */
    @Test
    void testChooseActivityForNonExistentCourse() {
        // Arrange
        String nonExistentCourse = "FAKE1000";
        when(mockView.getInput(contains("course code"))).thenReturn(nonExistentCourse);
        when(mockView.getIntegerInput(contains("activity ID"))).thenReturn(TUTORIAL_ID);

        // Act
        studentController.chooseActivityForCourse();

        // Assert
        verify(mockView, atLeastOnce()).displayError(contains("does not exist"));
    }
}