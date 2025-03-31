package system_tests;

import controller.AdminStaffController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.*;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import view.TextUserInterface;
import view.View;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System tests for testing the "Add FAQ Q-A" functionality.
 * Since addFAQItem is private, we test it through the manageFAQ method
 * by providing specific inputs and verifying the results.
 */
public class AddFAQQASystemTests extends TUITest {

    private SharedContext context;
    private AdminStaffController controller;
    private View view;

    @BeforeEach
    public void setUp() {
        // Create a new context for each test
        context = new SharedContext(new TextUserInterface());
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");

        // Create a root section for testing
        context.getFAQManager().addSection("General Information");
    }

    /**
     * Tests adding a basic FAQ item without a course tag.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testAddFAQItem_Basic() throws URISyntaxException, IOException, ParseException {
        // Set up mock input for navigating to General Information,
        // adding a new FAQ item, and returning to the main menu
        setMockInput(
                "0",        // Select General Information section
                "-2",       // Choose Add FAQ item
                "n",        // Don't create a new topic
                "How do I register for courses?",  // Question
                "Visit the registration portal and follow the instructions.",  // Answer
                "n",        // Don't add a course tag
                "-1",       // Return to main
                "-1"        // Exit
        );

        // Create the view and controller after setting mock input
        view = new TextUserInterface();
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());

        // Capture output and run the method
        startOutputCapture();
        controller.manageFAQ();

        // Verify the output contains success message
        assertOutputContains("Created new FAQ item");

        // Verify the item was added with the correct content
        FAQSection section = context.getFAQManager().getSections().get(0);
        List<FAQItem> items = section.getItems();

        // Assertions to verify the item was added correctly
        assertEquals(1, items.size(), "Should have added exactly one FAQ item");
        FAQItem addedItem = items.get(0);
        assertEquals("How do I register for courses?", addedItem.getQuestion(), "Question text should match");
        assertEquals("Visit the registration portal and follow the instructions.", addedItem.getAnswer(), "Answer text should match");
        assertNull(addedItem.getCourseTag(), "Course tag should be null");
    }


    @Test
    public void testAddFAQWithCourseTag() throws URISyntaxException, IOException, ParseException {
        // First, add a course to use as tag
        CourseManager courseManager = context.getCourseManager();
        courseManager.addCourse(
                "CSC3001", "Computer Science 101", "Introduction to programming", false,
                "Dr. Smith", "smith@hindeburg.ac.uk", "Mrs. Jones", "jones@hindeburg.ac.uk",
                2, 1, "admin1@hindeburg.ac.nz"
        );

        // Now simulate adding a FAQ with this course tag
        setMockInput(
                "0",       // Select "General Information" section
                "-2",      // Choose "Add FAQ item" option
                "n",       // Don't create a new topic
                "What programming language is used in CSC3001?", // Question
                "Python is the main language used in the course.", // Answer
                "y",       // Add a course tag
                "CSC3001", // Course code to add as tag
                "-1",      // Return to main level
                "-1"       // Exit FAQ management
        );


        view = new TextUserInterface();
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.manageFAQ();

        // Verify the FAQ was added
        assertOutputContains("Created new FAQ item");

        // Verify the FAQ section now has the item with the course tag
        FAQSection section = context.getFAQManager().getSections().get(0);
        assertEquals(1, section.getItems().size());
        assertEquals("What programming language is used in CSC3001?", section.getItems().get(0).getQuestion());
        assertEquals("Python is the main language used in the course.", section.getItems().get(0).getAnswer());
        assertEquals("CSC3001", section.getItems().get(0).getCourseTag());
    }


    @Test
    public void testAddFAQItemWithInvalidCourseTag() throws URISyntaxException, IOException, ParseException {
        // First, add a course to use as tag
        CourseManager courseManager = context.getCourseManager();
        courseManager.addCourse(
                "CSC3001", "Computer Science 101", "Introduction to programming", false,
                "Dr. Smith", "smith@hindeburg.ac.uk", "Mrs. Jones", "jones@hindeburg.ac.uk",
                2, 1, "admin1@hindeburg.ac.nz"
        );

        // Test adding an FAQ item with an invalid course tag
        setMockInput(
                "0",       // Select "General Information" section
                "-2",      // Choose "Add FAQ item" option
                "Databases", // new topic
                "What is SQL?", // question
                "SQL is a query language for databases.", // answer
                "y", // yes to course tag
                "INVALID123", // invalid course code
                "-1",      // Return to main menu
                "-1"       // Exit
        );

        view = new TextUserInterface();
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.manageFAQ();

        // Should fail with an error message
        assertOutputContains("The tag must correspond to a course code");
    }

    /**
     * Tests adding an FAQ item with a new topic.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testAddFAQItem_WithNewTopic() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "0",        // Select General Information section
                "-2",       // Choose Add FAQ item
                "y",        // Create a new topic
                "Registration",  // New topic name
                "How do I register for courses?",  // Question
                "Visit the registration portal and follow the instructions.",  // Answer
                "n",        // Don't add a course tag
                "-1",       // Return to parent section
                "-1",       // Return to main
                "-1"        // Exit
        );

        view = new TextUserInterface();
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.manageFAQ();

        // Verify output contains topic creation and item addition messages
        assertOutputContains("Created topic 'Registration'");
        assertOutputContains("Created new FAQ item");

        // Verify the subsection was created
        FAQSection generalSection = context.getFAQManager().getSections().get(0);
        List<FAQSection> subsections = generalSection.getSubsections();
        assertEquals(1, subsections.size(), "Should have created one subsection");

        // Verify the subsection has the correct name
        FAQSection registrationSection = subsections.get(0);
        assertEquals("Registration", registrationSection.getTopic(), "Subsection should have the correct name");

        // Verify the item was added to the subsection
        List<FAQItem> items = registrationSection.getItems();
        assertEquals(1, items.size(), "Should have added one item to the subsection");

        // Verify the item content
        FAQItem addedItem = items.get(0);
        assertEquals("How do I register for courses?", addedItem.getQuestion(), "Question text should match");
        assertEquals("Visit the registration portal and follow the instructions.", addedItem.getAnswer(), "Answer text should match");
        assertNull(addedItem.getCourseTag(), "Course tag should be null");
    }

    /**
     * Tests validation of empty question input.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testAddFAQItem_EmptyQuestion_ValidationError() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "0",        // Select General Information section
                "-2",       // Choose Add FAQ item
                "n",        // Don't create a new topic
                "",         // Empty question (should be rejected)
                "-1",       // Return to main menu
                "-1"        // Exit
        );

        view = new TextUserInterface();
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.manageFAQ();

        // Verify error message about empty question
        assertOutputContains("The question cannot be empty");

        // Verify no item was added
        FAQSection section = context.getFAQManager().getSections().get(0);
        assertTrue(section.getItems().isEmpty(), "No item should be added with empty question");
    }

    /**
     * Tests validation of empty answer input.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testAddFAQItem_EmptyAnswer_ValidationError() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "0",        // Select General Information section
                "-2",       // Choose Add FAQ item
                "n",        // Don't create a new topic
                "What is the deadline for registration?",  // Question
                "",         // Empty answer (should be rejected)
                "-1",       // Return to main menu
                "-1"        // Exit
        );

        view = new TextUserInterface();
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.manageFAQ();

        // Verify error message about empty answer
        assertOutputContains("The answer cannot be empty");

        // Verify no item was added
        FAQSection section = context.getFAQManager().getSections().get(0);
        assertTrue(section.getItems().isEmpty(), "No item should be added with empty answer");
    }

    /**
     * Tests handling of course tag when no courses are available.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testAddFAQItem_WithCourseTagNoCoursesAvailable() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "0",        // Select General Information section
                "-2",       // Choose Add FAQ item
                "n",        // Don't create a new topic
                "How do I register for Algorithms?",  // Question
                "Visit the registration portal.",     // Answer
                "y",        // Try to add a course tag
                "-1",       // Return to main menu
                "-1"        // Exit
        );

        view = new TextUserInterface();
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.manageFAQ();

        // Verify message about no courses available
        assertOutputContains("No courses available in the system");

        // Verify item was added (without a tag since no courses were available)
        FAQSection section = context.getFAQManager().getSections().get(0);
        assertFalse(section.getItems().isEmpty(), "Item should be added even if course tag couldn't be added");

        FAQItem addedItem = section.getItems().get(0);
        assertEquals("How do I register for Algorithms?", addedItem.getQuestion(), "Question text should match");
        assertEquals("Visit the registration portal.", addedItem.getAnswer(), "Answer text should match");
        assertNull(addedItem.getCourseTag(), "Course tag should be null when no courses are available");
    }

    /**
     * Tests adding multiple FAQ items to the same section.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testAddFAQItem_MultipleItems() throws URISyntaxException, IOException, ParseException {
        setMockInput(
                "0",        // Select General Information section
                "-2",       // Choose Add FAQ item
                "n",        // Don't create a new topic
                "Question 1?",  // Question
                "Answer 1.",    // Answer
                "n",        // Don't add a course tag
                "0",        // Stay in General Information
                "-2",       // Add another item
                "n",        // Don't create a new topic
                "Question 2?",  // Question
                "Answer 2.",    // Answer
                "n",        // Don't add a course tag
                "-1",       // Return to main menu
                "-1"        // Exit
        );

        view = new TextUserInterface();
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.manageFAQ();

        // Verify both items were added
        FAQSection section = context.getFAQManager().getSections().get(0);
        List<FAQItem> items = section.getItems();

        assertEquals(2, items.size(), "Should have added exactly two FAQ items");
        assertEquals("Question 1?", items.get(0).getQuestion(), "First question text should match");
        assertEquals("Answer 1.", items.get(0).getAnswer(), "First answer text should match");
        assertEquals("Question 2?", items.get(1).getQuestion(), "Second question text should match");
        assertEquals("Answer 2.", items.get(1).getAnswer(), "Second answer text should match");
    }

    /**
     * Tests adding an FAQ item to a nested subsection.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testAddFAQItem_NestedSections() throws URISyntaxException, IOException, ParseException {
        // First, create a nested structure - General Information > Registration
        FAQSection generalSection = context.getFAQManager().getSections().get(0);
        FAQSection registrationSection = new FAQSection("Registration");
        generalSection.addSubsection(registrationSection);

        setMockInput(
                "0",        // Select General Information
                "0",        // Select Registration subsection
                "-2",       // Add FAQ item
                "n",        // Don't create another subsection
                "How do I register late?", // Question
                "Contact the registrar's office.", // Answer
                "n",        // No course tag
                "-1",       // Return to General Information
                "-1",       // Return to main menu
                "-1"        // Exit
        );

        view = new TextUserInterface();
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.manageFAQ();

        // Verify item was added to the Registration subsection
        assertEquals(1, registrationSection.getItems().size(), "Should have added one item to the subsection");

        FAQItem addedItem = registrationSection.getItems().get(0);
        assertEquals("How do I register late?", addedItem.getQuestion(), "Question text should match");
        assertEquals("Contact the registrar's office.", addedItem.getAnswer(), "Answer text should match");
    }

    /**
     * Tests adding a new subsection with a name that already exists.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testAddFAQItem_DuplicateTopicName() throws URISyntaxException, IOException, ParseException {
        // First create a Registration subsection
        FAQSection generalSection = context.getFAQManager().getSections().get(0);
        FAQSection registrationSection = new FAQSection("Registration");
        generalSection.addSubsection(registrationSection);

        setMockInput(
                "0",        // Select General Information
                "-2",       // Add FAQ item
                "y",        // Create a new topic
                "Registration", // Topic that already exists
                "How do I register late?", // Question
                "Contact the registrar's office.", // Answer
                "n",        // No course tag
                "-1",       // Return to main menu
                "-1"        // Exit
        );

        view = new TextUserInterface();
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());

        startOutputCapture();
        controller.manageFAQ();

        // Should display a warning and add to existing section
        assertOutputContains("Topic 'Registration' already exists");

        // Item should be added to existing Registration section
        assertEquals(1, registrationSection.getItems().size(), "Should have added item to existing section");
        assertEquals("How do I register late?", registrationSection.getItems().get(0).getQuestion());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testFAQItemIDAssignment() throws URISyntaxException, IOException, ParseException {
        // Set up mock input for navigating to General Information,
        // adding multiple FAQ items, and returning to the main menu
        setMockInput(
                "0",        // Select General Information section
                "-2",       // Choose Add FAQ item
                "n",        // Don't create a new topic
                "First question?",  // Question 1
                "First answer.",    // Answer 1
                "n",        // Don't add a course tag
                "0",        // Stay in General Information section
                "-2",       // Add another FAQ item
                "n",        // Don't create a new topic
                "Second question?", // Question 2
                "Second answer.",   // Answer 2
                "n",        // Don't add a course tag
                "0",        // Stay in General Information section
                "-2",       // Add another FAQ item
                "n",        // Don't create a new topic
                "Third question?",  // Question 3
                "Third answer.",    // Answer 3
                "n",        // Don't add a course tag
                "-1",       // Return to main menu
                "-1"        // Exit
        );

        // Create the view and controller after setting mock input
        view = new TextUserInterface();
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());

        // Run the method
        controller.manageFAQ();

        // Verify the IDs of the added items
        FAQSection section = context.getFAQManager().getSections().get(0);
        List<FAQItem> items = section.getItems();

        // Check that we have 3 items
        assertEquals(3, items.size(), "Should have added 3 FAQ items");

        // Check that the IDs are assigned sequentially
        assertEquals(0, items.get(0).getId(), "First item should have ID 0");
        assertEquals(1, items.get(1).getId(), "Second item should have ID 1");
        assertEquals(2, items.get(2).getId(), "Third item should have ID 2");

        // Also verify that the questions and answers match
        assertEquals("First question?", items.get(0).getQuestion());
        assertEquals("First answer.", items.get(0).getAnswer());
        assertEquals("Second question?", items.get(1).getQuestion());
        assertEquals("Second answer.", items.get(1).getAnswer());
        assertEquals("Third question?", items.get(2).getQuestion());
        assertEquals("Third answer.", items.get(2).getAnswer());
    }
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void testFAQItemIDsInDifferentSections() throws URISyntaxException, IOException, ParseException {
        // Add a second section
        context.getFAQManager().addSection("Technical Support");

        // Set up input for adding to both sections
        setMockInput(
                "0",        // Select "General Information" section
                "-2",       // Add FAQ item
                "n",        // Don't create a new topic
                "General question?", // Question
                "General answer.",   // Answer
                "n",        // No course tag
                "-1",        // Select "Technical Support" section
                "1",
                "-2",       // Add FAQ item
                "n",        // Don't create a new topic
                "Technical question?", // Question
                "Technical answer.",   // Answer
                "n",        // No course tag
                "-1",       // Return to main menu
                "-1"        // Exit
        );

        view = new TextUserInterface();
        controller = new AdminStaffController(context, view,
                new MockAuthenticationService(), new MockEmailService());

        controller.manageFAQ();

        // Verify each section has its own ID sequence
        FAQSection generalSection = context.getFAQManager().getSections().get(0);
        FAQSection techSection = context.getFAQManager().getSections().get(1);

        assertEquals(1, generalSection.getItems().size(), "General section should have 1 item");
        assertEquals(1, techSection.getItems().size(), "Tech section should have 1 item");

        // Each section should start its IDs from 0
        assertEquals(0, generalSection.getItems().get(0).getId(), "General section item should have ID 0");
        assertEquals(0, techSection.getItems().get(0).getId(), "Tech section item should have ID 0");
    }
}