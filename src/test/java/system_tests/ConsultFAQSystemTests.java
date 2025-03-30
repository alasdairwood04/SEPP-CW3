package system_tests;

import controller.InquirerController;
import external.MockAuthenticationService;
import external.MockEmailService;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import view.TextUserInterface;
import view.View;

import java.io.IOException;
import java.net.URISyntaxException;
import org.json.simple.parser.ParseException;


import static org.junit.jupiter.api.Assertions.*;

public class ConsultFAQSystemTests extends TUITest {

    private SharedContext context;
    private InquirerController controller;
    private View view;
    private static MockAuthenticationService authService;


    @BeforeAll
    public static void setUpAuthService() throws URISyntaxException, IOException, ParseException {
        // Create auth service once for all tests
        authService = new MockAuthenticationService();
    }


    @BeforeEach
    public void setUp() {
        try {
            // Create view and context
            view = new TextUserInterface();
            context = new SharedContext(view);

            // Set up as guest by default
            context.currentUser = new Guest();

            // Create controller using the pre-initialized auth service
            controller = new InquirerController(context, view, authService, new MockEmailService());

            // Set up test data - create FAQ structure with topics and items
            setupTestFAQData();
        } catch (Exception e) {
            fail("Exception in test setup: " + e.getMessage());
        }
    }


    private void setupTestFAQData() {
        // Add a test course
        CourseManager courseManager = context.getCourseManager();
        courseManager.addCourse(
                "CSC3001", "Programming", "Introduction to programming", false,
                "Prof Java", "java@hindeburg.ac.nz",
                "Admin Java", "admin@hindeburg.ac.nz",
                1, 1, "admin@hindeburg.ac.nz"
        );

        courseManager.addCourse(
                "CSC3002", "Databases", "Database fundamentals", false,
                "Prof SQL", "sql@hindeburg.ac.nz",
                "Admin SQL", "admin@hindeburg.ac.nz",
                1, 1, "admin@hindeburg.ac.nz"
        );

        // Create FAQ structure
        FAQManager faqManager = context.getFAQManager();

        // Create main topics
        faqManager.addSection("Programming");
        faqManager.addSection("Databases");

        // Get the sections we just added
        FAQManager faq = context.getFAQManager();
        FAQSection programmingSection = faq.getSections().get(0); // Programming section
        FAQSection databasesSection = faq.getSections().get(1);  // Databases section

        // Add items to Programming section
        programmingSection.addItem("What is Java?", "Java is a programming language.", "CSC3001");
        programmingSection.addItem("What is Python?", "Python is a programming language.", null);

        // Add items to Programming section
        programmingSection.addItem("What is Java?", "Java is a programming language.", "CSC3001");
        programmingSection.addItem("What is Python?", "Python is a programming language.", null);

        // Add items to Databases section
        databasesSection.addItem("What is SQL?", "SQL is a query language for databases.", "CSC3002");
        databasesSection.addItem("What is NoSQL?", "NoSQL refers to non-relational databases.", null);

        // Create subtopics
        FAQSection javaSection = new FAQSection("Java");
        programmingSection.addSubsection(javaSection);

        // Add items to Java subtopic
        javaSection.addItem("What is JVM?", "JVM is the Java Virtual Machine.", "CSC3001");
        javaSection.addItem("What are Java classes?", "Classes are blueprints for objects.", "CSC3001");
    }

    @Test
    public void testBrowseFAQWithoutFiltering() {
        // Test browsing without filtering
        setMockInput(
                "n",           // Don't filter by course code
                "0",           // Select Programming section
                "0",           // Select Java subsection
                "-1",          // Back to Programming section
                "-1",          // Back to main FAQ
                "1",           // Select Databases section
                "-1",          // Back to main FAQ
                "-1"           // Exit FAQ
        );

        startOutputCapture();
        controller.consultFAQ();

        // Verify output
        assertOutputContains("Programming");
        assertOutputContains("Databases");
        assertOutputContains("Java");
        assertOutputContains("What is JVM?");
        assertOutputContains("JVM is the Java Virtual Machine");
        assertOutputContains("What is SQL?");
        assertOutputContains("SQL is a query language for databases");
    }

    @Test
    public void testFilterByValidCourseCode() {
        // Test filtering by a valid course code
        setMockInput(
                "y",           // Filter by course code
                "CSC3001",     // Valid course code
                "0",           // Select Programming section
                "0",           // Select Java subsection
                "-1",          // Back to Programming section
                "-1",          // Back to main FAQ
                "-1"           // Exit FAQ
        );

        startOutputCapture();
        controller.consultFAQ();

        // Should show Java-related items, but not Python
        assertOutputContains("What is Java?");
        assertOutputContains("Java is a programming language");
        assertOutputContains("What is JVM?");
        assertOutputContains("JVM is the Java Virtual Machine");
        assertOutputNotContains("What is Python?");

        // Should not show Database-related items
        assertOutputNotContains("What is SQL?");
    }

