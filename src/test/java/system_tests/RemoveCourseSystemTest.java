package system_tests;

import controller.AdminController;
import external.MockEmailService;
import model.AuthenticatedUser;
import model.SharedContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;

import static org.junit.jupiter.api.Assertions.*;

public class RemoveCourseSystemTest extends TUITest {

    @Test
    public void testRemoveExistingCourse() {
        // Step 1: Add a course
        setMockInput(
                "CSC4001", "Removable Course", "For deletion", "y",
                "Dr. X", "x@hindeburg.ac.nz",
                "Ms. Y", "y@hindeburg.ac.nz",
                "2", "1",
                "n" 
        );

        SharedContext context = new SharedContext();
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");
        AdminController controller = new AdminController(context, new TextUserInterface(), new MockEmailService());

        startOutputCapture();
        controller.addCourse();
        assertOutputContains("successfully created");

        // Step 2: remove the course
        setMockInput("CSC4001"); 

        startOutputCapture();
        controller.removeCourse();
        assertOutputContains("removed successfully");
    }

    @Test
    public void testRemoveNonexistentCourse() {
        SharedContext context = new SharedContext();
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");
        AdminController controller = new AdminController(context, new TextUserInterface(), new MockEmailService());

        setMockInput("NON9999"); // non-existent course

        startOutputCapture();
        controller.removeCourse();
        assertOutputContains("Course not found");
    }
}
