package system_tests;

import controller.AdminStaffController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.*;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;
import view.View;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class AddFAQQASystemTests extends TUITest {

    private SharedContext context;
    private AdminStaffController controller;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, ParseException {
        // Create a new context with admin user
        context = new SharedContext(new TextUserInterface());
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");
        controller = new AdminStaffController(context, new TextUserInterface(),
                new MockAuthenticationService(), new MockEmailService());

        // Create a root FAQ section
        context.getFAQManager().addSection("General Information");
    }

    @Test
    public void testBasicFAQNavigation() {
        // Simple test to verify we can navigate the FAQ structure
        setMockInput("-1");  // Just exit management

        startOutputCapture();
        controller.manageFAQ();

        assertOutputContains("General Information");
        assertOutputContains("Return to main menu");
    }

    @Test
    public void testSimpleAddFAQ() {
        // Just add one FAQ without course tag and return
        setMockInput(
                "0",       // Select "General Information" section
                "-2",      // Choose "Add FAQ item" option
                "n",       // Don't create a new topic
                "How do I enroll in courses?", // Question
                "Use the course enrollment page.", // Answer
                "n",       // Don't add a course tag
                "-1",      // Return to main level
                "-1"       // Exit FAQ management
        );

        startOutputCapture();
        controller.manageFAQ();

        // Just verify we get a success message
        assertOutputContains("new FAQ item");
    }
}