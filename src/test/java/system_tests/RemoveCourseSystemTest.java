package system_tests;

import controller.AdminController;
import external.MockEmailService;
import model.AuthenticatedUser;
import model.SharedContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;
import view.View;

import static org.junit.jupiter.api.Assertions.*;

public class RemoveCourseSystemTest extends TUITest {
    private SharedContext context;
    private View view;

    @BeforeEach
    public void setUp() {
        // Create the view AFTER setMockInput has been called
        view = new TextUserInterface();
        context = new SharedContext(view);
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");
    }

    @Test
    public void testRemoveExistingCourse() {
        // Step 1: Add a course
        setMockInput(
                "CSC4001", "Removable Course", "For deletion", "y",
                "Dr. X", "x@hindeburg.ac.nz",
                "Ms. Y", "y@hindeburg.ac.nz",
                "2", "1",
                "n",
                // Also include the input for the remove step
                "CSC4001"
        );

        AdminController controller = new AdminController(context, view, new MockEmailService());

        // Step 1: Add course
        startOutputCapture();
        controller.addCourse();
        assertOutputContains("successfully created");

        // Step 2: Remove the course (no need for another setMockInput)
        startOutputCapture();
        controller.removeCourse();
        assertOutputContains("removed successfully");
    }

    @Test
    public void testRemoveNonexistentCourse() {
        setMockInput("NON9999"); // non-existent course

        AdminController controller = new AdminController(context, view, new MockEmailService());

        startOutputCapture();
        controller.removeCourse();
        assertOutputContains("Course not found");
    }
}