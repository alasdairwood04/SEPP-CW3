package system_tests;

import controller.AdminStaffController;
import controller.AdminStaffController;
import external.MockEmailService;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.TextUserInterface;
import view.View;

import static org.junit.jupiter.api.Assertions.*;


public class AddFAQQASystemTests extends TUITest {
    private SharedContext context;
    private AdminStaffController controller;
    private View view;

    @BeforeEach
    public void setUp() {
        view = new TextUserInterface();
        context = new SharedContext(view);
        context.currentUser = new AuthenticatedUser("admin1@hindeburg.ac.nz", "AdminStaff");
        controller = new AdminStaffController(context, new TextUserInterface(), null, new MockEmailService());

        CourseManager courseManager = context.getCourseManager();
        courseManager.addCourse(
                "CSC3001", "Test Course", "For FAQ testing", false,
                "Test Organizer", "organizer@hindeburg.ac.nz",
                "Test Secretary", "secretary@hindeburg.ac.nz",
                2, 1, "admin1@hindeburg.ac.nz"
        );
    }

    @Test
    public void testAddFAQItemAtRootLevel() {
        // Test adding an FAQ item at the root level
        setMockInput(
                // Root level requires creating a section first
                "Programming", // new topic title
                "How do I write a Java program?", // question
                "Start by creating a class with a main method.", // answer
                "n" // no course tag
        );

        startOutputCapture();
        controller.manageFAQ();

        // Look for success messages
        //assertOutputContains("Created topic 'Programming'");
        assertOutputContains("Created new FAQ item");

        // Verify the FAQ structure
        FAQManager faqManager = context.getFAQManager();
        assertEquals(1, faqManager.getSections().size());
        assertEquals("Programming", faqManager.getSections().get(0).getTopic());
        assertEquals(1, faqManager.getSections().get(0).getItems().size());
        assertEquals("How do I write a Java program?", faqManager.getSections().get(0).getItems().get(0).getQuestion());
    }

    @Test
    public void testAddFAQItemWithCourseTag() {
        // Test adding an FAQ item with a course tag
        setMockInput(
                "Web Development", // new topic
                "What is HTML?", // question
                "HTML is a markup language for the web.", // answer
                "y", // yes to course tag
                "CSC3001" // course code
        );

        startOutputCapture();
        controller.manageFAQ();

        // Look for success messages
        assertOutputContains("Created topic 'Web Development'");
        assertOutputContains("Created new FAQ item");

        // Verify the FAQ item has the course tag
        FAQ faq = context.getFAQ();
        FAQItem item = faq.getSections().get(0).getItems().get(0);
        assertEquals("CSC3001", item.getCourseTag());
    }

    @Test
    public void testAddFAQItemWithInvalidCourseTag() {
        // Test adding an FAQ item with an invalid course tag
        setMockInput(
                "Databases", // new topic
                "What is SQL?", // question
                "SQL is a query language for databases.", // answer
                "y", // yes to course tag
                "INVALID123" // invalid course code
        );

        startOutputCapture();
        controller.manageFAQ();

        // Should fail with an error message
        assertOutputContains("The tag must correspond to a course code");
    }

    @Test
    public void testAddFAQItemToExistingTopic() {
        // First add a topic
        setMockInput(
                "Algorithms", // new topic
                "What is Big O notation?", // question
                "Big O notation describes the complexity of an algorithm.", // answer
                "n", // no course tag
                "-1" // back to main menu
        );

        controller.manageFAQ();

        // Now add a second item to the same topic
        setMockInput(
                "0", // select the Algorithms topic
                "n", // don't create a new topic
                "What is a sorting algorithm?", // question
                "A sorting algorithm arranges elements in a certain order.", // answer
                "n", // no course tag
                "-1", // back to topic
                "-1" // back to main menu
        );

        startOutputCapture();
        controller.manageFAQ();

        // Check the results
        assertOutputContains("Created new FAQ item");

        FAQ faq = context.getFAQ();
        FAQSection section = faq.getSections().get(0);
        assertEquals(2, section.getItems().size());
    }

    @Test
    public void testAddFAQItemToSubtopic() {
        // First create a main topic with a subtopic
        setMockInput(
                "Programming Concepts", // main topic
                "What is object-oriented programming?", // question for main topic
                "OOP is a programming paradigm based on objects.", // answer
                "n", // no course tag
                "-1", // back to main menu
                "0", // select Programming Concepts
                "y", // create new subtopic
                "Java", // subtopic name
                "How do I declare a variable in Java?", // question for subtopic
                "Use the type followed by variable name: int x = 5;", // answer
                "n", // no course tag
                "-1", // back to subtopic
                "-1" // back to main menu
        );

        controller.manageFAQ();

        // Now add another item to the subtopic
        setMockInput(
                "0", // select Programming Concepts
                "0", // select Java subtopic
                "n", // don't create new topic
                "What are Java classes?", // question
                "Classes are blueprints for objects in Java.", // answer
                "y", // yes to course tag
                "CSC3001", // course code
                "-1", // back to subtopic
                "-1", // back to main topic
                "-1" // back to main menu
        );

        startOutputCapture();
        controller.manageFAQ();

        // Verify the structure and content
        assertOutputContains("Created new FAQ item");

        FAQ faq = context.getFAQ();
        FAQSection mainSection = faq.getSections().get(0);
        FAQSection subSection = mainSection.getSubsections().get(0);

        assertEquals("Java", subSection.getTopic());
        assertEquals(2, subSection.getItems().size());
        assertEquals("What are Java classes?", subSection.getItems().get(1).getQuestion());
        assertEquals("CSC3001", subSection.getItems().get(1).getCourseTag());
    }

    @Test
    public void testEmptyQuestionValidation() {
        // Test validation for empty question
        setMockInput(
                "Validation", // new topic
                "", // empty question
                "-1" // exit after error
        );

        startOutputCapture();
        controller.manageFAQ();

        // Check for error message
        assertOutputContains("The question cannot be empty");
    }

    @Test
    public void testEmptyAnswerValidation() {
        // Test validation for empty answer
        setMockInput(
                "Validation", // new topic
                "Is this a valid question?", // question
                "", // empty answer
                "-1" // exit after error
        );

        startOutputCapture();
        controller.manageFAQ();

        // Check for error message
        assertOutputContains("The answer cannot be empty");
    }

    @Test
    public void testUniqueIdentifiersForQuestions() {
        // Add multiple questions and verify they get unique identifiers
        setMockInput(
                "Multiple Items", // new topic
                "First question?", // first question
                "First answer.", // first answer
                "n", // no course tag
                "-1", // back to main menu
                "0", // select Multiple Items topic
                "n", // don't create a new topic
                "Second question?", // second question
                "Second answer.", // second answer
                "n", // no course tag
                "-1", // back to topic
                "-1" // back to main menu
        );

        controller.manageFAQ();

        // Verify both items have different IDs
        FAQ faq = context.getFAQ();
        FAQSection section = faq.getSections().get(0);
        FAQItem item1 = section.getItems().get(0);
        FAQItem item2 = section.getItems().get(1);

        assertNotEquals(item1.getId(), item2.getId());
    }
}

