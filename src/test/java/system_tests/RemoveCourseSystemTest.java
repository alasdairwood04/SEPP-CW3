package system_tests;

import controller.AdminController;
import controller.AdminStaffController;
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

import static org.junit.jupiter.api.Assertions.*;

public class RemoveCourseSystemTest extends TUITest {
    private SharedContext context;
    private AdminStaffController controller;
    private View view;
    private MockAuthenticationService authService;
    private MockEmailService emailService;


    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, ParseException {
        // Create the view AFTER setMockInput has been called
        view = new TextUserInterface();
        context = new SharedContext(view);
        authService = new MockAuthenticationService();
        emailService = new MockEmailService();

        // Set up admin user
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");

        // Create controller with all required parameters
        // Fix this line:
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());
    }

    @Test
    public void testAddCourse() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "CSC4001", "Removable Course", "For deletion", "y",
                "Dr. X", "x@hindeburg.ac.nz",
                "Ms. Y", "y@hindeburg.ac.nz",
                "2", "1",
                "n"
        );
        //SharedContext context = new SharedContext();
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");

        AdminStaffController admin = new AdminStaffController(context, new TextUserInterface(), new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        admin.addCourse();
        assertOutputContains("successfully created");
    }

    @Test
    public void testRemoveCourse() throws URISyntaxException, IOException, ParseException {
        // First add a course
        testAddCourse();

        // Then try to remove it
        setMockInput("CSC4001");

        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");

        AdminStaffController admin = new AdminStaffController(context, new TextUserInterface(), new MockAuthenticationService(), new MockEmailService());


        startOutputCapture();
        admin.removeCourse();
        assertOutputContains("removed successfully");
    }

    @Test
    public void testRemoveNonexistentCourse() throws URISyntaxException, IOException, ParseException {
        setMockInput("NON9999"); // non-existent course

        //controller = new AdminController(context, view, new MockEmailService());
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");

        AdminStaffController admin = new AdminStaffController(context, new TextUserInterface(), new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        admin.removeCourse();
        assertOutputContains("Course not found");
    }
}