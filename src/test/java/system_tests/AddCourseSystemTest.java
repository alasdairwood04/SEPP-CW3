package system_tests;

import controller.AdminController;
import controller.GuestController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.AuthenticatedUser;
import model.SharedContext;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class AddCourseSystemTest extends TUITest {

    private SharedContext context;
    private AdminController controller;

    @BeforeEach
    public void setUp() throws Exception {
        context = SharedContext.getInstance();
        loginAsAdminStaff(context);

        controller = new AdminController(context, new TextUserInterface(), new MockEmailService());
    }

    @Test
    public void testLoginAsAdminStaff() throws URISyntaxException, IOException, ParseException {
        setMockInput("admin1", "admin1pass");
        SharedContext context = new SharedContext();
        GuestController guestController = new GuestController(context, new TextUserInterface(), new MockAuthenticationService(), new MockEmailService());
        startOutputCapture();
        guestController.login();
        assertOutputContains("Logged in as admin1");
        assertInstanceOf(AuthenticatedUser.class, context.currentUser);
        assertEquals("AdminStaff", ((AuthenticatedUser) context.currentUser).getRole());
    }

    @Test
    public void testAddCourse() {
        setMockInput(
                "CSC3001", "Advanced Systems", "Design", "y",
                "Dr. A", "a@hindeburg.ac.nz",
                "Ms. B", "b@hindeburg.ac.nz",
                "3", "2",
                "n"
        );

        SharedContext context = new SharedContext();
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");

        AdminController admin = new AdminController(context, new TextUserInterface(), new MockEmailService());

        startOutputCapture();
        admin.addCourse();

        assertOutputContains("successfully created");
        assertOutputContains("Confirmation email sent");
    }

    @Test
    public void testAddCourseWithActivity() {
        setMockInput(
                "CSC3333", "Embedded Systems", "Low-level", "y",
                "Dr. Z", "z@hindeburg.ac.nz",
                "Ms. X", "x@hindeburg.ac.nz",
                "4", "2",
                "y", "1", "2025-03-26", "09:00", "2025-04-01", "10:00", "Room 42", "Monday",
                "n"
        );

        SharedContext context = new SharedContext();
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");

        AdminController admin = new AdminController(context, new TextUserInterface(), new MockEmailService());

        startOutputCapture();
        admin.addCourse();

        assertOutputContains("successfully created");
        assertOutputContains("Confirmation email sent");
    }

    @Test
    public void testEmptyCourseCode() {
        setMockInput(
                "", "Advanced Systems", "Design", "y",
                "Dr. A", "a@hindeburg.ac.nz",
                "Ms. B", "b@hindeburg.ac.nz",
                "3", "2", "n"
        );

        SharedContext context = new SharedContext();
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");

        AdminController admin = new AdminController(context, new TextUserInterface(), new MockEmailService());

        startOutputCapture();
        admin.addCourse();

        assertOutputContains("Required course info not provided");
    }

    @Test
    public void testDuplicateCourseCode() {
        setMockInput(
                "CSC3001", "Advanced Systems", "Design", "y",
                "Dr. A", "a@hindeburg.ac.nz",
                "Ms. B", "b@hindeburg.ac.nz",
                "3", "2", "n"
        );

        SharedContext context = new SharedContext();
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");
        AdminController admin = new AdminController(context, new TextUserInterface(), new MockEmailService());

        startOutputCapture();
        admin.addCourse();
        assertOutputContains("successfully created");

        // Second course with same code
        setMockInput(
                "CSC3001", "Advanced Systems Again", "Another Description", "n",
                "Dr. C", "c@hindeburg.ac.nz",
                "Ms. D", "d@hindeburg.ac.nz",
                "2", "1", "n"
        );


        AdminController admin2 = new AdminController(context, new TextUserInterface(), new MockEmailService());

        startOutputCapture();
        admin2.addCourse();
        assertOutputContains("Course with that code already exists");
    }


    @Test
    public void testInvalidCourseCodeFormat() {
        setMockInput(
                "123-INVALID", "Something", "Description", "n",
                "Dr. X", "x@hindeburg.ac.nz",
                "Ms. Y", "y@hindeburg.ac.nz",
                "1", "1", "n"
        );

        SharedContext context = new SharedContext();
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");
        AdminController admin = new AdminController(context, new TextUserInterface(), new MockEmailService());

        startOutputCapture();
        admin.addCourse();
        assertOutputContains("Provided courseCode is invalid");
    }
}
