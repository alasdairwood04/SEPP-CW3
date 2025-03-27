package system_tests;

import controller.AdminController;
import external.MockEmailService;
import model.AuthenticatedUser;
import model.Course;
import model.SharedContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;

import static org.junit.jupiter.api.Assertions.*;

public class ViewCourseSystemTest extends TUITest {

    private SharedContext context;
    private AdminController controller;

    @BeforeEach
    public void setUp() {
        context = new SharedContext(); // fresh context for test
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");
        controller = new AdminController(context, new TextUserInterface(), new MockEmailService());
    }

    @Test
    public void testViewCoursesEmptyList() {
        startOutputCapture();
        controller.viewCourses();
        assertOutputContains("No courses found");
    }



    @Test
    public void testViewCoursesAfterAddingOne() {
        setMockInput(
                "CSC3333", "Embedded Systems", "Low-level", "y",
                "Dr. Z", "z@hindeburg.ac.nz",
                "Ms. X", "x@hindeburg.ac.nz",
                "4", "2",
                "y", "1", "2025-03-26", "09:00", "2025-04-01", "10:00", "Room 42", "Monday",
                "n"
        );

        SharedContext context = SharedContext.getInstance();
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");

        AdminController controller = new AdminController(context, new TextUserInterface(), new MockEmailService());

        // Step 1: Add course
        startOutputCapture();
        controller.addCourse();
        assertOutputContains("successfully created");

        // Step 2: View courses
        startOutputCapture();
        controller.viewCourses();
        assertOutputContains("CSC3333");
        assertOutputContains("Embedded Systems");
    }




    @Test
    public void testViewMultipleCourses() {
        //Add first course
        setMockInput(
                "CSC1001", "Artificial Intelligence", "Learn AI", "y",
                "Prof. A", "a@hindeburg.ac.nz",
                "Ms. B", "b@hindeburg.ac.nz",
                "3", "2",
                "n"
        );
        SharedContext context = new SharedContext();
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");
        AdminController controller = new AdminController(context, new TextUserInterface(), new MockEmailService());

        startOutputCapture();
        controller.addCourse();
        assertOutputContains("successfully created");

        // Add second course
        setMockInput(
                "CSC1002", "Machine Learning", "Learn ML", "n",
                "Prof. X", "x@hindeburg.ac.nz",
                "Ms. Y", "y@hindeburg.ac.nz",
                "2", "1",
                "n"
        );
        AdminController controller2 = new AdminController(context, new TextUserInterface(), new MockEmailService());

        startOutputCapture();
        controller2.addCourse();
        assertOutputContains("successfully created");

        //viewCourses
        startOutputCapture();
        controller.viewCourses();

        assertOutputContains("CSC1001");
        assertOutputContains("Artificial Intelligence");
        assertOutputContains("CSC1002");
        assertOutputContains("Machine Learning");
    }

}