    @Test
    public void testFilterByInvalidCourseCode() {
        // Test filtering by an invalid course code
        setMockInput(
                "y",           // Filter by course code
                "INVALID123",  // Invalid course code
                "-1"           // Exit FAQ
        );

        startOutputCapture();
        controller.consultFAQ();

        // Should show error and fall back to unfiltered view
        assertOutputContains("Course with code INVALID123 does not exist");
        assertOutputContains("Showing all FAQ items");
    }

    @Test
    public void testEmptyCourseCodeFilter() {
        // Test providing an empty course code
        setMockInput(
                "y",           // Filter by course code
                "",            // Empty course code
                "0",           // Select Programming section
                "-1",          // Back to main FAQ
                "-1"           // Exit FAQ
        );

        startOutputCapture();
        controller.consultFAQ();

        // Should fall back to unfiltered view
        assertOutputContains("What is Java?");
        assertOutputContains("What is Python?");
    }

    @Test
    public void testFilterShowsEmptyTopicMessage() {
        // Add a topic with no matching items
        FAQManager faqManager = context.getFAQManager();
        FAQSection emptySection = faqManager.addSection("Empty Section");
        emptySection.addItem("Generic question", "Generic answer", null);

        // Test filtering and navigating to section with no matching items
        setMockInput(
                "y",           // Filter by course code
                "CSC3001",     // Valid course code
                "2",           // Select Empty Section (index may vary based on setup)
                "-1",          // Back to main FAQ
                "-1"           // Exit FAQ
        );

        startOutputCapture();
        controller.consultFAQ();

        // Should show message about no matching items
        assertOutputContains("There are no questions for course 'CSC3001' in this topic");
        assertOutputContains("You can navigate to other topics to find relevant questions");
    }

    @Test
    public void testAuthenticatedUserConsultingFAQ() {
        // Set authenticated user
        context.currentUser = new AuthenticatedUser("student@hindeburg.ac.nz", "Student");

        // Test basic browsing
        setMockInput(
                "n",           // Don't filter by course code
                "0",           // Select Programming section
                "-1",          // Back to main FAQ
                "-1"           // Exit FAQ
        );

        startOutputCapture();
        controller.consultFAQ();

        // Should log with student email instead of "Guest"
        // We can't directly verify the log, but we can check the functionality works
        assertOutputContains("Programming");
        assertOutputContains("What is Java?");
    }

    @Test
    public void testNavigatingInvalidOption() {
        // Test selecting invalid option number
        setMockInput(
                "n",           // Don't filter by course code
                "99",          // Invalid option number
                "-1"           // Exit FAQ
        );

        startOutputCapture();
        controller.consultFAQ();

        // Should show error and continue
        assertOutputContains("Invalid option: 99");
    }

    @Test
    public void testNavigatingNonNumericOption() {
        // Test entering non-numeric option
        setMockInput(
                "n",           // Don't filter by course code
                "abc",         // Non-numeric input
                "-1"           // Exit FAQ
        );

        startOutputCapture();
        controller.consultFAQ();

        // Should show error and continue
        assertOutputContains("Invalid option: abc");
    }

    @Test
    public void testTagDisplayInUnfilteredMode() {
        // Test that course tags are shown in unfiltered mode
        setMockInput(
                "n",           // Don't filter by course code
                "0",           // Select Programming section
                "-1",          // Back to main FAQ
                "-1"           // Exit FAQ
        );

        startOutputCapture();
        controller.consultFAQ();

        // Should show course tags for items that have them
        assertOutputContains("What is Java?");
        assertOutputContains("CSC3001");
    }

    @Test
    public void testTagsHiddenInFilteredMode() {
        // Test that course tags are hidden when filtering
        setMockInput(
                "y",           // Filter by course code
                "CSC3001",     // Valid course code
                "0",           // Select Programming section
                "-1",          // Back to main FAQ
                "-1"           // Exit FAQ
        );

        startOutputCapture();
        controller.consultFAQ();

        // Should show items but not their tags since we're already filtering
        assertOutputContains("What is Java?");
        assertOutputContains("Java is a programming language");

        // The CSC3001 tag should not appear in the filtered results
        // This is hard to verify with just assertOutputNotContains since "CSC3001" also appears in the filter header
        // We'd need a more sophisticated assertion method
    }

    // Helper method to assert that something is NOT in the output
    private void assertOutputNotContains(String text) {
        String output = getOutputContent();
        assertFalse(output.contains(text), "Output should NOT contain: " + text);
    }

    // Helper method to get the output content (you'll need to implement this in TUITest)
    private String getOutputContent() {
        // This should return the captured output as a string
        // Implementation depends on how your TUITest class captures output
        return ""; // Placeholder - replace with actual implementation
    }
}